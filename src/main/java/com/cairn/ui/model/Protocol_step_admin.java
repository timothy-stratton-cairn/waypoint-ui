package com.cairn.ui.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;

public class Protocol_step_admin {
    private int stepId;
    private String stepName;
    private String details;
	private int type;

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
	public int getType() {
		/* 4 types of steps *
		  0: Undefined
		  1: Gather Data
		  2: Run Analysis
		  3: Craft Recommendation
		  4: Share Education
		   */
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}

    public void setDetails(String details) {
        this.details = details;
    }

    // Sample JSON data for demonstration
    private static final String jsonData = "{\n" +
            "  \"steps\": [\n" +
            "    {\"stepId\": 1, \"stepName\": \"Step 1 Gather Data\",\"type\": 1 ,\"details\": \"Details for Step 1\"},\n" +
            "    {\"stepId\": 2, \"stepName\": \"Step 2 Run Analysis \",\"type\": 2 ,\"details\": \"Details for Step 2\"},\n" +
            "    {\"stepId\": 3, \"stepName\": \"Step 3 Craft Recommendation\",\"type\":3 ,\"details\": \"Details for Step 3\"},\n" +
            "    {\"stepId\": 4, \"stepName\": \"Step 4 Share Education\",\"type\": 4 ,\"details\": \"Details for Step 4\"}\n" +
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
                step.setType(node.get("type").asInt());
                steps.add(step);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return steps;
    }

    // Method to find a step by ID
    public static Protocol_step_admin findById(int stepId, List<Protocol_step_admin> steps) {
        for (Protocol_step_admin step : steps) {
            if (step.getStepId() == stepId) {
                return step;
            }
        }
        return null;
    }
}
