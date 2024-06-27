package com.cairn.ui.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.cairn.ui.model.Entity;
import com.cairn.ui.model.User;

@Service
public class APIHelper {
    Logger logger = LoggerFactory.getLogger(APIHelper.class); 

	private RestTemplate restTemplate;

	private RestTemplate getRestTemplate() {
		if (this.restTemplate == null) {
			// Using HttpComponentsClientHttpRequestFactory to support PATCH
			HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
			requestFactory.setConnectTimeout(5000); // Optional: set connection timeout
			this.restTemplate = new RestTemplate(requestFactory);
		}
		return this.restTemplate;
	}

	public String callAPI(String apiUrl, User usr) {
		String jsonResponse = "";
		HttpEntity<String> entity = Entity.getEntity(usr, apiUrl);
		// Make the GET request and retrieve the response
		try {
			ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.GET, entity, String.class);
			// Process the response
			if (response.getStatusCode().is2xxSuccessful()) {
				jsonResponse = response.getBody();
			} else {
				logger.info("Failed to fetch data " + apiUrl + ". Status code: " + response.getStatusCode());
			}
		} catch (Exception e) {
			logger.info("No records returned for " + apiUrl);
		}
		return jsonResponse;
	}

	public int postAPI(String apiUrl, String requestBody, User usr) {
		int result = 0;
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + usr.getToken());
		headers.add("Content-Type", "application/json");
		HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
		try {
			ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.POST, entity, String.class);
			if (response.getStatusCode().is2xxSuccessful()) {
				result = 1;
			} else {
				result = -1;
				logger.warn(apiUrl + "==>Failed to fetch data. Status code: " + response.getStatusCode());
			}

		} catch (Exception e) {
			logger.info("Error in updating note");
			e.printStackTrace();
		}
		return result;
	}

	public int deleteAPI(String apiUrl, User usr) {
		int result = 0;
        HttpEntity<String> entity = Entity.getEntity(usr, apiUrl);
        try {
			ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.DELETE, entity, String.class);
			if (response.getStatusCode().is2xxSuccessful()) {
			result = 1;
			}
        }
	    catch (Exception e) {
	        logger.error("Error in deleting " + apiUrl);
	        e.printStackTrace();
	    }
        
		return result;
	}

	public int patchAPI(String apiUrl, String requestBody, User usr) {
		int result = 0;
		HttpEntity<String> entity = Entity.getEntityWithBody(usr, apiUrl, requestBody);
		try {
			ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.PATCH, entity,
					String.class);
			if (response.getStatusCode().is2xxSuccessful()) {
				result = 1;
			} else {
				result = -1;
				logger.info(apiUrl + "==>Failed to fetch data. Status code: " + response.getStatusCode());
				// Update result to indicate a specific type of failure
			}

		} catch (Exception e) {
			logger.info("Error in updating progress");
			e.printStackTrace();
		}
		return result;
	}

	public String postAPIResponse(String apiUrl, String requestBody, User usr) {
		String result = "";
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + usr.getToken());
		headers.add("Content-Type", "application/json");
		HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
		try {
			ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.POST, entity, String.class);
			if (response.getStatusCode().is2xxSuccessful()) {
				result = response.getBody();
			} else {
				result = "";
				logger.warn(apiUrl + "==>Failed to fetch data. Status code: " + response.getStatusCode());
			}
	
		} catch (Exception e) {
			logger.info("Error in updating note");
			e.printStackTrace();
		}
		return result;
	}
}
