package com.cairn.ui.model;

import java.util.Date;

public class ProtocolStepNote {
	private int noteId;
	private String note;
	private String takenBy;
	private String type;
	private Date takenAt;
	
	
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public String getTakenBy() {
		return takenBy;
	}
	public void setTakenBy(String takenBy) {
		this.takenBy = takenBy;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Date getTakenAt() {
		return takenAt;
	}
	public void setTakenAt(Date takenAt) {
		this.takenAt = takenAt;
	}
	public int getNoteId() {
		return noteId;
	}
	public void setNoteId(int noteId) {
		this.noteId = noteId;
	}
	
}