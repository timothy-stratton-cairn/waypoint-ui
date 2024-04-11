package com.cairn.ui.model;


import java.util.List;

public class AssignedUsers {
  private List<AssignedUser> accounts;
  private int numOfAccounts;

  public Integer getNumOfAccounts() {
    return accounts.size();
  }

	public List<AssignedUser> getAccounts() {
		return accounts;
	}
	
	public void setAccounts(List<AssignedUser> accounts) {
		this.accounts = accounts;
	}

	public void setNumOfAccounts(int numOfAccounts) {
		this.numOfAccounts = numOfAccounts;
	}
}
