package com.cairn.ui.model;

import java.util.ArrayList;
import java.util.List;

public class HomeworkTemplate {

	private int id;
	private String name;
	private String description;
	private String status;
	private List<HomeworkQuestionsTemplate> questions;
	private int numClients;
	
	public HomeworkTemplate() {
	    this.questions = new ArrayList<>();
	}

	public int getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public String getDescription() {
		return description;
	}
	public int getNumClients() {
		return numClients;
	}
	
	public List<HomeworkQuestionsTemplate> getQuestions() {
	        return questions;
	    }
	    
	public void setId(int id) {
		this.id = id;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public void setNumClients(int numClients) {
		this.numClients = numClients;
	}

    public void setQuestions(List<HomeworkQuestionsTemplate> questions) {
        this.questions = questions;
    }

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
    
}
