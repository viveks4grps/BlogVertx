package com.mysocial;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.query.Query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysocial.model.User;
import com.mysocial.model.UserDTO;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.web.RoutingContext;

class LoginHandler implements Handler<RoutingContext> {
	public void handle(RoutingContext routingContext) {
		System.out.println("Thread LoginHandler: "
				+ Thread.currentThread().getId());
		// This handler will be called for every request
		HttpServerResponse response = routingContext.response();
		routingContext.request().bodyHandler(new Handler<Buffer>() {
			public void handle(Buffer buf) {
				String json = buf.toString("UTF-8");
				ObjectMapper mapper = new ObjectMapper();
				UserDTO dto = null;
				try {
					dto = mapper.readValue(json, UserDTO.class);
				} catch (IOException e) {
					e.printStackTrace();
				}

				String userName= dto.getUserName();
				String password = dto.getPassword();
				Datastore dataStore = ServicesFactory.getMongoDB();
				List<User> users = dataStore.createQuery(User.class).field("userName").equal(userName).asList();
				if (users.size() != 0) {
					UserDTO dtoDB = new UserDTO().fillFromModel(users.get(0));
					if(dtoDB.getPassword().equals(password)){
						response.setStatusCode(204).end("Login Successful");
						Query<User> updateQuery = dataStore.createQuery(User.class).field(Mapper.ID_KEY).equal(dtoDB.toModel().getId());
						dataStore.update(updateQuery, dataStore.createUpdateOperations(User.class).set("signedIn", true));
						routingContext.setUser(new io.vertx.ext.auth.User() {

							@Override
							public void setAuthProvider(AuthProvider authProvider) {
								// TODO Auto-generated method stub

							}

							@Override
							public JsonObject principal() {
								Map<String, Object> dataMap = new HashMap<>();
								dataMap.put("userFirst", dtoDB.getFirst());
								dataMap.put("userLast", dtoDB.getLast());
								dataMap.put("userName", dtoDB.getUserName());
								JsonObject obj = new JsonObject(dataMap);
								return obj;
							}

							@Override
							public io.vertx.ext.auth.User isAuthorised(String authority, Handler<AsyncResult<Boolean>> resultHandler) {
								// TODO Auto-generated method stub
								return this;
							}

							@Override
							public io.vertx.ext.auth.User clearCache() {
								// TODO Auto-generated method stub
								return null;
							}
						});
					}
					else {
						response.setStatusCode(404).end("Login Error");
					}

				} else {
					response.setStatusCode(404).end("Login Error");
				}


			};
		});
	}	
}