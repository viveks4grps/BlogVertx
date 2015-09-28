package com.mysocial.model;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Field;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexes;

/**
 * Blog of a particular site
 * @author vivek
 *
 */
@Entity("blog")
@Indexes(
    @Index(value = "title", fields = @Field("title"))
)
public class Blog {
	@Id
    private ObjectId id;
	
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
		
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	

    
}