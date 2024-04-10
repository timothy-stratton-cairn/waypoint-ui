package com.cairn.ui.helper;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.cairn.ui.Constants;
import com.cairn.ui.model.HomeworkTemplate;
import com.cairn.ui.model.ProtocolStepTemplate;
import com.cairn.ui.model.HomeworkTemplate;
import com.cairn.ui.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class HomeworkTemplateHelper{

	@Value("${waypoint.dashboard-api.base-url}")
	private String dashboardApiBaseUrl;

    private RestTemplate restTemplate;


    private RestTemplate getRestTemplate() {
        if (this.restTemplate == null) {
            // Using HttpComponentsClientHttpRequestFactory to support PATCH
            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setConnectTimeout(5000); 
            this.restTemplate = new RestTemplate(requestFactory);
        }
        return this.restTemplate;
    }
    
    
    
    public ArrayList<HomeworkTemplate> getList(User usr) {
		ArrayList<HomeworkTemplate> results = new ArrayList<HomeworkTemplate>();
		
		if (usr == null) {
			return results;
		}

		// Prepare the request body
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + usr.getToken());

		// Create a HttpEntity with the headers
		HttpEntity<String> entity = new HttpEntity<>(headers);

		String apiUrl = this.dashboardApiBaseUrl + Constants.api_homeworktemplate;

		// Make the GET request and retrieve the response
		try {
			ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.GET, entity, String.class);
			// Process the response
			if (response.getStatusCode().is2xxSuccessful()) {
				String jsonResponse = response.getBody();
				ObjectMapper objectMapper = new ObjectMapper();

				JsonNode jsonNode;
				try {
					jsonNode = objectMapper.readTree(jsonResponse);
					JsonNode hwork = jsonNode.get("homeworkTemplates");
					// Iterate through the array elements
					HomeworkTemplate entry = null;
					if (hwork.isArray()) {
						for (JsonNode element : hwork) {
							// Access and print array elements
							if (element != null) {
								entry = new HomeworkTemplate();
								entry.setName(element.get("name").asText());
								entry.setId(Integer.valueOf(element.get("id").toString()));
								if (element.has("description") && !element.get("description").isNull()) {
	                                entry.setDescription(element.get("description").asText());
	                            } else {
	                                entry.setDescription("No Description Given");
	                            }
								entry.getNumClients();
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
			System.out.println("No Homeworks returned");
		}

		return results;

	}

	public HomeworkTemplate getTemplate(User usr, int id) {
		HomeworkTemplate result = new HomeworkTemplate();
		// Prepare the request body
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + usr.getToken());

		// Create a HttpEntity with the headers
		HttpEntity<String> entity = new HttpEntity<>(headers);

		String apiUrl = this.dashboardApiBaseUrl + Constants.api_homeworktemplate+ "/" + id;

		// Make the GET request and retrieve the response
		try {
			ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.GET, entity, String.class);
			// Process the response
			if (response.getStatusCode().is2xxSuccessful()) {
				String jsonResponse = response.getBody();
				ObjectMapper objectMapper = new ObjectMapper();

				JsonNode jsonNode;
				try {
					jsonNode = objectMapper.readTree(jsonResponse);
					result.setName(jsonNode.get("name").asText());
					result.setId(Integer.valueOf(jsonNode.get("id").toString()));
					result.setDescription(jsonNode.get("description").asText());

				} catch (JsonMappingException e) {
					e.printStackTrace();
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("Failed to fetch data. Status code: " + response.getStatusCode());
			}
		} catch (Exception e) {
			System.out.println("No Homeworks returned");
		}

		return result;
	}
}