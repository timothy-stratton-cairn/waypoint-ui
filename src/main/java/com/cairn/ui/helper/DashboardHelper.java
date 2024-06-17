package com.cairn.ui.helper;

import com.cairn.ui.model.AssignedUsers;
import com.cairn.ui.model.Entity;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.cairn.ui.Constants;
import com.cairn.ui.model.ProtocolStats;
import com.cairn.ui.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class DashboardHelper {
	@Value("${waypoint.dashboard-api.base-url}")
	private String dashboardApiBaseUrl;

	private final RestTemplate restTemplate = new RestTemplate();
	private final ObjectMapper mapper = new ObjectMapper();

	/**
	 * Get the dashboard stats
	 * 
	 * @return
	 */
    public ArrayList<ProtocolStats> getDashboard(User usr) {
        ArrayList<ProtocolStats> results = new ArrayList<>();

        // Prepare the request body
        String apiUrl = this.dashboardApiBaseUrl + Constants.api_dashboard_get;
        System.out.println(apiUrl);
        HttpEntity<String> entity = Entity.getEntity(usr, apiUrl);

        // Make the GET request and retrieve the response
        try {
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, String.class);
            // Process the response
            if (response.getStatusCode().is2xxSuccessful()) {
                String jsonResponse = response.getBody();
                ObjectMapper objectMapper = new ObjectMapper();

                JsonNode jsonNode;
                try {
                    jsonNode = objectMapper.readTree(jsonResponse);
                    JsonNode prots = jsonNode.get("protocols");
                    // Iterate through the array elements
                    if (prots != null && prots.isArray()) {
                        for (JsonNode element : prots) {
                            ProtocolStats entry = new ProtocolStats();
                            
                            JsonNode protocolTemplateIdNode = element.get("protocolTemplateId");
                            entry.setTemplateId(protocolTemplateIdNode != null ? protocolTemplateIdNode.asInt() : null);

                            JsonNode assignedUsersNode = element.get("assignedUsers");
                            entry.setAssignedUsers(assignedUsersNode != null ? mapper.readValue(assignedUsersNode.asText(), AssignedUsers.class) : null);

                            JsonNode numStepsTodoNode = element.get("numStepsTodo");
                            entry.setNumSteps(numStepsTodoNode != null ? numStepsTodoNode.asInt() : null);

                            JsonNode numStepsInProgressNode = element.get("numStepsInProgress");
                            entry.setProgress(numStepsInProgressNode != null ? numStepsInProgressNode.asInt() : null);

                            JsonNode numStepsDoneNode = element.get("numStepsDone");
                            entry.setDone(numStepsDoneNode != null ? numStepsDoneNode.asInt() : null);

                            JsonNode completionPercentageNode = element.get("completionPercentage");
                            entry.setCompletion(completionPercentageNode != null ? String.valueOf(Float.parseFloat(completionPercentageNode.asText()) * 100) : null);

                            JsonNode protocolTemplateNameNode = element.get("protocolTemplateName");
                            entry.setTemplateName(protocolTemplateNameNode != null ? protocolTemplateNameNode.asText() : null);

                            results.add(entry);
                        }
                    }
                } catch (JsonMappingException e) {
                    e.printStackTrace();
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Failed to fetch data. Status code: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("No dashboard data returned");
        }

        return results;
    }
}
