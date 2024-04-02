package com.cairn.ui.helper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.cairn.ui.Constants;
import com.cairn.ui.model.Protocol;
import com.cairn.ui.model.ProtocolStep;
import com.cairn.ui.model.ProtocolStepTemplate;
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

	/**
	 * Get a specific protocol.
	 * 
	 * @param usr a User object that is the logged in user. The token from the instance is used to 
	 * authenticate the API call.
	 * @param id The id of the protocol.
	 * @return Protocol instance. New/Blank is returned if an error occurs.
	 */
	public Protocol getProtocol(User usr, int id) {
		Protocol result = new Protocol();
		// Prepare the request body
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + usr.getToken());

		// Create a HttpEntity with the headers
		HttpEntity<String> entity = new HttpEntity<>(headers);

		String apiUrl = Constants.api_server + Constants.api_ep_protocol + "/" + id;

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
					result.setDescription(jsonNode.get("description").asText());
					result.setCompletionPercent(jsonNode.get("completionPercentage").asText());
					JsonNode comments = jsonNode.get("protocolComments").get("comments");
					String commentString = "";
					if (comments.isArray()) {
						for (JsonNode element : comments) {
							// Access and print array elements
							if (element != null) {
								commentString += element.get("comment").asText();
							}
						}
					}
					result.setComment(commentString);
					result.setId(Integer.valueOf(jsonNode.get("id").toString()));
					result.setNeedsAttention(Boolean.valueOf(jsonNode.get("needsAttention").toString()));
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(sdf.parse(jsonNode.get("lastStatusUpdateTimestamp").asText()));
					result.setLastStatus(calendar.getTime());
					
					/* Add in the steps */
					JsonNode assoc = jsonNode.get("associatedSteps");
					JsonNode asteps = assoc.get("steps");
					// Iterate through the array elements
					ArrayList<ProtocolStep> steps = new ArrayList<ProtocolStep>();
					if (asteps.isArray()) {
						for (JsonNode element : asteps) {
							// Access and print array elements
							if (element != null) {
								ProtocolStep curStep = new ProtocolStep();
								curStep.setId(Integer.parseInt(element.get("id").asText()));
								curStep.setName(element.get("name").asText());
								curStep.setDescription(element.get("description").asText());
								curStep.setNotes(element.get("stepNotes").get("notes").asText());
								curStep.setCategory(element.get("category").asText());
								curStep.setStatus(element.get("status").asText());
								steps.add(curStep);
							}
						}
						result.setSteps(steps);
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
			e.printStackTrace();
			System.out.println("No protocols returned");
		}

		return result;
	}
	
	public ArrayList<ProtocolStep> getStepList(User usr, int id) {
		ArrayList<ProtocolStep> results = new ArrayList<ProtocolStep>();

		// Prepare the request body
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + usr.getToken());

		// Create a HttpEntity with the headers
		HttpEntity<String> entity = new HttpEntity<>(headers);

		String apiUrl = Constants.api_server + Constants.api_ep_protocol + '/' + id;

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
					JsonNode temp = jsonNode.get("associatedSteps");
					JsonNode prots = temp.get("steps");
					// Iterate through the array elements
					ProtocolStep entry = null;
					if (prots.isArray()) {
						int idx = 1;
						for (JsonNode element : prots) {
							// Access and print array elements
							if (element != null) {
								entry = new ProtocolStep();
								entry.setDescription(element.get("description").asText());
								entry.setName(element.get("name").asText());
								entry.setId(Integer.valueOf(element.get("id").toString()));
								// Test data, fix this later
								entry.setCategory(element.get("category").asText());
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
			e.printStackTrace();
		}

		return results;
	}
	/**
	 * Provides list of Protocols that have been assigned clientId to their user array
	 * @param usr
	 * @param clientId
	 * @return
	 */
	public ArrayList<Protocol> getAssignedProtocols(User usr, int clientId){
		ArrayList<Protocol> results = new ArrayList<Protocol>();
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + usr.getToken());

		// Create a HttpEntity with the headers
		HttpEntity<String> entity = new HttpEntity<>(headers);

		String apiUrl = Constants.api_server + Constants.api_ep_protocolaccount + clientId;//retrieves all protocols assigned to clientId
		
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
	/*
	 * Assigns a clientId to a protocol
	 * @param usr
	 * @param pcol
	 */
	
	public int addClient(User usr, int clientid, int protocolTemplateId) {
		int result = 0;
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Authorization", "Bearer " + usr.getToken());

		headers.add("Content-Type", "application/json");
		// Create a HttpEntity with the headers
		String requestBody = "{\"protocolTemplateId\": " + protocolTemplateId + ", \"comment\": \"\", \"associatedAccountId\": " + clientid + "}";
		HttpEntity<String> entity = new HttpEntity<>(requestBody, headers); // assign  as part of the request body 
		String apiUrl = Constants.api_server + Constants.api_ep_protocol;//retrieves all protocols assigned to clientId
	    try {
	        ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.POST, entity, String.class);
	        if (response.getStatusCode().is2xxSuccessful()) {

	            result = 1;
	        } else {
	            System.out.println("Failed to fetch data. Status code: " + response.getStatusCode());
	            // Update result to indicate a specific type of failure
	            result = -1;
	        }
	    } catch (Exception e) {
	        System.out.println("Step not found or error in assigning step");
	        e.printStackTrace();
	        // Keep result as -1 or set to another specific value indicating error
	    }

	    return result;
	}
		
	    
	public int updateStepStatus(User usr, int protocolId,int stepId, String status) {
		int result = 0;
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Authorization", "Bearer " + usr.getToken());

		headers.add("Content-Type", "application/json");
		String requestBody = "{\"status\": \"" + status + "\"}";
		String apiUrl = Constants.api_server + Constants.api_ep_protocol+'/'+ protocolId + "/protocol-step/"+ stepId +"/status";
		HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
		System.out.println(apiUrl);
		System.out.println(entity);
		try {
	        ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.PATCH, entity, String.class);
	        if (response.getStatusCode().is2xxSuccessful()) {

	            result = 1;
	        } else {
	            System.out.println("Failed to fetch data. Status code: " + response.getStatusCode());
	            // Update result to indicate a specific type of failure
	        }  
			
		}
		catch(Exception e) {
			System.out.println("Error in Update Step Status");
	        e.printStackTrace();
		}
			
		return result;
	 
	}
	
	public int updateStepNote(User usr, int protocolId,int stepId, String note) {
		int result = 0;
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Authorization", "Bearer " + usr.getToken());

		headers.add("Content-Type", "application/json");
		String requestBody = "{\"note\": \"" + note + "\"}";
		String apiUrl = Constants.api_server + Constants.api_ep_protocol+'/'+ protocolId + "/protocol-step/"+ stepId +"/note";
		HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
		System.out.println(apiUrl);
		System.out.println(entity);
		try {
	        ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.PATCH, entity, String.class);
	        if (response.getStatusCode().is2xxSuccessful()) {

	            result = 1;
	        } else {
	            System.out.println("Failed to fetch data. Status code: " + response.getStatusCode());
	            // Update result to indicate a specific type of failure
	        }  
			
		}
		catch(Exception e) {
			System.out.println("Error in updating note");
	        e.printStackTrace();
		}
			
		return result;
	 
	}
}
