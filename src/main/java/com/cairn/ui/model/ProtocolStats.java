package com.cairn.ui.model;

public class ProtocolStats {
	private int templateId;
	private int assignedUsers;
	private int numSteps;
	private int progress;
	private int done;
	private String completion;
	private String templateName;
	public int getTemplateId() {
		return templateId;
	}
	public int getAssignedUsers() {
		return assignedUsers;
	}
	public int getNumSteps() {
		return numSteps;
	}
	public int getProgress() {
		return progress;
	}
	public int getDone() {
		return done;
	}
	public String getCompletion() {
		return completion;
	}
	public String getTemplateName() {
		return templateName;
	}
	public void setTemplateId(int templateId) {
		this.templateId = templateId;
	}
	public void setAssignedUsers(int assignedUsers) {
		this.assignedUsers = assignedUsers;
	}
	public void setNumSteps(int numSteps) {
		this.numSteps = numSteps;
	}
	public void setProgress(int progress) {
		this.progress = progress;
	}
	public void setDone(int done) {
		this.done = done;
	}
	public void setCompletion(String completion) {
		this.completion = completion;
	}
	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

}
