package com.mysocial;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.query.Query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysocial.model.Blog;
import com.mysocial.model.BlogDTO;
import com.mysocial.model.Comment;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

 public class BlogCommentHandler implements Handler<RoutingContext> {
	public void handle(RoutingContext routingContext) {
		System.out.println("Thread BlogCommentHandler: "
				+ Thread.currentThread().getId());
		// This handler will be called for every request
		HttpServerResponse response = routingContext.response();
		String blogId = routingContext.request().getParam("blogId");

		routingContext.request().bodyHandler(new Handler<Buffer>() {
			public void handle(Buffer buf) {
				handleComments(routingContext, buf, blogId);

				response.setStatusCode(204).end("Data saved");
			};
		});
	}
	
	public static void handleComments(Comment commentData,String blogId) {
		ObjectMapper mapper = new ObjectMapper();
		Datastore dataStore = ServicesFactory.getMongoDB();

		if( commentData == null) 
			return;
		/*try {
			commentModel = mapper.readValue(json, Comment.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		commentModel.setUserFirst(routingContext.user().principal().getString("userFirst"));
		commentModel.setUserLast(routingContext.user().principal().getString("userLast"));
		commentModel.setUserName(routingContext.user().principal().getString("userName"));
		commentModel.setDate(new Date(System.currentTimeMillis()).toString());*/
		dataStore.save(commentData);

		ObjectId oid = null;
		try {
			oid = new ObjectId(blogId);
		} catch (Exception e) {// Ignore format errors
		}

		List<Blog> blogs = dataStore.createQuery(Blog.class).field("id")
				.equal(oid).asList();
		BlogDTO blogDto =null;
		if (blogs.size() != 0) {
			blogDto = new BlogDTO().fillFromModel(blogs.get(0));
		}
		Query<Blog> updateQuery = dataStore.createQuery(Blog.class).field(Mapper.ID_KEY).equal(blogDto.toModel().getId());
		dataStore.update(updateQuery, dataStore.createUpdateOperations(Blog.class).add("comments", commentData));
	
		
	}
	public static Comment extractCommentsData(RoutingContext routingContext, Buffer buf, String blogId) {
		String json = buf.toString("UTF-8");
		Comment commentData = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			commentData = mapper.readValue(json, Comment.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		commentData.setUserFirst(routingContext.user().principal().getString("userFirst"));
		commentData.setUserLast(routingContext.user().principal().getString("userLast"));
		commentData.setUserName(routingContext.user().principal().getString("userName"));
		commentData.setDate(new Date(System.currentTimeMillis()).toString());
		return commentData;
	}
	
	public static Map readCommentsFromDB() {
		HashMap commentsDBMap = new HashMap();
		Datastore dataStore = ServicesFactory.getMongoDB();
		List<Blog> blogs = dataStore.createQuery(Blog.class).asList();
		for(Blog blog:blogs) {
			commentsDBMap.put(blog.getId().toHexString(), blog.getComments());
		}
		return commentsDBMap;
		
	}
	
	public static void handleComments(RoutingContext routingContext, Buffer buf, String blogId){
		String json = buf.toString("UTF-8");
		ObjectMapper mapper = new ObjectMapper();
		Datastore dataStore = ServicesFactory.getMongoDB();

		Comment commentModel = null;
		try {
			commentModel = mapper.readValue(json, Comment.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		commentModel.setUserFirst(routingContext.user().principal().getString("userFirst"));
		commentModel.setUserLast(routingContext.user().principal().getString("userLast"));
		commentModel.setUserName(routingContext.user().principal().getString("userName"));
		commentModel.setDate(new Date(System.currentTimeMillis()).toString());
		dataStore.save(commentModel);

		ObjectId oid = null;
		try {
			oid = new ObjectId(blogId);
		} catch (Exception e) {// Ignore format errors
		}

		List<Blog> blogs = dataStore.createQuery(Blog.class).field("id")
				.equal(oid).asList();
		BlogDTO blogDto =null;
		if (blogs.size() != 0) {
			blogDto = new BlogDTO().fillFromModel(blogs.get(0));
		}
		Query<Blog> updateQuery = dataStore.createQuery(Blog.class).field(Mapper.ID_KEY).equal(blogDto.toModel().getId());
		dataStore.update(updateQuery, dataStore.createUpdateOperations(Blog.class).add("comments", commentModel));
	}
}