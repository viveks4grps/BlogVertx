package com.mysocial.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import com.mysocial.utils.CommentsUtil;
/**
 * DTO class for transfering Blog data over json
 * @author vivek
 *
 */

public class BlogDTO implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Blog toModel(){
    	Blog blog = new Blog();
    	if(id!=null)
    		blog.setId(new ObjectId(id));
    	blog.setUserName(userName);
    	blog.setUserFirst(userFirst);
    	blog.setUserLast(userLast);
    	blog.setTags(tags);
    	blog.setTitle(title);
    	blog.setContent(content);
    	/*List<ObjectId> commentIdList= new ArrayList<>();
    	for(String commentId:commentIds){
    		commentIdList.add(new ObjectId(commentId));
    	}*/
    	blog.setComments(comments);
    	blog.setDate(date);
    	return blog;
    }
    
    public BlogDTO fillFromModel(Blog blog){
    	id = blog.getId()!=null?blog.getId().toHexString():null;
    	userName = blog.getUserName();
    	userFirst = blog.getUserFirst();
    	userLast = blog.getUserLast();
    	tags = blog.getTags();
    	content = blog.getContent();
    	title=blog.getTitle();
    	date= blog.getDate();
    	/*List<String> commentIdList= new ArrayList<>();
    	for(ObjectId commentId:blog.getCommentIds()){
    		commentIdList.add(commentId!=null?commentId.toHexString():null);
    	}
    	*/
    	comments= blog.getComments();
    	
    	
    	
    	//comments = CommentsUtil.getCommentsForBlog(blog.getId().toHexString());
    	return this;
    }
    
    
    private String id;
	
	public void setId(String id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getTags() {
		return tags;
	}
	public void setTags(String tags) {
		this.tags = tags;
	}
	private String title;
    private String tags;
    private String content;
    private String userFirst;
    private String userName;
    private String date;
    
    private List<Comment> comments = new ArrayList<>();
    
    public List<Comment> getComments() {
		return comments;
	}
	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getUserFirst() {
		return userFirst;
	}
	public void setUserFirst(String userFirst) {
		this.userFirst = userFirst;
	}
	public String getUserLast() {
		return userLast;
	}
	public void setUserLast(String userLast) {
		this.userLast = userLast;
	}
	private String userLast;
    public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
	
	public String getId() {
		return id;
	}
	

    
}