package com.mysocial;

import java.util.ArrayList;
import java.util.List;

import org.mongodb.morphia.Datastore;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysocial.model.Blog;
import com.mysocial.model.BlogDTO;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

class MyBlogLoader implements Handler<RoutingContext> {
	public void handle(RoutingContext routingContext) {
		System.out.println("Thread MyBlogLoader: "
				+ Thread.currentThread().getId());
		// This handler will be called for every request
		HttpServerResponse response = routingContext.response();
		//String id = routingContext.request().getParam("id");

		response.putHeader("content-type", "application/json");
		Datastore dataStore = ServicesFactory.getMongoDB();

		String blogUserName=routingContext.user().principal().getString("userName");

		List<Blog> blogs = dataStore.createQuery(Blog.class).field("userName")
				.equal(blogUserName).asList();
		List<BlogDTO> blogdtos= new ArrayList<>();

		for (Blog blog : blogs){
			blogdtos.add(new BlogDTO().fillFromModel(blog));
		}
		if (blogdtos.size() != 0) {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode node = mapper.valueToTree(blogdtos);
			response.end(node.toString());
		} else {
			response.setStatusCode(204).end("no blogs found");
		}
	}
}