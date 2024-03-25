package com.cairn.ui.helper;

import java.util.ArrayList;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.cairn.ui.Constants;
import com.cairn.ui.model.Protocol;
import com.cairn.ui.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ProtocolHelper {
    
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
    

	/**
	 * Get a list of available protocols
	 * 
	 * @return
	 */
	public ArrayList<Protocol> getList(User usr) {
		ArrayList<Protocol> results = new ArrayList<Protocol>();

		// Prepare the request body
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + usr.getToken());

		// Create a HttpEntity with the headers
		HttpEntity<String> entity = new HttpEntity<>(headers);

		String apiUrl = Constants.api_server + Constants.api_ep_protocol;

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
					JsonNode prots = jsonNode.get("protocols");
					// Iterate through the array elements
					Protocol entry = null;
					if (prots.isArray()) {
						for (JsonNode element : prots) {
							// Access and print array elements
							if (element != null) {
								entry = new Protocol();
								entry.setName(element.get("name").toString());
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

	static public ArrayList<Protocol> getListDemo() {
		ArrayList<Protocol> results = new ArrayList<Protocol>();

		String data = "{\n" + "    \"protocols\": [\n" + "        {\n" + "            \"id\": 1,\n"
				+ "            \"name\": \"New Client\"\n" + "        },\n" + "        {\n" + "            \"id\": 2,\n"
				+ "            \"name\": \"House Purchase\"\n" + "        },\n" + "        {\n"
				+ "            \"id\": 3,\n" + "            \"name\": \"Job Change/Retirement\"\n" + "        },\n"
				+ "        {\n" + "            \"id\": 4,\n" + "            \"name\": \"Marriage\"\n" + "        },\n"
				+ "        {\n" + "            \"id\": 5,\n" + "            \"name\": \"Baby\"\n" + "        }\n"
				+ "    ],\n" + "    \"numOfProtocols\": 5\n" + "}";

		try {
			ObjectMapper objectMapper = new ObjectMapper();

			JsonNode jsonNode = objectMapper.readTree(data);
			JsonNode prots = jsonNode.get("protocols");
			// Iterate through the array elements
			Protocol entry = null;
			if (prots.isArray()) {
				for (JsonNode element : prots) {
					// Access and print array elements
					if (element != null) {
						entry = new Protocol();
						entry.setName(element.get("name").asText());
						entry.setId(Integer.valueOf(element.get("id").toString()));
						results.add(entry);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return results;
	}

}
