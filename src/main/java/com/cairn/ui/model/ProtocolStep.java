package com.cairn.ui.model;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ProtocolStep {

	private int id;
	private String name;
	private String description;
	private String notes;
	private String status;
	private int categoryId;
	private String categoryName;
	private String categoryDescription;
	private Date startDate;
	private Date completionDate;
	private int daysToComplete;

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
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getCompletionDate() {
		return completionDate;
	}
	public void setCompletionDate(Date completionDate) {
		this.completionDate = completionDate;
	}
	public int getDaysToComplete() {
        if (completionDate == null || startDate == null) {
            return -1; // -1 means the protocol has yet to be completed and can be left out of the calculus
        } else {
            long diffInMilli = completionDate.getTime() - startDate.getTime();
            return (int) TimeUnit.DAYS.convert(diffInMilli, TimeUnit.MILLISECONDS);
        }
    }
}
