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
public class ReportHelper {
	@Value("${waypoint.dashboard-api.base-url}")
	private String dashboardApiBaseUrl;

	private final RestTemplate restTemplate = new RestTemplate();
	private final ObjectMapper mapper = new ObjectMapper();

	/**
	 * Get the report data for general stats
	 * 
	 * @return
	 */
	public ArrayList<ReportStat> getStatsReport(User usr) {
		ArrayList<ReportStat> results = new ArrayList<ReportStat>();

		// Get protocol numbers assigned to clients.

		String apiUrl = this.dashboardApiBaseUrl + Constants.api_dashboard_get;
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
					ProtocolStats entry = null;
					if (prots.isArray()) {
						for (JsonNode element : prots) {
							// Access and print array elements
							if (element != null) {
								entry = new ProtocolStats();
								entry.setTemplateId(Integer.valueOf(element.get("protocolTemplateId").toString()));
								entry.setAssignedUsers(mapper.readValue(element.get("assignedUsers").toString(), AssignedUsers.class));
								entry.setNumSteps(Integer.valueOf(element.get("numStepsTodo").toString()));
								entry.setProgress(Integer.valueOf(element.get("numStepsInProgress").toString()));
								entry.setDone(Integer.valueOf(element.get("numStepsDone").toString()));
								entry.setCompletion(String.valueOf(Float.parseFloat(element.get("completionPercentage").asText()) * 100));
								entry.setTemplateName(element.get("protocolTemplateName").asText());

								results.add(entry);
							}
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
			System.out.println("No dashboard data returned");
		}

		return results;

	}
}
