package com.cairn.ui.model;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Protocol {

	private int id;
	private int catagoryId;
	private String name;
	private String goal;
	private String progress;
	private String description;
	private String status;
	private ArrayList<ProtocolComments> comments;
	private boolean needsAttention;
	private int templateId;
	private Date lastStatus;
	private Date startDate;
	private String dueDate;
	private Date completionDate;
	private String completionPercent;
	private int stepCount;
	private int completedSteps;

	private ArrayList<ProtocolStep> steps = new ArrayList<ProtocolStep>();
	private ArrayList<Integer> users = new ArrayList<Integer>();
	private int userId;

	public int getUserId() {
		return this.userId;
	}
	public void setUserId(int id) {
		this.userId = id;
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
	public ArrayList<ProtocolStep> getSteps() {
		return steps;
	}
	public void setSteps(ArrayList<ProtocolStep> steps) {
		this.steps = steps;
	}
	public String getDescription() {
		return description;
	}
	public boolean isNeedsAttention() {
		return needsAttention;
	}
	public Date getLastStatus() {
		return lastStatus;
	}
	public String getCompletionPercent() {
		float temp = Float.parseFloat(this.completionPercent);
		return new DecimalFormat("#.##").format(temp * 100.0);
	}
	public ArrayList<Integer> getUsers() {
		return users;
	}
	public void setDescription(String description) {
		this.description = description;
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
	public String getGoal() {
		return goal;
	}
	public String getProgress() {
		return this.progress;
	}
	public void setGoal(String goal) {
		this.goal = goal;
	}
	public void setProgress(String progress) {
		this.progress = progress;
	}
	
	public void setStepCount() {
		this.stepCount = steps.size();
	}
	
	public int getStepCount()
	{
		return stepCount;
	}
	public void setCompletedSteps() {
	    int completed = 0;
	    for (ProtocolStep step : steps) { 
	        if ("Done".equals(step.getStatus())) {
	            completed += 1;
	        }
	    }
	    this.completedSteps = completed;
	}
	public int getCompletedSteps() {
		return completedSteps;
	}
	public int getCatagoryId() {
		return catagoryId;
	}
	public void setCatagoryId(int catagoryId) {
		this.catagoryId = catagoryId;
	}
	public void setStepCount(int stepCount) {
		this.stepCount = stepCount;
	}
	public void setCompletedSteps(int completedSteps) {
		this.completedSteps = completedSteps;
	}
	public ArrayList<ProtocolComments> getComments() {
		return comments;
	}
	public void setComments(ArrayList<ProtocolComments> comments) {
		this.comments = comments;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
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
	public int getTemplateId() {
		return templateId;
	}
	public void setTemplateId(int templateId) {
		this.templateId = templateId;
	}
	public String getDueDate() {
		return dueDate;
	}
	public void setDueDate(String dueDate) {
		this.dueDate = dueDate;
	}

}
