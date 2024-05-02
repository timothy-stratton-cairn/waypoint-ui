package com.cairn.ui.dto;

import java.util.Set;

public class AccountDetailsDto {

  private String username;
  private String firstName;
  private String lastName;
  private Set<String> roles;
  private String email;
  private LinkedAccountDetailsDto coClient;
  private Set<LinkedAccountDetailsDto> dependents;
  
  
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public Set<String> getRoles() {
		return roles;
	}
	public void setRoles(Set<String> roles) {
		this.roles = roles;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public LinkedAccountDetailsDto getCoClient() {
		return coClient;
	}
	public void setCoClient(LinkedAccountDetailsDto coClient) {
		this.coClient = coClient;
	}
	public Set<LinkedAccountDetailsDto> getDependents() {
		return dependents;
	}
	public void setDependents(Set<LinkedAccountDetailsDto> dependents) {
		this.dependents = dependents;
	}
}
