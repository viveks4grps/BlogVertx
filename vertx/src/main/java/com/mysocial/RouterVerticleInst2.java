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

public class RouterVerticleInst2 extends BaseRouterVerticle {

	private static final String NODE_NAME = "Node2";
	private static final int NODE_PORT = 9091;
	
	public RouterVerticleInst2() {
		currentNodeId = NODE_NAME;
		currentNodePort = NODE_PORT;
	}
	/*public RouterVerticleInst1(String currentNodeId, int currentNodePort,
			boolean createDistMap) {
		super(createDistMap);
	}*/
	
	public static void main(String[] args) {
		createAndDeployVerticle(RouterVerticleInst2.class.getName(),true,NODE_NAME);
	}

}