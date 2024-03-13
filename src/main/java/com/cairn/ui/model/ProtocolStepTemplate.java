package com.cairn.ui.model;

public class ProtocolStepTemplate {

	private int id;
	private String name;
	private int type;

	public String getTypeDisplay() {
		String returnValue = "Undefined";
		switch (this.type) {
		case 1:
			returnValue = "Gather Data";
			break;
		case 2:
			returnValue = "Run Analysis";
			break;
		case 3:
			returnValue = "Craft Recommendation";
			break;
		case 4:
			returnValue = "Share Education";
			break;
		}
		return returnValue;
	}
	public int getType() {
		/* 4 types of steps *
		  0: Undefined
		  1: Gather Data
		  2: Run Analysis
		  3: Craft Recommendation
		  4: Share Education
		   */
		return type;
	}
	public void setType(int type) {
		this.type = type;
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
}
