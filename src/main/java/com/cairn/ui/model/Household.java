package com.cairn.ui.model;

import java.util.ArrayList;

public class Household{
	private int id;
	private String name;
	private String description;
	private ArrayList<Integer>householdAccountsIds;
	private ArrayList<Integer>primaryContactsIds;
	private ArrayList<User>householdAccounts;
	private ArrayList<User>primaryContacts;
    public Household() {
        this.householdAccounts = new ArrayList<>();
        this.primaryContacts = new ArrayList<>();
        this.primaryContactsIds = new ArrayList<>();
        this.householdAccountsIds = new ArrayList<>();
    }
	
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

	public ArrayList<Integer> getHouseholdAccountsIds() {
		return householdAccountsIds;
	}

	public void setHouseholdAccountsIds(ArrayList<Integer> householdAccountsIds) {
		this.householdAccountsIds = householdAccountsIds;
	}

	public ArrayList<Integer> getPrimaryContactsIds() {
		return primaryContactsIds;
	}

	public void setPrimaryContactsIds(ArrayList<Integer> primaryContactsIds) {
		this.primaryContactsIds = primaryContactsIds;
	}
}