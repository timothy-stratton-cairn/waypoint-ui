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
	static public ArrayList<Homework> getList() {
		ArrayList<Homework> results = new ArrayList();
		
		String data = "{\n"
				+ "    \"protocols\": [\n"
				+ "        {\n"
				+ "            \"id\": 1,\n"
				+ "            \"name\": \"New Client\"\n"
				+ "        },\n"
				+ "        {\n"
				+ "            \"id\": 2,\n"
				+ "            \"name\": \"House Purchase\"\n"
				+ "        },\n"
				+ "        {\n"
				+ "            \"id\": 3,\n"
				+ "            \"name\": \"Job Change/Retirement\"\n"
				+ "        },\n"
				+ "        {\n"
				+ "            \"id\": 4,\n"
				+ "            \"name\": \"Marriage\"\n"
				+ "        },\n"
				+ "        {\n"
				+ "            \"id\": 5,\n"
				+ "            \"name\": \"Baby\"\n"
				+ "        }\n"
				+ "    ],\n"
				+ "    \"numOfProtocols\": 5\n"
				+ "}";
		
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            JsonNode jsonNode = objectMapper.readTree(data);
            JsonNode prots = jsonNode.get("protocols");
            // Iterate through the array elements
            Homework entry = null;
            if (prots.isArray()) {
                for (JsonNode element : prots) {
                    // Access and print array elements
                	if (element != null) {
                		entry = new Homework();
                		entry.setName(element.get("name").asText());
                		entry.setId(Integer.valueOf(element.get("id").toString()));
                		results.add(entry);
                	}
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

		return results;
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
	
}
