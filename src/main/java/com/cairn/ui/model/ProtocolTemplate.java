package com.cairn.ui.model;

import java.util.ArrayList;
import java.util.List;

public class ProtocolTemplate {

	private int id;
	private String name;
	private String description;

	private int type;
	private boolean active;
	private List<Integer> dueDate;
	private List<Integer> schedule;
	private ArrayList<ProtocolStepTemplate> steps = new ArrayList<ProtocolStepTemplate>();

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
	public int getType() {
		/* 2 types of protocols *
		  0: Undefined
		  1: Life Events
		  2: Life Cycle */
		return type;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public ArrayList<ProtocolStepTemplate> getSteps() {
		return steps;
	}
	public void setSteps(ArrayList<ProtocolStepTemplate> steps) {
		this.steps = steps;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public List<Integer> getSchedule() {
		return schedule;
	}
	public void setSchedule(List<Integer> schedule) {
		this.schedule = schedule;
	}
	public void setDueDate(List<Integer> dueDate) {
		this.dueDate = dueDate;
	}
	public List<Integer> getDueDate() {
		return dueDate;
	}
}
