package com.mysocial.model;


import java.io.IOException;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

/**
 * Comment of a particular blogs
 * @author vivek
 *
 */
@Entity("comment")

public class Comment implements com.hazelcast.nio.serialization.DataSerializable{
	@Id
    private ObjectId id;
	
	
    private String content;
    private String userFirst;
    private String userName;
    private String date;
    
   
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
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		/*byte[] idBytes = id.toByteArray();
		int length = idBytes.length;
		out.write(length);*/
		//out.write(id.toByteArray());
		out.writeObject(id);
		out.writeUTF(content);
		out.writeUTF(userFirst);
		out.writeUTF(userName);
		out.writeUTF(date);
		
	}
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		/*int length = in.readInt();
		byte[] idBytes = new byte[length];
		in.readFully(idBytes, 0, length);*/
		/*byte[] idBytes = in.readByteArray();
		id = new ObjectId(idBytes);*/
		id=in.readObject();
		content = in.readUTF();
		userFirst = in.readUTF();
		userName = in.readUTF();
		date = in.readUTF();
	}
	

    
}