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
	private ArrayList<String>householdGoals;
    public Household() {
        this.householdAccounts = new ArrayList<>();
        this.householdGoals = new ArrayList<>();
        /* This data is for place holder until the API exists to overwrite it. */
        this.householdGoals.add("Your goal of saving $100,000 in six years has five years left and you have saved $15,000");
        this.householdGoals.add("Your plan is to invest 15% of your income of $5000/month for $750/month. You currently have $500/month going into Roth IRA and $100/month going into a mutual fund.");
        this.householdGoals.add("You need $2,000,000 life insurance and $500,000 LTD. Currently you have 1.5 Mil in term life and a $500,000 LTD benefit.");
        this.householdGoals.add("You have $20,000 in CC debt, $40,000 in student loans and a $250,000 mortgage. You are currently paying down $2,500/month and can be down to mortgage inly in 24 months.");
        this.householdGoals.add("Your current tax rate overall is 20%. We recommend paying less.");
        this.householdGoals.add("Your current estate has a value of $1. Your goal of $1,000,000 needs help.");
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
	public ArrayList<String> getHouseholdGoals() {
		return householdGoals;
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