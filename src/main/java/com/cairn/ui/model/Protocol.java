package com.cairn.ui.model;

import java.util.ArrayList;
import java.util.Date;

public class Protocol {

	private int id;
	private String name;
	private String description;
	private String comment;
	private boolean needsAttention;
	private Date lastStatus;
	private String completionPercent;
	private ArrayList<ProtocolStep> steps = new ArrayList<ProtocolStep>();
	private ArrayList<Integer> users = new ArrayList<Integer>();

	public int getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public void setId(int id) {
		this.id = id;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ArrayList<ProtocolStep> getSteps() {
		return steps;
	}
	public void setSteps(ArrayList<ProtocolStep> steps) {
		this.steps = steps;
	}
	public String getDescription() {
		return description;
	}
	public String getComment() {
		return comment;
	}
	public boolean isNeedsAttention() {
		return needsAttention;
	}
	public Date getLastStatus() {
		return lastStatus;
	}
	public String getCompletionPercent() {
		return completionPercent;
	}
	public ArrayList<Integer> getUsers() {
		return users;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public void setNeedsAttention(boolean needsAttention) {
		this.needsAttention = needsAttention;
	}
	public void setLastStatus(Date lastStatus) {
		this.lastStatus = lastStatus;
	}
	public void setCompletionPercent(String completionPercent) {
		this.completionPercent = completionPercent;
	}
	public void setUsers(ArrayList<Integer> users) {
		this.users = users;
	}
	public void addUser(int id) {
		this.users.add(id);
		
	}
	
	
	
}
