package com.cairn.ui.helper;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.cairn.ui.Constants;
import com.cairn.ui.model.AssignedUsers;
import com.cairn.ui.model.Entity;
import com.cairn.ui.model.Household;
import com.cairn.ui.model.ProtocolStats;
import com.cairn.ui.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class DashboardHelper {
    Logger logger = LoggerFactory.getLogger(DashboardHelper.class); 
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
	    logger.info(apiUrl);
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
	                        entry.setTemplateId(protocolTemplateIdNode != null && !protocolTemplateIdNode.isNull() ? protocolTemplateIdNode.asInt() : null);

	                        JsonNode assignedUsersNode = element.get("assignedUsers");
	                        entry.setAssignedUsers(assignedUsersNode != null && !assignedUsersNode.isNull() ? objectMapper.readValue(assignedUsersNode.toString(), AssignedUsers.class) : null);

	                        JsonNode numStepsTodoNode = element.get("numStepsTodo");
	                        entry.setNumSteps(numStepsTodoNode != null && !numStepsTodoNode.isNull() ? numStepsTodoNode.asInt() : null);

	                        JsonNode numStepsInProgressNode = element.get("numStepsInProgress");
	                        entry.setProgress(numStepsInProgressNode != null && !numStepsInProgressNode.isNull() ? numStepsInProgressNode.asInt() : null);

	                        JsonNode numStepsDoneNode = element.get("numStepsDone");
	                        entry.setDone(numStepsDoneNode != null && !numStepsDoneNode.isNull() ? numStepsDoneNode.asInt() : null);

	                        JsonNode completionPercentageNode = element.get("completionPercentage");
	                        entry.setCompletion(completionPercentageNode != null && !completionPercentageNode.isNull() ? String.valueOf(Float.parseFloat(completionPercentageNode.asText()) * 100) : null);

	                        JsonNode protocolTemplateNameNode = element.get("protocolTemplateName");
	                        entry.setTemplateName(protocolTemplateNameNode != null && !protocolTemplateNameNode.isNull() ? protocolTemplateNameNode.asText() : null);

	                        // Parse assignedHouseholds
	                        JsonNode assignedHouseholdsNode = element.get("assignedHousehold");
	                        if (assignedHouseholdsNode != null && !assignedHouseholdsNode.isNull()) {
	                            ArrayList<Household> households = new ArrayList<>();
	                            JsonNode householdsArray = assignedHouseholdsNode.get("households");
	                            if (householdsArray != null && householdsArray.isArray()) {
	                                for (JsonNode householdNode : householdsArray) {
	                                   Household household = new Household();

	                                    household.setId(householdNode.get("id") != null && !householdNode.get("id").isNull() ? householdNode.get("id").asInt() : null);
	                                    household.setName(householdNode.get("name") != null && !householdNode.get("name").isNull() ? householdNode.get("name").asText() : null);
	                                    household.setDescription(householdNode.get("description") != null && !householdNode.get("description").isNull() ? householdNode.get("description").asText() : null);

	                                    JsonNode householdAccountsNode = householdNode.get("householdAccounts");
	                                    if (householdAccountsNode != null && !householdAccountsNode.isNull()) {
	                                        JsonNode accountsArray = householdAccountsNode.get("accounts");
	                                        if (accountsArray != null && accountsArray.isArray()) {
	                                            for (JsonNode accountNode : accountsArray) {
	                                                User user = new User();
	                                                user.setId(accountNode.get("id") != null && !accountNode.get("id").isNull() ? accountNode.get("id").asInt() : null);
	                                                user.setFirstName(accountNode.get("firstName") != null && !accountNode.get("firstName").isNull() ? accountNode.get("firstName").asText() : null);
	                                                user.setLastName(accountNode.get("lastName") != null && !accountNode.get("lastName").isNull() ? accountNode.get("lastName").asText() : null);
	                                                user.setRole(accountNode.get("role") != null && !accountNode.get("role").isNull() ? accountNode.get("role").asText() : null);

	                                                household.getHouseholdAccounts().add(user);
	                                            }
	                                        }
	                                    }

	                                    households.add(household);
	                                }
	                            }
	                            entry.setAssignedHouseholds(households);
	                        }

	                        results.add(entry);
	                    }
	                }
	            } catch (JsonMappingException e) {
	                logger.error("Error mapping JSON", e);
	            } catch (JsonProcessingException e) {
	                logger.error("Error processing JSON", e);
	            }
	        } else {
	            logger.info("Failed to fetch data. Status code: " + response.getStatusCode());
	        }
	    } catch (Exception e) {
	        logger.error(e.getMessage());
	        logger.error("No dashboard data returned");
	    }

	    return results;
	}


}
