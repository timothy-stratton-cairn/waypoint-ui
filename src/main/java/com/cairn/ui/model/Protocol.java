package com.cairn.ui.model;

import java.util.ArrayList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Protocol {

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
	
	static public ArrayList<Protocol> getList() {
		ArrayList<Protocol> results = new ArrayList();
		
		String data = "{\n"
				+ "    \"protocols\": [\n"
				+ "        {\n"
				+ "            \"id\": 3,\n"
				+ "            \"name\": \"protocol-1\"\n"
				+ "        },\n"
				+ "        {\n"
				+ "            \"id\": 4,\n"
				+ "            \"name\": \"protocol-2\"\n"
				+ "        }\n"
				+ "    ],\n"
				+ "    \"numOfProtocols\": 2\n"
				+ "}";
		
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            JsonNode jsonNode = objectMapper.readTree(data);
            JsonNode prots = jsonNode.get("protocols");
            // Iterate through the array elements
            Protocol entry = null;
            if (prots.isArray()) {
                for (JsonNode element : prots) {
                    // Access and print array elements
                	if (element != null) {
                		entry = new Protocol();
                		entry.setName(element.get("name").toString());
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
