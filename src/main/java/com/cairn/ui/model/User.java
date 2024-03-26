package com.cairn.ui.model;

import java.util.ArrayList;
import java.util.Date;

public class User {
	private int id;
	private String email;
	private String pwd;
	private String verifyPwd;
	private String username;
	private String firstName;
	private String lastName;
	private String authToken;
	private User coclient;
	private ArrayList<String> roles = new ArrayList<String>();
	private ArrayList<User> dependents = new ArrayList<User>();	
	private ArrayList<String> permissions = new ArrayList<String>();
	
	public String getVerifypassword() {
		return verifyPwd;
	}

	public void setVerifypassword(String verifyPwd) {
		this.verifyPwd = verifyPwd;
	}

	public void addPermission(String perm) {
		this.permissions.add(perm);
	}
	
	public ArrayList<String> getPermissions() {
		return this.permissions;
	}
	
	private Date lastLogin;
	private Date created;
	private Date updated;
	
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return the pwd
	 */
	public String getPwd() {
		return pwd;
	}

	/**
	 * @param pwd the pwd to set
	 */
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	/**
	 * @return the pwd
	 */
	public String getPassword() {
		return pwd;
	}

	/**
	 * @param pwd the pwd to set
	 */
	public void setPassword(String pwd) {
		this.pwd = pwd;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the firstName
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * @param firstName the firstName to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * @return the lastName
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * @param lastName the lastName to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * @return the refresh token
	 */
	public String getToken() {
		return  this.authToken;
	}

	/**
	 * @param token the auth token to set
	 */
	public void setToken(String token) {
		this.authToken = token;
	}

	/**
	 * @return the lastLogin
	 */
	public Date getLastLogin() {
		return lastLogin;
	}

	/**
	 * @param lastLogin the lastLogin to set
	 */
	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}

	/**
	 * @return the created
	 */
	public Date getCreated() {
		return created;
	}

	/**
	 * @param created the created to set
	 */
	public void setCreated(Date created) {
		this.created = created;
	}

	/**
	 * @return the updated
	 */
	public Date getUpdated() {
		return updated;
	}

	/**
	 * @param updated the updated to set
	 */
	public void setUpdated(Date updated) {
		this.updated = updated;
	}

	public User(){
		// Nothing to initialize
	}
	
	public User (String email, String pwd, String name){
		this.username = name;
		this.email = email;
		this.pwd = pwd;
	}

	public String getVerifyPwd() {
		return verifyPwd;
	}

	public String getAuthToken() {
		return authToken;
	}

	public User getCoclient() {
		return coclient;
	}

	public ArrayList<String> getRoles() {
		return roles;
	}

	public ArrayList<User> getDependents() {
		return dependents;
	}

	public void setVerifyPwd(String verifyPwd) {
		this.verifyPwd = verifyPwd;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public void setCoclient(User coclient) {
		this.coclient = coclient;
	}

	public void setRoles(ArrayList<String> roles) {
		this.roles = roles;
	}

	public void setDependents(ArrayList<User> dependents) {
		this.dependents = dependents;
	}

	public void setPermissions(ArrayList<String> permissions) {
		this.permissions = permissions;
	}
}
