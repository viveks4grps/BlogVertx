package com.mysocial;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.AuthHandler;
import io.vertx.ext.web.handler.BasicAuthHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.UserSessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import java.util.ArrayList;
import java.util.List;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import com.mysocial.model.Comment;
import com.mysocial.utils.CommentsUtil;

public class RouterVerticle extends AbstractVerticle {

	private static String currentNodeId = "Node1";
	private static int currentNodePort = 9090;
	
	
	public static void main(String[] args) {
		System.setProperty("vertx.disableFileCaching", "true");
		ClusterManager cm = new HazelcastClusterManager();
		VertxOptions opts = new VertxOptions().setClusterManager(cm);
		Vertx.clusteredVertx(opts, res -> {
			if (res.succeeded()) {
				Vertx vertx = res.result();
				vertx.deployVerticle(RouterVerticle.class.getName());
				//Ways to communicate with other systems in the cluster
				vertx.eventBus().consumer(currentNodeId, m->{
					//System.out.println("Cluster Message meant for Node1: "+m.body());
					CommentsUtil.handleCommentsEventBusMessage((String) m.body());
				});
				CommentsUtil.initCommentsMap(((HazelcastClusterManager)cm).getHazelcastInstance(),true);
			} else {
				System.out.println("Cluster start failure");
				// failed!
			}
		});

	}

	//Maintains a list of all current websocket connections to the server
	private static List<ServerWebSocket> allConnectedSockets = new ArrayList<>();
	@Override
	public void start(Future<Void> startFuture) throws Exception {
		HttpServer server = vertx.createHttpServer();
		Router router = Router.router(vertx);

		router.route().handler(CookieHandler.create());
		router.route().handler(
				SessionHandler.create(LocalSessionStore.create(vertx)));
		AuthProvider ap = new MyAuthProvier();
		router.route().handler(UserSessionHandler.create(ap));

		AuthHandler basicAuthHandler = BasicAuthHandler.create(ap);

		router.route("/private/*").handler(basicAuthHandler);
		router.route("/private/*").handler(new Handler<RoutingContext>() {
			@Override
			public void handle(RoutingContext rc) {
				System.out.println("Handler: " + rc.user().principal());
				rc.response().end("Done");
			}
		});

		//Setup websocket handler
		server.websocketHandler(serverWebSocket -> {
			//Got a new connection
			System.out.println("Connected: "+serverWebSocket.remoteAddress());
			//Store new connection in list
			allConnectedSockets.add(serverWebSocket);
			//Setup handler to receive the data
			serverWebSocket.handler( handler ->{
				String message = new String(handler.getBytes());
				System.out.println("message: "+message);
				//Now broadcast received message to all other clients
				for(ServerWebSocket sock : allConnectedSockets){
					System.out.println("Sending message to client...");
					Buffer buf = Buffer.buffer();
					buf.appendBytes(message.getBytes());
					sock.writeFinalTextFrame(message);
				}
			});
			//Register handler to remove connection from list when connection is closed
			serverWebSocket.closeHandler(handler->{
				allConnectedSockets.remove(serverWebSocket);
			});

		});
		
		router.get("/Services/rest/loadblogs").handler(new BlogLoader());
		router.get("/Services/rest/loadMyBlogs").handler(new MyBlogLoader());
		router.post("/Services/users").handler(new UserPersister());
		router.get("/Services/rest/user").handler(new UserHandler());
		router.post("/Services/rest/user/auth").handler(new LoginHandler());
		//router.post("/Services/rest/blogs/:blogId/comments").handler(new BlogCommentHander());
		router.post("/Services/rest/blogs").handler(new BlogPersister());

		router.post("/Services/rest/logout").handler(new LogoutHandler());
		
		ZooKeeper zk = new ZooKeeper("localhost:2181", 3000,watchedEvent -> {
			System.out.println(watchedEvent.getPath());
			System.out.println(watchedEvent);
		});
		
		router.post("/Services/rest/blogs/:blogId/comments").handler(routingCtx -> {
			HttpServerResponse response = routingCtx.response();
			
			routingCtx.request().bodyHandler(new Handler<Buffer>() {
				public void handle(Buffer buf) {
					String blogId = routingCtx.request().getParam("blogId");
					//Persist comment in Mongo DB using existing Handler
					
					Comment newComment=BlogCommentHandler.extractCommentsData(routingCtx, buf, blogId);
					//First try to take ownership of this blog comments
					try{
						//If below call succeeds that means we are the owner of the blog and comments will be stored here
						zk.create("/"+blogId, currentNodeId.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
						CommentsUtil.updateDistributedMap(blogId,newComment);
						//TODO : store comments in hashmap (write a private function for same) 
					}catch(Exception e){
						//We are not the owner.. so find the owner
						try {
							byte[] ownerNodeAddress = zk.getData("/"+blogId, false, null);
							//And send that owner node the blog comments to be cached
							String jsonString =   CommentsUtil.getJsonSringFromComment(blogId, newComment);
							vertx.eventBus().publish(new String(ownerNodeAddress),jsonString);
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
				};
			});
			
		});
		
		router.route("/*").handler(StaticHandler.create());		
		
		server.requestHandler(router::accept).listen(currentNodePort);
		
		System.out.println("Thread Router Start: "
				+ Thread.currentThread().getId());
		System.out.println("STARTED ROUTER");
		startFuture.complete(); 
	}
	
	

}