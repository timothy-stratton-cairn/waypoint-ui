package com.cairn.ui.helper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.time.Instant;

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
					if (jsonNode.has("goal") && !jsonNode.get("goal").isNull()) {
					    result.setGoal(jsonNode.get("goal").asText());
					} else {
					    result.setGoal("No Goal Set"); // Fallback if goal is not set
					}


					if (jsonNode.has("goalProgress") && !jsonNode.get("goalProgress").isNull()) {
					    result.setProgress(jsonNode.get("goalProgress").asText());
					} else {
					    result.setProgress("None"); // Fallback if goalProgress is not present or is null
					}

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
					result.setStepCount();
					System.out.println(result.getStepCount());

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

	    String apiUrl = Constants.api_server + Constants.api_ep_protocolaccount + clientId; //retrieves all protocols assigned to clientId
	    
	    try {
	        ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.GET, entity, String.class);
	        // Process the response
	        if (response.getStatusCode().is2xxSuccessful()) {
	            String jsonResponse = response.getBody();
	            ObjectMapper objectMapper = new ObjectMapper();

	            JsonNode jsonNode = objectMapper.readTree(jsonResponse);
	            JsonNode prots = jsonNode.get("protocols");
	            // Iterate through the array elements
	            if (prots != null && prots.isArray()) {
	                for (JsonNode element : prots) {
	                    Protocol entry = new Protocol();
	                    entry.setName(element.has("name") && !element.get("name").isNull() ? element.get("name").asText() : "null");
	                    entry.setDescription(element.has("description") && !element.get("description").isNull() ? element.get("description").asText() : "null");
	                    entry.setId(element.has("id") && !element.get("id").isNull() ? element.get("id").intValue() : -1); // Consider using a default ID like -1
	                    
	                    entry.setCompletionPercent(element.has("completionPercentage") && !element.get("completionPercentage").isNull() ? element.get("completionPercentage").asText() : "null");
	                    
	                    if (element.has("goal") && !element.get("goal").isNull()) {
	                        entry.setProgress(element.get("goal").asText());
	                    } else {
	                        entry.setGoal("No Goal Set"); // This should probably be entry.setGoal("null");
	                    }
	                    if(element.has("goalProgress") && !element.get("goalProgress").isNull()) {
	                        entry.setProgress(element.get("goalProgress").asText());
	                    } else {
	                        entry.setProgress("None"); // Adjusted as per your instruction, but "None" seems more appropriate here
	                    }
	                    
	                    JsonNode assoc = element.get("associatedSteps"); // This should target 'element', not 'jsonNode'
	                    ArrayList<ProtocolStep> steps = new ArrayList<ProtocolStep>();
	                    if (assoc != null) {
	                        JsonNode asteps = assoc.get("steps");
	                        if (asteps != null && asteps.isArray()) {
	                            for (JsonNode stepElement : asteps) {
	                                ProtocolStep curStep = new ProtocolStep();
	                                curStep.setId(stepElement.has("id") && !stepElement.get("id").isNull() ? stepElement.get("id").intValue() : -1);
	                                curStep.setName(stepElement.has("name") && !stepElement.get("name").isNull() ? stepElement.get("name").asText() : "null");
	                                curStep.setDescription(stepElement.has("description") && !stepElement.get("description").isNull() ? stepElement.get("description").asText() : "null");
	                                curStep.setNotes(stepElement.has("stepNotes") && !stepElement.get("stepNotes").isNull() && stepElement.get("stepNotes").has("notes") ? stepElement.get("stepNotes").get("notes").asText() : "null");
	                                curStep.setCategory(stepElement.has("category") && !stepElement.get("category").isNull() ? stepElement.get("category").asText() : "null");
	                                curStep.setStatus(stepElement.has("status") && !stepElement.get("status").isNull() ? stepElement.get("status").asText() : "null");
	                                steps.add(curStep);
	                            }
	                        }
	                        entry.setSteps(steps);
	                        entry.setStepCount();
	                        entry.setCompletedSteps();
	                        System.out.println("Step Count"+ entry.getStepCount() +  " Completed Steps" + entry.getCompletedSteps());
	                    }
	                    results.add(entry);
	                }
	            }
	        } else {
	            System.out.println("Failed to fetch data. Status code: " + response.getStatusCode());
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
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
	    	result = -1;
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
	        	result = -1;
	            System.out.println("Failed to fetch data. Status code: " + response.getStatusCode());
	            // Update result to indicate a specific type of failure
	        }  
			
		}
		catch(Exception e) {
			result = -1;
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
	        ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.POST, entity, String.class);
	        if (response.getStatusCode().is2xxSuccessful()) {

	            result = 1;
	        } else {
	        	result = -1;
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
	public int updateProtocolGoal(User usr, int protocolId, String goal) {
		int result = 0;
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Authorization", "Bearer " + usr.getToken());
		headers.add("Content-Type", "application/json");
		String requestBody ="{\"goal\": \"" + goal + "\"}";
		String apiUrl = Constants.api_server + Constants.api_ep_protocol+'/'+ protocolId;
		HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
		try {
	        ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.PATCH, entity, String.class);
	        if (response.getStatusCode().is2xxSuccessful()) {

	            result = 1;
	        } else {
	        	result = -1;
	            System.out.println("Failed to fetch data. Status code: " + response.getStatusCode());
	            // Update result to indicate a specific type of failure
	        }  
			
		}
		catch(Exception e) {
			System.out.println("Error in updated goal ");
	        e.printStackTrace();
		}
			
		return result;
		
	}
	
	public int updateProtocolComment(User usr, int protocolId, String comment) {
		int result = 0;
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Authorization", "Bearer " + usr.getToken());
	    Instant timestamp = Instant.now();
	    String username = usr.getUsername();
		headers.add("Content-Type", "application/json");
		String requestBody = "{\"comment\": \""+comment+"\"}";
		String apiUrl = Constants.api_server + Constants.api_ep_protocol + '/' + protocolId;
		HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
		System.out.println(apiUrl);
		System.out.println(entity);
		try {
	        ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.PATCH, entity, String.class);
	        if (response.getStatusCode().is2xxSuccessful()) {

	            result = 1;
	        } else {
	        	result = -1;
	            System.out.println("Failed to fetch data. Status code: " + response.getStatusCode());
	            // Update result to indicate a specific type of failure
	        }  
			
		}
		catch(Exception e) {
			System.out.println("Error in updating Comment");
	        e.printStackTrace();
		}
			
		return result;
		
	}
	
	public int updateProtocolProgress(User usr, int protocolId, String progress) {
		int result = 0;
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Authorization", "Bearer " + usr.getToken());
		headers.add("Content-Type", "application/json");
		String requestBody ="{\"goalProgress\": \"" + progress + "\"}";
		String apiUrl = Constants.api_server + Constants.api_ep_protocol+'/'+ protocolId;
		HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
		System.out.println(apiUrl);
		System.out.println(entity);
		try {
	        ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.PATCH, entity, String.class);
	        if (response.getStatusCode().is2xxSuccessful()) {

	            result = 1;
	        } else {
	        	result = -1;
	            System.out.println("Failed to fetch data. Status code: " + response.getStatusCode());
	            // Update result to indicate a specific type of failure
	        }  
			
		}
		catch(Exception e) {
			System.out.println("Error in updating progress");
	        e.printStackTrace();
		}
			
		return result;
		
	}
}
