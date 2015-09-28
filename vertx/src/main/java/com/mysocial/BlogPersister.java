package com.mysocial;

import java.io.IOException;
import java.util.Date;

import org.mongodb.morphia.Datastore;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysocial.model.BlogDTO;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

class BlogPersister implements Handler<RoutingContext> {
	public void handle(RoutingContext routingContext) {
		System.out.println("Thread BlogPersister: "
				+ Thread.currentThread().getId());
		// This handler will be called for every request
		HttpServerResponse response = routingContext.response();
		routingContext.request().bodyHandler(new Handler<Buffer>() {
			public void handle(Buffer buf) {
				String json = buf.toString("UTF-8");
				ObjectMapper mapper = new ObjectMapper();
				BlogDTO dto = null;
				try {
					dto = mapper.readValue(json, BlogDTO.class);
				} catch (IOException e) {
					e.printStackTrace();
				}
				dto.setUserFirst(routingContext.user().principal().getString("userFirst"));
				dto.setUserLast(routingContext.user().principal().getString("userLast"));
				dto.setUserName(routingContext.user().principal().getString("userName"));
				dto.setDate(new Date(System.currentTimeMillis()).toString());
				System.out.println("User Name is "+routingContext.user().principal());
				Datastore dataStore = ServicesFactory.getMongoDB();
				dataStore.save(dto.toModel());
				response.setStatusCode(204).end("Data saved");
			};
		});
	}
}