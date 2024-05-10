package com.cairn.ui.model;

import java.util.ArrayList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Homework {

	private int id;
	private int templateId;
	private String description;
	private String name;
	private String response;
	private int clientId;
	private int parentStepId;
	private ArrayList<HomeworkQuestion> questions;

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
	public void setDescription(String Description) {
		this.description = Description;
	}
	public String getDescription() {
		return description;
	}
	public int getTemplateId() {
		return templateId;
	}
	public String getResponse() {
		return response;
	}
	public int getClientId() {
		return clientId;
	}
	public void setTemplateId(int templateId) {
		this.templateId = templateId;
	}
	public void setResponse(String response) {
		this.response = response;
	}
	public void setClientId(int clientId) {
		this.clientId = clientId;
	}
	public ArrayList<HomeworkQuestion> getQuestions() {
		return questions;
	}
	public void setQuestions(ArrayList<HomeworkQuestion> questions) {
		this.questions = questions;
	}
	public int getParentStepId() {
		return parentStepId;
	}
	public void setParentStepId(int parentStepId) {
		this.parentStepId = parentStepId;
	}
	
}
