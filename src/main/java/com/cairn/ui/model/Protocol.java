package com.cairn.ui.model;

public class Protocol {

	private int id;
	private String name;
	private int numClients;

	public int getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public int getNumClients() {
		return numClients;
	}
	public void setId(int id) {
		this.id = id;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setNumClients(int numClients) {
		this.numClients = numClients;
	}	
}
