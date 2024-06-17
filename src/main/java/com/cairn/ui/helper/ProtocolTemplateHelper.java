package com.cairn.ui.helper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.cairn.ui.Constants;
import com.cairn.ui.model.Entity;
import com.cairn.ui.model.HomeworkTemplate;
import com.cairn.ui.model.ProtocolStepTemplate;
import com.cairn.ui.model.ProtocolTemplate;
import com.cairn.ui.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ProtocolTemplateHelper {

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
				System.out.println("Failed to fetch data " + apiUrl + ". Status code: " + response.getStatusCode());
			}
		} catch (Exception e) {
			System.out.println("No records returned for " + apiUrl);
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
				System.out.println(apiUrl + "==>Failed to fetch data. Status code: " + response.getStatusCode());
			}

		} catch (Exception e) {
			System.out.println("Error in updating note");
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
				System.out.println(apiUrl + "==>Failed to fetch data. Status code: " + response.getStatusCode());
				// Update result to indicate a specific type of failure
			}

		} catch (Exception e) {
			System.out.println("Error in updating progress");
			e.printStackTrace();
		}
		return result;
	}
	

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
		
		if (usr == null) {
			return results;
		}

		String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocolsteptemplate;
		HttpEntity<String> entity = Entity.getEntity(usr, apiUrl);

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
	
	public int newProtocolTemplate (User usr, ProtocolTemplate template) {
		int result = -1;
		String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocoltemplate;
		
		StringBuilder associatedStepTemplateIds = new StringBuilder("\"associatedStepTemplateIds\":[");
		ArrayList<ProtocolStepTemplate>stepList = template.getSteps();
	    int dueDateDays = Integer.parseInt(template.getDueDate()); // Assuming dueDate is stored as total days
	    
		for (int i = 0; i < stepList.size(); i++) {
			ProtocolStepTemplate step = stepList.get(i);
			associatedStepTemplateIds.append(step.getId());
			if (i < stepList.size() - 1) {
				associatedStepTemplateIds.append(", ");  // Append a comma and a space if not the last element
            }
		}
		associatedStepTemplateIds.append(']');
		String requestBody = "{"  
				+ "\"name\": \"" + template.getName() + "\","
				+ "\"description\": \"" + template.getDescription() + "\","
				+ "\"dueDate\": \"" + dueDateDays + "\","
				+ associatedStepTemplateIds.toString()+
				"}";
		result = postAPI(apiUrl, requestBody, usr);
		return result;
	}
	
	
	
	//same as above but instead grabbing all types 
	public ArrayList<ProtocolStepTemplate> getAllSteps(User usr) {
		ArrayList<ProtocolStepTemplate> results = new ArrayList<ProtocolStepTemplate>();
		
		if (usr == null) {
			return results;
		}

		String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocolsteptemplate;
		// Create a HttpEntity with the headers
		HttpEntity<String> entity = Entity.getEntity(usr, apiUrl);

		

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
					JsonNode prots = jsonNode.get("stepTemplates");
					// Iterate through the array elements
					ProtocolStepTemplate entry = null;
					if (prots.isArray()) {
						int idx = 1;
						for (JsonNode element : prots) {
							// Access and print array elements
							if (element != null) {
								entry = new ProtocolStepTemplate();
								entry.setName(element.get("name").asText());
								entry.setId(Integer.valueOf(element.get("id").toString()));
								// Test data, fix this later
								entry.setType(idx++);
								if (idx > 4) {
									idx = 1;
								}
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
	public int saveProtocolTemplate(User usr, int tempId, String requestBody) {

		int result = -1;
	    String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocoltemplate + "/" + tempId;
	    result = patchAPI(apiUrl, requestBody, usr);
		
		return result;
	}
	
	
	public int updateProtocolTemplateName(User usr, int tempId, String name) {
		int result = -1;
		String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocoltemplateget + tempId;
		String requestBody = "{\"name\": \""+name+"\"}";
		
	    result = patchAPI(apiUrl, requestBody, usr);
	
		return result;
		
	}
	
	public int updateProtocolTemplateDescription(User usr, int tempId, String description) {
		int result = -1;
		
		String apiUrl = this.dashboardApiBaseUrl +  Constants.api_ep_protocoltemplateget + tempId;
		String requestBody = "{\"description\": \""+description+"\"}";
	    result = patchAPI(apiUrl, requestBody, usr);
		return result;
		
	}
	public int updateProtocolTemplateDueDate(User usr, int tempId, String date) {
		int result = -1;
		
		String apiUrl = this.dashboardApiBaseUrl +  Constants.api_ep_protocoltemplateget + tempId;
		String requestBody = "{\"dueDate\": \""+date+"\"}";
	    result = patchAPI(apiUrl, requestBody, usr);
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
	public int saveTemplateStep(User usr, int tempId, String requestBody ) {
		int result = -1;

	    // Prepare the request body
	    String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocolsteptemplate_get;
	    result = patchAPI(apiUrl, requestBody, usr);
	    
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
	public int addTemplateStep(User usr, ProtocolTemplate theTemplate, ProtocolStepTemplate theStep) {
	    // Initial result indicating failure
	    int result = -1;

	    // Prepare the request body
	    HttpHeaders headers = new HttpHeaders();
	    headers.add("Authorization", "Bearer " + usr.getToken());
	    
	    // Assuming you need to send theStep as part of the request body
	    HttpEntity<ProtocolStepTemplate> entity = new HttpEntity<>(theStep, headers);

	    String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocolsteptemplate_assign + "/" + theTemplate.getId()+ "/"+ theStep.getId();

	    try {
	        ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.PATCH, entity, String.class);
	        if (response.getStatusCode().is2xxSuccessful()) {
	            System.out.println("Assigned Step... " + response.getStatusCode());
	            // Update result to indicate success
	            result = 1;
	        } else {
	            System.out.println("Failed to fetch data. Status code: " + response.getStatusCode());
	            // Update result to indicate a specific type of failure
	            result = 0;
	        }
	    } catch (Exception e) {
	        System.out.println("Step not found or error in assigning step");
	        e.printStackTrace();
	        // Keep result as -1 or set to another specific value indicating error
	    }

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
	
	
	public int removeTemplateStep(User usr, int tempId, ArrayList<ProtocolStepTemplate >steps) {
		int result = 0;
	    
		String requestBody = "{\"associatedSteps\":[" + "{";
		for (ProtocolStepTemplate step : steps) {
			
			String bodyString = "{\"id\": " + step.getId() + ", " +
	                "\"name\": \"" + step.getName() + "\", " +
	                "\"description\": \"" + step.getDescription() + "\", " +
	                "\"category\": {" +
	                "\"id\": " + step.getCategoryId() + ", " +
	                "\"name\": \"" + step.getCategoryName() + "\", " +
	                "\"description\": \"" + step.getCategoryDescription() + "\"" +
	                "}}";
			
			requestBody = requestBody+bodyString;
		}

		String apiUrl = this.dashboardApiBaseUrl +  Constants.api_ep_protocoltemplateget + tempId;
	    result = patchAPI(apiUrl, requestBody, usr);
		
		return result;
	}

	
	/**
	 * Get an individual step Template.
	 * 
	 * @param usr User object that is the logged in user. The token from the instance is used to 
	 * authenticate the API call
	 * @param id Step Id used to retrieve the step template 
	 * @return
	 */
	public ProtocolStepTemplate getStep(User usr, int id) {
		ProtocolStepTemplate result = new ProtocolStepTemplate();

		// Prepare the request body

		String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocolsteptemplate_get + id;
		HttpEntity<String> entity = Entity.getEntity(usr, apiUrl);
		
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
					result.setId(Integer.valueOf(jsonNode.get("id").toString()));
				    JsonNode stepTemplateCategoryNode = jsonNode.path("category");
				    if (!stepTemplateCategoryNode.isMissingNode() && !stepTemplateCategoryNode.path("id").isMissingNode()) {
				        result.setCategoryId(stepTemplateCategoryNode.path("id").asInt());
				        result.setCategoryName(stepTemplateCategoryNode.path("name").asText());
				        result.setCategoryDescription(stepTemplateCategoryNode.path("description").asText());
				    } else {
				        result.setType(0); // Set type to 0 if "stepTemplateCategory" or "id" is missing.
				    }
				    JsonNode linkedHomeworkTemplatesNode = jsonNode.get("linkedHomeworkTemplates");
				    ArrayList<HomeworkTemplate> homeworks = new ArrayList<HomeworkTemplate>();
				    if (linkedHomeworkTemplatesNode != null) {
				        JsonNode homeworkTemplatesNode = linkedHomeworkTemplatesNode.get("homeworkTemplates");
				        if (homeworkTemplatesNode != null && homeworkTemplatesNode.isArray()) {
				            for (JsonNode element : homeworkTemplatesNode) {
				                // Check if the current element is not null
				                if (element != null) {
				                    HomeworkTemplate curHw = new HomeworkTemplate();
				                    curHw.setName(element.get("name").asText());
				                    curHw.setId(Integer.parseInt(element.get("id").asText()));
				                    homeworks.add(curHw);
				                }
				            }
				        }
				    }
				    result.setHomework(homeworks);


				} catch (JsonMappingException e) {
					e.printStackTrace();
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("Failed to fetch data. Status code: " + response.getStatusCode());
			}
		} catch (Exception e) {
		    e.printStackTrace(); // This will give you more detailed error information
		}

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

		String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocoltemplate;
		HttpEntity<String> entity = Entity.getEntity(usr, apiUrl);
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
					JsonNode prots = jsonNode.get("protocolTemplates");
					// Iterate through the array elements
					ProtocolTemplate entry = null;
					int idx = 1;
					if (prots.isArray()) {
						for (JsonNode element : prots) {
							// Access and print array elements
							if (element != null) {
								entry = new ProtocolTemplate();
								entry.setName(element.get("name").asText());
								entry.setId(Integer.valueOf(element.get("id").toString()));
								// Test data, fix this later

								results.add(entry);
								if (idx > 4) {
									idx = 1;
								}
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

		String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocoltemplateget + id;
		HttpEntity<String> entity = Entity.getEntity(usr, apiUrl);

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
					ProtocolStepTemplate entry = null;
					if (prots.isArray()) {
						int idx = 1;
						for (JsonNode element : prots) {
							// Access and print array elements
							if (element != null) {
								entry = new ProtocolStepTemplate();
								entry.setDescription(element.get("description").asText());
								entry.setName(element.get("name").asText());
								entry.setId(Integer.valueOf(element.get("id").toString()));
								
								// Test data, fix this later
								entry.setType(idx++);
								if (idx > 4) {
									idx = 1;
								}
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

		String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocoltemplate + "/" + id;
		// Create a HttpEntity with the headers
		HttpEntity<String> entity = Entity.getEntity(usr, apiUrl);


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
					result.setId(Integer.valueOf(jsonNode.get("id").toString()));
					result.setDueDate(jsonNode.get("dueDate").asText());
					/* Add in the steps */
					JsonNode perms = jsonNode.get("associatedSteps");
					// Iterate through the array elements
					ArrayList<ProtocolStepTemplate> steps = new ArrayList<ProtocolStepTemplate>();
					if (perms.isArray()) {
						for (JsonNode element : perms) {
							// Access and print array elements
							if (element != null) {
								ProtocolStepTemplate curStep = new ProtocolStepTemplate();
								curStep.setName(element.get("name").asText());
								curStep.setId(Integer.parseInt(element.get("id").asText()));
								curStep.setDescription(element.get("description").asText());
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
			System.out.println("No protocols returned");
		}

		return result;
	}
	
    public int deleteProtocolStepTemplate(User usr, int templateId, int stepId) {
    	int result = -1;
		String apiUrl = this.dashboardApiBaseUrl + Constants. api_ep_protocoltemplateget +"/"+ templateId + "?stepTemplateId="+stepId ;
		HttpEntity<String> entity = Entity.getEntity(usr, apiUrl);
		try {
	        ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.DELETE, entity, String.class);
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
			System.out.println("Error in deleteStepTemplate");
	        e.printStackTrace();
		}
    	return result;
    }
    
    public int deleteProtocolTemplate(User usr, int protocolTemplateId) {
    	int result = 0;
    	String apiUrl = Constants.api_server + Constants.api_ep_protocoltemplateget + protocolTemplateId;
        HttpEntity<String> entity = Entity.getEntity(usr, apiUrl);
        try {
			ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.DELETE, entity, String.class);
			if (response.getStatusCode().is2xxSuccessful()) {
			result = 1;
			}
        }
	    catch (Exception e) {
	        System.out.println("Error in deleting protocol");
	        e.printStackTrace();
	    }
        
    	
    	return result;
    }
}
