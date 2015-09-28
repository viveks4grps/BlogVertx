package com.mysocial.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

import com.google.gson.Gson;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.mysocial.BlogCommentHandler;
import com.mysocial.model.Comment;

public class CommentsUtil {

	private static IMap<String, List<Comment>> commentsMap=null;
	
	private static class BlogCommentCombination {
		private String blogId;
		private Comment comment;
		public BlogCommentCombination() {
			
		}
		public BlogCommentCombination(String blogId, Comment comment) {
			super();
			this.blogId = blogId;
			this.comment = comment;
		}
		public String getBlogId() {
			return blogId;
		}
		public void setBlogId(String blogId) {
			this.blogId = blogId;
		}
		public Comment getComment() {
			return comment;
		}
		public void setComment(Comment comment) {
			this.comment = comment;
		}
	}
	public static void initCommentsMap(HazelcastInstance hzInstance, boolean readFromDB) {
	    
	    //HazelcastInstance hzInstance = Hazelcast.newHazelcastInstance();
	    /*commentsMap = hzInstance.getMap( "commentsStore" );
	    if(readFromDB) {
	    	Map commentsFromDB = BlogCommentHandler.readCommentsFromDB();
	    	commentsMap.putAll(commentsFromDB);
	    }*/
	    	
	}
	
	public static void updateDistributedMap(String blogId, Comment comment) {
		/*ObjectId objId=new ObjectId(blogId);
		List<Comment> comments=commentsMap.get(objId);
		if(comments==null){
			comments = new ArrayList<Comment>();
		}
		comments.add(comment);
		commentsMap.put(objId.toHexString(), comments);*/
		persistComments(blogId,comment);
	}

	public static List getCommentsForBlog(String objectId) {
		if(commentsMap==null){
			return new ArrayList();
		}
		
		//ObjectId objId=new ObjectId(objectId);
		List<Comment> comments=commentsMap.get(objectId);
		if(comments==null){
			comments = new ArrayList<Comment>();
		}
		return comments;
	}
	
	private static void persistComments(String blogId, Comment newComment) {
		BlogCommentHandler.handleComments(newComment, blogId);
	}
	
	public static String getJsonSringFromComment(String blogId, Comment newComment){
		BlogCommentCombination objComments=new BlogCommentCombination(blogId,newComment);
		//JSONObject jsonObj =  new JSONObject(objComments);
		Gson gson = new Gson();
		return gson.toJson(objComments);
	}
	public static void handleCommentsEventBusMessage(String jsonString) {
		Gson gson = new Gson();
		BlogCommentCombination data = gson.fromJson(jsonString, 
				BlogCommentCombination.class);
		updateDistributedMap(data.getBlogId(),data.getComment());
	}
}
