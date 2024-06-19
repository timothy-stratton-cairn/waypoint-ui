package com.cairn.ui.model;

import java.util.Date;

public class ProtocolComments{
	private String comment;
	private Date takenAt;
	private String takenBy;
	private String commentType;
	
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public Date getTakenAt() {
		return takenAt;
	}
	public void setTakenAt(Date takenAt) {
		this.takenAt = takenAt;
	}
	public String getTakenBy() {
		return takenBy;
	}
	public void setTakenBy(String takenBy) {
		this.takenBy = takenBy;
	}
	public String getCommentType() {
		return commentType;
	}
	public void setCommentType(String commentType) {
		this.commentType = commentType;
	}
	
}