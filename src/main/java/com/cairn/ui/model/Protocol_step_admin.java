package com.cairn.ui.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;

public class Protocol_step_admin {
    private int stepId;
    private String stepName;
    private String details;

    public Protocol_step_admin() {}

    public int getStepId() {
        return stepId;
    }

    public void setStepId(int stepId) {
        this.stepId = stepId;
    }

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    // Sample JSON data for demonstration
    private static final String jsonData = "{\n" +
            "  \"steps\": [\n" +
            "    {\"stepId\": 1, \"stepName\": \"Step 1 Name\", \"details\": \"Details for Step 1\"},\n" +
            "    {\"stepId\": 2, \"stepName\": \"Step 2 Name\", \"details\": \"Details for Step 2\"},\n" +
            "    {\"stepId\": 3, \"stepName\": \"Step 3 Name\", \"details\": \"Details for Step 3\"},\n" +
            "    {\"stepId\": 4, \"stepName\": \"Step 4 Name\", \"details\": \"Details for Step 4\"},\n" +
            "    {\"stepId\": 5, \"stepName\": \"Step 5 Name\", \"details\": \"Details for Step 5\"}\n" +
            "  ]\n" +
            "}";

    // Method to dynamically load steps from the hardcoded JSON data
    public static List<Protocol_step_admin> loadStepsFromJson() {
        List<Protocol_step_admin> steps = new ArrayList<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonData);
            JsonNode stepsNode = rootNode.path("steps");
            for (JsonNode node : stepsNode) {
                Protocol_step_admin step = new Protocol_step_admin();
                step.setStepId(node.get("stepId").asInt());
                step.setStepName(node.get("stepName").asText());
                step.setDetails(node.get("details").asText());
                steps.add(step);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return steps;
    }

    // Method to find a step by ID, assuming steps are loaded into this list
    public static Protocol_step_admin findById(int stepId, List<Protocol_step_admin> steps) {
        for (Protocol_step_admin step : steps) {
            if (step.getStepId() == stepId) {
                return step;
            }
        }
        return null;
    }
}
