package com.cairn.ui.model;

public class ProtocolStep {

	private int id;
	private String name;
	private String description;
	private String notes;
	private String status;
	private int categoryId;
	private String categoryName;
	private String categoryDescription;

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
	public int getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}
	public String getCategoryName() {
		return categoryName;
	}
	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
	public String getCategoryDescription() {
		return categoryDescription;
	}
	public void setCategoryDescription(String categoryDescription) {
		this.categoryDescription = categoryDescription;
	}
}
