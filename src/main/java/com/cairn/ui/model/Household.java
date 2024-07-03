package com.cairn.ui.model;

import java.util.ArrayList;

public class Household{
	private int id;
	private String name;
	private String description;
	private ArrayList<User>householdAccounts;
	private ArrayList<User>primaryContacts;
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ArrayList<User> getHouseholdAccounts() {
		return householdAccounts;
	}
	public void setHouseholdAccounts(ArrayList<User> householdAccounts) {
		this.householdAccounts = householdAccounts;
	}
	
	public int getNumberOfAccounts() {
		return householdAccounts.size();
	}
	public ArrayList<User> getPrimaryContacts() {
		return primaryContacts;
	}
	public void setPrimaryContacts(ArrayList<User> primaryContacts) {
		this.primaryContacts = primaryContacts;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}