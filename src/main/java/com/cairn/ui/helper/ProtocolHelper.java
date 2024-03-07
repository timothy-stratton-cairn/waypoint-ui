package com.cairn.ui.helper;

import java.util.ArrayList;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.cairn.ui.model.Protocol;
import com.cairn.ui.model.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ProtocolHelper {
	private final RestTemplate restTemplate = new RestTemplate();
	
	/**
	 * Get a list of available protocols
	 * 
	 * @return
	 */
	public ArrayList<Protocol> getList(User usr) {
		ArrayList<Protocol> results = new ArrayList<Protocol>();
		
		// Prepare the request body
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/json");
		headers.add("Authorization", "Bearer " + usr.getToken());
		String requestBody = "";
		HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

		ResponseEntity<String> response = restTemplate.exchange("http://96.61.158.12:8082/api/protocol", HttpMethod.GET,
				requestEntity, String.class, requestBody);

		// Check if the response is successful (e.g., status code 200)
		if (response.getStatusCode().is2xxSuccessful()) {
			try {
				ObjectMapper objectMapper = new ObjectMapper();

				JsonNode jsonNode = objectMapper.readTree(response.getBody());
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
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return results;

	}

	static public ArrayList<Protocol> getListDemo() {
		ArrayList<Protocol> results = new ArrayList<Protocol>();
		
		String data = "{\n"
				+ "    \"protocols\": [\n"
				+ "        {\n"
				+ "            \"id\": 1,\n"
				+ "            \"name\": \"New Client\"\n"
				+ "        },\n"
				+ "        {\n"
				+ "            \"id\": 2,\n"
				+ "            \"name\": \"House Purchase\"\n"
				+ "        },\n"
				+ "        {\n"
				+ "            \"id\": 3,\n"
				+ "            \"name\": \"Job Change/Retirement\"\n"
				+ "        },\n"
				+ "        {\n"
				+ "            \"id\": 4,\n"
				+ "            \"name\": \"Marriage\"\n"
				+ "        },\n"
				+ "        {\n"
				+ "            \"id\": 5,\n"
				+ "            \"name\": \"Baby\"\n"
				+ "        }\n"
				+ "    ],\n"
				+ "    \"numOfProtocols\": 5\n"
				+ "}";
		
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
