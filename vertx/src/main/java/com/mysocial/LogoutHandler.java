package com.mysocial;

import java.util.List;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.query.Query;

import com.mysocial.model.User;
import com.mysocial.model.UserDTO;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

class LogoutHandler implements Handler<RoutingContext> {
	public void handle(RoutingContext routingContext) {
		System.out.println("Thread LogoutHandler: "
				+ Thread.currentThread().getId());
		//set SignedIn Param to false in DB;
		String userName=routingContext.user().principal().getString("userName");
		Datastore dataStore = ServicesFactory.getMongoDB();
		List<User> users = dataStore.createQuery(User.class).field("userName").equal(userName).asList();
		if (users.size() != 0) {
			UserDTO dtoDB = new UserDTO().fillFromModel(users.get(0));
			Query<User> updateQuery = dataStore.createQuery(User.class).field(Mapper.ID_KEY).equal(dtoDB.toModel().getId());
			dataStore.update(updateQuery, dataStore.createUpdateOperations(User.class).set("signedIn", false));

		}

		routingContext.clearUser();

		// Redirect back to the login page
		routingContext.response().putHeader("location", "/").setStatusCode(302).end();
	}
}