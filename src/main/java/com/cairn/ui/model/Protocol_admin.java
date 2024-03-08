package com.cairn.ui.model;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Protocol_admin {
    private int id;
    private String name;
    private int numClients;
    private List<Protocol_step_admin> steps;

    public Protocol_admin() {
        this.steps = new ArrayList<>(); // Initialize the steps list
    }

    // Getters and setters for id, name, numClients
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

    public int getNumClients() {
        return numClients;
    }

    public void setNumClients(int numClients) {
        this.numClients = numClients;
    }

    public List<Protocol_step_admin> getSteps() {
        return steps;
    }

    public void setSteps(List<Protocol_step_admin> steps) {
        this.steps = steps;
    }

    // Method to find a protocol by its ID
    public static Protocol_admin findById(int id) {
        for (Protocol_admin protocol : getList_admin()) {
            if (protocol.getId() == id) {
                return protocol;
            }
        }
        return null; // or throw a custom exception as per your requirement
    }

    static public ArrayList<Protocol_admin> getList_admin() {
        ArrayList<Protocol_admin> results = new ArrayList<>();
        String data = "{\n"
        		+ "    \"protocols\": [\n"
        		+ "        {\n"
        		+ "            \"id\": 3,\n"
        		+ "            \"name\": \"protocol-1\",\n"
        		+ "            \"steps\": [\n"
        		+ "                {\"stepId\": 1, \"stepName\": \"Step 1\", \"details\": \"Details for Step 1\"},\n"
        		+ "                {\"stepId\": 2, \"stepName\": \"Step 2\", \"details\": \"Details for Step 2\"}\n"
        		+ "            ]\n"
        		+ "        },\n"
        		+ "        {\n"
        		+ "            \"id\": 4,\n"
        		+ "            \"name\": \"protocol-2\",\n"
        		+ "            \"steps\": [\n"
        		+ "                {\"stepId\": 1, \"stepName\": \"Step 1\", \"details\": \"Details for Step 1\"},\n"
        		+ "                {\"stepId\": 2, \"stepName\": \"Step 2\", \"details\": \"Details for Step 2\"}\n"
        		+ "            ]\n"
        		+ "        }\n"
        		+ "    ]\n"
        		+ "}"; 

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(data);
            JsonNode protocolsNode = jsonNode.get("protocols");
            if (protocolsNode.isArray()) {
                for (JsonNode protocolNode : protocolsNode) {
                    Protocol_admin protocol = new Protocol_admin();
                    protocol.setId(protocolNode.get("id").asInt());
                    protocol.setName(protocolNode.get("name").asText());
                    ArrayList<Protocol_step_admin> steps = new ArrayList<>();
                    for (JsonNode stepNode : protocolNode.get("steps")) {
                        Protocol_step_admin step = new Protocol_step_admin();
                        step.setStepId(stepNode.get("stepId").asInt());
                        step.setStepName(stepNode.get("stepName").asText());
                        step.setDetails(stepNode.get("details").asText());
                        steps.add(step);
                    }
                    protocol.setSteps(steps);
                    results.add(protocol);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }
}

