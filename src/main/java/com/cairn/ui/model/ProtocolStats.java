package com.cairn.ui.model;

import java.util.ArrayList;

public class ProtocolStats {
	private int templateId;
	private AssignedUsers assignedUsers;
	private int numSteps;
	private int progress;
	private int done;
	private String completion;
	private String templateName;
	private ArrayList<Household> assignedHouseholds;
	
	
	public int getTemplateId() {
		return templateId;
	}
	public AssignedUsers getAssignedUsers() {
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
	public void setAssignedUsers(AssignedUsers assignedUsers) {
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
	public ArrayList<Household> getAssignedHouseholds() {
		return assignedHouseholds;
	}
	public void setAssignedHouseholds(ArrayList<Household> assignedHouseholds) {
		this.assignedHouseholds = assignedHouseholds;
	}

}
