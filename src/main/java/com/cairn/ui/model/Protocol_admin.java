package com.cairn.ui.model;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Protocol_admin {
    private int id;
    private String name;
    private int numClients;
    private List<Integer> steps; 

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

    public List<Integer> getSteps() {
        return steps;
    }

    public void setSteps(List<Integer> steps) {
        this.steps = steps;
    }

    // Method to find a protocol by its ID
    public static Protocol_admin findById(int id) {
        for (Protocol_admin protocol : getList_admin()) {
            if (protocol.getId() == id) {
                return protocol;
            }
        }
        return null;
    }
    public void addStep(Integer stepId) {
        if (!this.steps.contains(stepId)) {
            this.steps.add(stepId);
        }
    }

    static public ArrayList<Protocol_admin> getList_admin() {
        ArrayList<Protocol_admin> results = new ArrayList<>();
        String data = "{\n"
                + "    \"protocols\": [\n"
                + "        {\n"
                + "            \"id\": 3,\n"
                + "            \"name\": \"Marriage\",\n"
                + "            \"steps\": [\n"
                + "                {\"stepId\": 1},\n"
                + "                {\"stepId\": 2}\n"
                + "            ]\n"
                + "        },\n"
                + "        {\n"
                + "            \"id\": 4,\n"
                + "            \"name\": \"House Purchase\",\n"
                + "            \"steps\": [\n"
                + "                {\"stepId\": 1},\n"
                + "                {\"stepId\": 2}\n"
                + "            ]\n"
                + "        },\n"
                + "			{\n"
                + "            \"id\": 6,\n"
                + "            \"name\": \"Retirement\",\n"
                + "            \"steps\": [\n"
                + "                {\"stepId\": 1},\n"
                + "                {\"stepId\": 2}\n"
                + "            ]\n"
                + "        },\n"
                + "			{\n"
                + "            \"id\": 7,\n"
                + "            \"name\": \"Baby\",\n"
                + "            \"steps\": [\n"
                + "                {\"stepId\": 1},\n"
                + "                {\"stepId\": 2},\n"
                + "                {\"stepId\": 3}\n"
                + "            ]\n"
                + "        },\n"
                + "			{\n"
                + "            \"id\": 8,\n"
                + "            \"name\": \"New Client\",\n"
                + "            \"steps\": [\n"
                + "                {\"stepId\": 1},\n"
                + "                {\"stepId\": 2},\n"
                + "                {\"stepId\": 3}\n"
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
                    List<Integer> steps = new ArrayList<>();
                    for (JsonNode stepNode : protocolNode.get("steps")) {
                        steps.add(stepNode.get("stepId").asInt());
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

