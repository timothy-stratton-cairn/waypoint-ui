package com.cairn.ui.model;

import java.util.ArrayList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HomeworkTemplate {

	private int id;
	private String name;
	private int numClients;

	public int getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public int getNumClients() {
		return numClients;
	}
	public void setId(int id) {
		this.id = id;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setNumClients(int numClients) {
		this.numClients = numClients;
	}
	
	static public ArrayList<HomeworkTemplate> getList() {
		ArrayList<HomeworkTemplate> results = new ArrayList();
		
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
            HomeworkTemplate entry = null;
            if (prots.isArray()) {
                for (JsonNode element : prots) {
                    // Access and print array elements
                	if (element != null) {
                		entry = new HomeworkTemplate();
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
	
}
