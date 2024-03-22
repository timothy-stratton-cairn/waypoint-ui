package com.cairn.ui.helper;

import java.util.ArrayList;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.cairn.ui.Constants;
import com.cairn.ui.model.ProtocolStats;
import com.cairn.ui.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DashboardHelper {
	private final RestTemplate restTemplate = new RestTemplate();

	/**
	 * Get the dashboard stats
	 * 
	 * @return
	 */
	public ArrayList<ProtocolStats> getDashboard(User usr) {
		ArrayList<ProtocolStats> results = new ArrayList<ProtocolStats>();

		// Prepare the request body
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + usr.getToken());

		// Create a HttpEntity with the headers
		HttpEntity<String> entity = new HttpEntity<>(headers);

		String apiUrl = Constants.api_server + Constants.api_dashboard_get;

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
								entry.setAssignedUsers(Integer.valueOf(element.get("numAssignedUsers").toString()));
								entry.setNumSteps(Integer.valueOf(element.get("numStepsTodo").toString()));
								entry.setProgress(Integer.valueOf(element.get("numStepsInProgress").toString()));
								entry.setDone(Integer.valueOf(element.get("numStepsDone").toString()));
								entry.setCompletion(element.get("completionPercentage").asText());
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
