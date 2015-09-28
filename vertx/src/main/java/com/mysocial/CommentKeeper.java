package com.mysocial;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.zookeeper.AsyncCallback.StringCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

public class CommentKeeper extends AbstractVerticle {
	private static String currentNodeId = "Node1";
	private static int currentNodePort = 8080;
	
//	// Convenience method so you can run it in your IDE
	public static void main(String[] args) {
		System.setProperty("vertx.disableFileCaching", "true");
		ClusterManager cm = new HazelcastClusterManager();
		VertxOptions opts = new VertxOptions().setClusterManager(cm);
		Vertx.clusteredVertx(opts, res -> {
			if (res.succeeded()) {
				Vertx vertx = res.result();
				vertx.deployVerticle(CommentKeeper.class.getName());
//				//Ways to communicate with other systems in the cluster
				vertx.eventBus().consumer(currentNodeId, m->{
//					System.out.println("Cluster Message meant for Node1: "+m.body());
				});
			} else {
				System.out.println("Cluster start failure");
//				// failed!
			}
		});

	}

	// Maintains a list of all current websocket connections to the server
	private static List<ServerWebSocket> allConnectedSockets = new ArrayList<>();

	@Override
	public void start() throws IOException {
		Router router = Router.router(vertx);
		HttpServer server = vertx.createHttpServer();
		// Setup websocket handler
		server.websocketHandler(serverWebSocket -> {
			// Got a new connection
			System.out.println("Connected: " + serverWebSocket.remoteAddress());
			// Store new connection in list
			allConnectedSockets.add(serverWebSocket);
			// Setup handler to receive the data
			serverWebSocket.handler(handler -> {
				String message = new String(handler.getBytes());
				System.out.println("message: " + message);
				// Now broadcast received message to all other clients
					for (ServerWebSocket sock : allConnectedSockets) {
						System.out.println("Sending message to client...");
						Buffer buf = Buffer.buffer();
						buf.appendBytes(message.getBytes());
						sock.writeFinalTextFrame(message);
					}
				});
			// Register handler to remove connection from list when connection
			// is closed
			serverWebSocket.closeHandler(handler -> {
				allConnectedSockets.remove(serverWebSocket);
			});

		});
		// Some sample route handler for non websocket request
		router.route("/somepath").handler(
				routingCtx -> {
					System.out.println("Handling request for /somepath");
					routingCtx.response().setStatusCode(200);
					routingCtx.response().setStatusMessage("OK");
					routingCtx.response().putHeader("content-length",
							"" + "String response to somepath".length());
					routingCtx.response().write("String response to somepath");
					routingCtx.response().end();
				});
		ZooKeeper zk = new ZooKeeper("localhost:2181", 3000,watchedEvent -> {
			System.out.println(watchedEvent.getPath());
			System.out.println(watchedEvent);
		});
		router.route("/addComment").handler(routingCtx -> {
			String blogId = routingCtx.request().getParam("blogId");
			//First try to take ownership of this blog comments
			try{
				//If below call succeeds that means we are the owner of the blog and comments will be stored here
				zk.create("/"+blogId, currentNodeId.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
			}catch(Exception e){
				//We are not the owner.. so find the owner
				try {
					byte[] ownerNodeAddress = zk.getData("/"+blogId, false, null);
					//And send that owner node the blog comments to be cached
					vertx.eventBus().publish(new String(ownerNodeAddress),"Comment data");
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
			
		});
		// Serve the static pages
		router.route().handler(StaticHandler.create("webroot"));
		server.requestHandler(router::accept).listen(currentNodePort);
		System.out.println("Server is started");

	}
}
