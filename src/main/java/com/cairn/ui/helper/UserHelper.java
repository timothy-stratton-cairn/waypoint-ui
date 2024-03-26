package com.cairn.ui.helper;

import java.util.ArrayList;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.cairn.ui.Constants;
import com.cairn.ui.model.ProtocolStats;
import com.cairn.ui.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UserHelper {
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

	/**
	 * Get the dashboard stats
	 * 
	 * @return
	 */
	public ArrayList<User> getUserList(User usr) {
		ArrayList<User> results = new ArrayList<User>();

		// Prepare the request body
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + usr.getToken());

		// Create a HttpEntity with the headers
		HttpEntity<String> entity = new HttpEntity<>(headers);

		String apiUrl = Constants.auth_server + Constants.api_userlist_get;

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
					JsonNode prots = jsonNode.get("accounts");
					// Iterate through the array elements
					User entry = null;
					if (prots.isArray()) {
						for (JsonNode element : prots) {
							// Access and print array elements
							if (element != null) {
								entry = new User();
								entry.setId(Integer.valueOf(element.get("id").toString()));
								entry.setFirstName(element.get("firstName").asText());
								entry.setLastName(element.get("lastName").asText());
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
			System.out.println("No user data returned");
			e.printStackTrace();
		}

		return results;

	}
	
	/**
	 * Get the user details
	 * 
	 * @return
	 */
	public User getUser(User usr, int uid) {
		User result = new User();

		// Prepare the request body
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + usr.getToken());

		// Create a HttpEntity with the headers
		HttpEntity<String> entity = new HttpEntity<>(headers);

		String apiUrl = Constants.auth_server + Constants.api_userlist_get + "/" + uid;

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
					result = new User();
					result.setId(Integer.valueOf(jsonNode.get("id").toString()));
					result.setFirstName(jsonNode.get("firstName").asText());
					result.setLastName(jsonNode.get("lastName").asText());
					result.setUsername(jsonNode.get("username").asText());
					result.setEmail(jsonNode.get("email").asText());
					JsonNode roles = jsonNode.get("roles");
					JsonNode deps = jsonNode.get("dependents");
					JsonNode coclient = jsonNode.get("coClient");
					User temp = new User();
					if (coclient != null) {
						temp = new User();
						temp.setId(Integer.valueOf(coclient.get("id").toString()));
						temp.setFirstName(coclient.get("firstName").asText());
						temp.setLastName(coclient.get("lastName").asText());
						temp.setUsername(coclient.get("username").asText());
						result.setCoclient(temp);
					}
					// Iterate through the array elements
					ArrayList<String> userRoles = new ArrayList<String>(); 
					if (roles.isArray()) {
						for (JsonNode element : roles) {
							// Access and print array elements
							if (element != null) {
								userRoles.add(element.get("lastName").asText());
							}
						}
					}
					result.setRoles(userRoles);
					ArrayList<User> userDeps = new ArrayList<User>(); 
					if (deps.isArray()) {
						for (JsonNode element : roles) {
							// Access and print array elements
							if (element != null) {
								temp = new User();
								temp.setId(Integer.valueOf(element.get("id").toString()));
								temp.setFirstName(element.get("firstName").asText());
								temp.setLastName(element.get("lastName").asText());
								temp.setUsername(element.get("username").asText());
								userDeps.add(temp);
							}
						}
					}
					result.setDependents(userDeps);
				} catch (JsonMappingException e) {
					e.printStackTrace();
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("Failed to fetch data. Status code: " + response.getStatusCode());
			}
		} catch (Exception e) {
			System.out.println("No user data returned");
			e.printStackTrace();
		}

		return result;

	}
}
