package com.cairn.ui.model;

import java.util.ArrayList;

public class Protocol {

	private int id;
	private String name;
	private int status;
	private ArrayList<ProtocolStep> steps = new ArrayList<ProtocolStep>();


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
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public ArrayList<ProtocolStep> getSteps() {
		return steps;
	}
	public void setSteps(ArrayList<ProtocolStep> steps) {
		this.steps = steps;
	}	
	
	
}
