package com.cairn.ui.helper;

import java.util.ArrayList;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.cairn.ui.model.Protocol;
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
	 * Get a list of available protocols
	 * 
	 * @return
	 */
	public ArrayList<ProtocolTemplate> getList(User usr) {
		ArrayList<ProtocolTemplate> results = new ArrayList<ProtocolTemplate>();

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
