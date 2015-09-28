package com.mysocial;

import java.util.List;

import org.mongodb.morphia.Datastore;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysocial.model.User;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

class UserHandler implements Handler<RoutingContext> {
	public void handle(RoutingContext routingContext) {
		System.out.println("Thread userHandler: "
				+ Thread.currentThread().getId());
		// This handler will be called for every request
		HttpServerResponse response = routingContext.response();
		String signedInParam = routingContext.request().getParam("signedIn");

		response.putHeader("content-type", "application/json");
		Datastore dataStore = ServicesFactory.getMongoDB();

		List<User> users = dataStore.createQuery(User.class).field("signedIn")
				.equal(Boolean.parseBoolean(signedInParam)).asList();
		if (users.size() != 0) {
			//UserDTO dto = new UserDTO().fillFromModel(users.get(0));
			ObjectMapper mapper = new ObjectMapper();
			JsonNode node = mapper.valueToTree(users);
			response.end(node.toString());
		} else {
			response.setStatusCode(404).end("not found");
		}
	}
}