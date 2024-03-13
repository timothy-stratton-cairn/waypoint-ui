package com.cairn.ui.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.cairn.ui.model.ProtocolStepTemplate;
import com.cairn.ui.model.ProtocolTemplate;
import com.cairn.ui.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ProtocolTemplateHelper {
	private final RestTemplate restTemplate = new RestTemplate();

	/**
	 * Get a list of step templates not assigned to the protocol template.
	 * 
	 * @param usr a User object that is the logged in user. The token from the instance is used to 
	 * authenticate the API call
	 * @param theTemplate A protocol template instance we want to check.
	 * @param type The type of steps to return.
	 * @return ArrayList of protocol steps that we could assign to the protocol.
	 */
	public ArrayList<ProtocolStepTemplate> availableSteps(User usr,ProtocolTemplate theTemplate, int type) {
		ArrayList<ProtocolStepTemplate> results = new ArrayList<ProtocolStepTemplate>();
		return results;
	}

	public Map<String,String> getStepTypes() {
        HashMap<String, String> retVal = new HashMap<String, String>();
        retVal.put("0", "-- Select A Value --");
        retVal.put("1", "Gather Data");
        retVal.put("2", "Run Analysis");
        retVal.put("3", "Craft Recommendation");
        retVal.put("4", "Share Education");

        return retVal;
	}

	/**
	 * Save a protocol template
	 * 
	 * @param usr a User object that is the logged in user. The token from the instance is used to 
	 * authenticate the API call
	 * @param theTemplate A protocol template instance we want to save (create or update).
	 * @return int. Return code is the protocol id on successful save or less than 1 for an error.
	 */
	public int saveProtocolTemplate(User usr,ProtocolTemplate theTemplate) {
		int result = 0;
		
		return result;
	}

	/**
	 * Save a protocol template
	 * 
	 * @param usr a User object that is the logged in user. The token from the instance is used to 
	 * authenticate the API call
	 * @param theStep A protocol template step instance we want to save (create or update).
	 * @return int. Return code is the step id on successful save or less than 1 for an error.
	 */
	public int saveTemplateStep(User usr,ProtocolStepTemplate theStep) {
		int result = 0;
		
		return result;
	}

	/**
	 * Save a protocol template step to a protocol template.
	 * 
	 * @param usr a User object that is the logged in user. The token from the instance is used to 
	 * authenticate the API call
	 * @param theTemplate A protocol template instance we want to add the step to.
	 * @param theStep A protocol template step instance we want to add to the template.
	 * @return int. Return code is 1 on successful addition or less than 1 for an error. 0 if the step is already assigned to the template.
	 */
	public int addTemplateStep(User usr,ProtocolTemplate theTemplate, ProtocolStepTemplate theStep) {
		int result = 0;
		
		return result;
	}

	/**
	 * Remove a protocol template step from a protocol template.
	 * 
	 * @param usr a User object that is the logged in user. The token from the instance is used to 
	 * authenticate the API call
	 * @param theTemplate A protocol template instance we want to remove the step from.
	 * @param theStep A protocol template step instance we want to remove from the template.
	 * @return int. Return code is 1 on successful remove or less than 1 for an error. 0 if the step was not assigned to the template.
	 */
	public int removeTemplateStep(User usr,ProtocolTemplate theTemplate, ProtocolStepTemplate theStep) {
		int result = 0;
		
		return result;
	}

	/**
	 * Get a list of protocol templates
	 * 
	 * @param usr a User object that is the logged in user. The token from the instance is used to 
	 * authenticate the API call
	 * @return ArrayList of ProtocolTemplates. Empty list on error.
	 */
	public ArrayList<ProtocolTemplate> getList(User usr) {
		ArrayList<ProtocolTemplate> results = new ArrayList<ProtocolTemplate>();
		
		if (usr == null) {
			return results;
		}

		// Prepare the request body
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + usr.getToken());

		// Create a HttpEntity with the headers
		HttpEntity<String> entity = new HttpEntity<>(headers);

		String apiUrl = "http://96.61.158.12:8083/api/protocol-template";

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
					JsonNode prots = jsonNode.get("protocolTemplates");
					// Iterate through the array elements
					ProtocolTemplate entry = null;
					if (prots.isArray()) {
						for (JsonNode element : prots) {
							// Access and print array elements
							if (element != null) {
								entry = new ProtocolTemplate();
								entry.setName(element.get("name").asText());
								entry.setId(Integer.valueOf(element.get("id").toString()));
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
			System.out.println("No protocols returned");
		}

		return results;

	}

	/**
	 * Get a list of protocol template steps
	 * 
	 * @param usr a User object that is the logged in user. The token from the instance is used to 
	 * authenticate the API call.
	 * @param id The id of the protocol template.
	 * @return ArrayList of ProtocolStepTemplates. Empty list on error.
	 */
	public ArrayList<ProtocolStepTemplate> getStepList(User usr, int id) {
		ArrayList<ProtocolStepTemplate> results = new ArrayList<ProtocolStepTemplate>();

		// Prepare the request body
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + usr.getToken());

		// Create a HttpEntity with the headers
		HttpEntity<String> entity = new HttpEntity<>(headers);

		String apiUrl = "http://96.61.158.12:8083/api/protocol-step-template";

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
					JsonNode prots = jsonNode.get("stepTemplates");
					// Iterate through the array elements
					ProtocolStepTemplate entry = null;
					if (prots.isArray()) {
						for (JsonNode element : prots) {
							// Access and print array elements
							if (element != null) {
								entry = new ProtocolStepTemplate();
								entry.setName(element.get("name").asText());
								entry.setId(Integer.valueOf(element.get("id").toString()));
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
			System.out.println("No protocols returned");
		}

		return results;
	}
		
	/**
	 * Get a specific protocol template.
	 * 
	 * @param usr a User object that is the logged in user. The token from the instance is used to 
	 * authenticate the API call.
	 * @param id The id of the protocol template.
	 * @return ProtocolTemplate instance. New/Blank is returned if an error occurs.
	 */
	public ProtocolTemplate getTemplate(User usr, int id) {
		ProtocolTemplate result = new ProtocolTemplate();
		// Prepare the request body
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + usr.getToken());

		// Create a HttpEntity with the headers
		HttpEntity<String> entity = new HttpEntity<>(headers);

		String apiUrl = "http://96.61.158.12:8083/api/protocol-template/" + id;

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
					result.setName(jsonNode.get("name").asText());
					result.setDescription(jsonNode.get("description").asText());
					result.setId(Integer.valueOf(jsonNode.get("id").toString()));
				} catch (JsonMappingException e) {
					e.printStackTrace();
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("Failed to fetch data. Status code: " + response.getStatusCode());
			}
		} catch (Exception e) {
			System.out.println("No protocols returned");
		}

		return result;
	}

}
