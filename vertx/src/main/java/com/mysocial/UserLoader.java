package com.mysocial;

import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysocial.model.User;
import com.mysocial.model.UserDTO;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

class UserLoader implements Handler<RoutingContext> {
	public void handle(RoutingContext routingContext) {
		System.out.println("Thread UserLoader: "
				+ Thread.currentThread().getId());
		// This handler will be called for every request
		HttpServerResponse response = routingContext.response();
		String id = routingContext.request().getParam("id");

		response.putHeader("content-type", "application/json");
		Datastore dataStore = ServicesFactory.getMongoDB();
		ObjectId oid = null;
		try {
			oid = new ObjectId(id);
		} catch (Exception e) {// Ignore format errors
		}
		List<User> users = dataStore.createQuery(User.class).field("id")
				.equal(oid).asList();
		if (users.size() != 0) {
			UserDTO dto = new UserDTO().fillFromModel(users.get(0));
			ObjectMapper mapper = new ObjectMapper();
			JsonNode node = mapper.valueToTree(dto);
			response.end(node.toString());
		} else {
			response.setStatusCode(404).end("not found");
		}
	}
}