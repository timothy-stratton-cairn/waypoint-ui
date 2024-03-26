package com.cairn.ui.model;

public class ProtocolStep {

	private int id;
	private String name;
	private String description;
	private String notes;
	private String category;
	private String status;
	public String getCategory() {
		/* 4 types of steps *
		  0: Undefined
		  1: Gather Data
		  2: Run Analysis
		  3: Craft Recommendation
		  4: Share Education
		   */
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}

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
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getDescription() {
		return description;
	}
	public String getNotes() {
		return notes;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
}
