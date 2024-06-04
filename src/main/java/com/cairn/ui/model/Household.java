package com.cairn.ui.model;

import java.util.ArrayList;

public class Household{
	private int id;
	private String name;
	private ArrayList<User>householdAccounts;
	
	
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
}