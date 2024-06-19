package com.cairn.ui.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

import net.minidev.json.JSONObject;


@Service 
public class ProtocolStepTemplateHelper{

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
    
    public ArrayList<ProtocolStepTemplate> getStepList(User usr) {
	    String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocolsteptemplate;
	    HttpEntity<String> entity = Entity.getEntity(usr, apiUrl);
    	ArrayList<ProtocolStepTemplate> stepTemplateList = new ArrayList<ProtocolStepTemplate>();
    	
        try {
            ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                String jsonResponse = response.getBody();
                ObjectMapper objectMapper = new ObjectMapper();

                JsonNode rootNode = objectMapper.readTree(jsonResponse);
                if (rootNode.isArray()) {
                    for (JsonNode jsonNode : rootNode) {
                        ProtocolStepTemplate template = new ProtocolStepTemplate();
                        template.setName(jsonNode.get("name").asText());
                        template.setDescription(jsonNode.get("description").asText());
                        template.setId(jsonNode.get("id").asInt());
                        
                        JsonNode stepTemplateCategoryNode = jsonNode.path("category");
                        if (!stepTemplateCategoryNode.isMissingNode() && !stepTemplateCategoryNode.path("id").isMissingNode()) {
                            template.setCategoryId(stepTemplateCategoryNode.path("id").asInt());
                            template.setCategoryName(stepTemplateCategoryNode.path("name").asText());
                            template.setCategoryDescription(stepTemplateCategoryNode.path("description").asText());
                        } else {
                            template.setType(0); // Set type to 0 if "stepTemplateCategory" or "id" is missing.
                        }
                        
                        JsonNode perms = jsonNode.get("linkedHomeworkTemplates");
                        ArrayList<HomeworkTemplate> homeworks = new ArrayList<>();
                        if (perms.isArray()) {
                            for (JsonNode element : perms) {
                                if (element != null) {
                                    HomeworkTemplate curHw = new HomeworkTemplate();
                                    curHw.setName(element.get("name").asText());
                                    curHw.setId(element.get("id").asInt());
                                    curHw.setDescription(element.get("description").asText());
                                    homeworks.add(curHw);
                                }
                            }
                            template.setHomework(homeworks);
                        }
                        
                        stepTemplateList.add(template);
                    }
                }
            } else {
                System.out.println("Failed to fetch data. Status code: " + response.getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("No protocols returned");
        }
    	
    	
    	return stepTemplateList;
    }
    
    
   

    public int addStepTemplate(User usr, ProtocolStepTemplate newStep) {
        int result = -1;
        List<HomeworkTemplate> homeworkList = newStep.getHomework();
        StringBuilder homeworkIds = new StringBuilder("\"linkedHomeworkTemplateIds\":");
        
        if (homeworkList == null || homeworkList.isEmpty()) {
            homeworkIds.append("null"); 
        } else {
            homeworkIds.append("[");
            for (int i = 0; i < homeworkList.size(); i++) {
                HomeworkTemplate hw = homeworkList.get(i);
                homeworkIds.append(hw.getId());
                if (i < homeworkList.size() - 1) {
                    homeworkIds.append(", ");  // Append a comma and a space if not the last element
                }
            }
            homeworkIds.append("]");  // Close the array outside the loop
        }
        
        // Construct the final JSON string
        String requestBody = "{"
                + "\"name\": \"" + newStep.getName() + "\","
                + "\"description\": \"" + newStep.getDescription() + "\","
                + "\"linkedStepTaskId\": null,"  
                + homeworkIds.toString() + ","
                + "\"stepTemplateCategoryId\": " + newStep.getCategoryId() + "}";
        
        String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocolsteptemplate;
        System.out.println(apiUrl);
        System.out.println(requestBody);
        HttpEntity<String> entity = Entity.getEntityWithBody(usr, apiUrl, requestBody);
        try {
            ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.POST, entity, String.class);
            System.out.println(response);
            if (response.getStatusCode().is2xxSuccessful()) {
                // Parse the response body to get the stepTemplateId
            	String jsonResponse = response.getBody();
				ObjectMapper objectMapper = new ObjectMapper();

				JsonNode jsonNode;
				jsonNode = objectMapper.readTree(jsonResponse);
                if (jsonNode.has("stepTemplateId")) {
                    result = jsonNode.get("stepTemplateId").asInt();
                } else {
                    result = -1; // Set to -1 if stepTemplateId is not present in the response
                }
            } else {
                result = -1;
                System.out.println("Failed to Post data. Status code: " + response.getStatusCode());
            }  
        } catch (Exception e) {
            result = -1;
            System.out.println("Error in addStepTemplate");
            e.printStackTrace();
        }
        System.out.println(result);
        return result;
    }

    
    
    public int addHomeworkTemplate(User usr, int stepTemplateId, int homeworkId) {
    	int result = 0;
    	
	    String requestBody = "{\"linkedHomeworkTemplateIds\": [" + homeworkId + "]}";
		String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocolsteptemplate_get +"/"+ stepTemplateId ;
		HttpEntity<String> entity = Entity.getEntityWithBody(usr, apiUrl,requestBody);
		
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
			System.out.println("Error in addHomeworkTemplate");
	        e.printStackTrace();
		}
    	return result;
    	
    }
    
    public int updateStepTemplate(User usr,int id ,ProtocolStepTemplate step) {
    	int result = -1;
		String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocolsteptemplate_get +"/"+ id ;
		System.out.println(apiUrl);
		String requestBody = "{"
		    + "\"name\": \"" + step.getName() + "\","
		    + "\"description\": \"" + step.getDescription() + "\","
		    + "\"stepTemplateCategoryId\": " + step.getCategoryId() + "}";
		
		System.out.println(requestBody);
		    
		HttpEntity<String> entity = Entity.getEntityWithBody(usr, apiUrl,requestBody);
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
			System.out.println("Error in addHomeworkTemplate");
	        e.printStackTrace();
		}
    	return result;

    }
    
    
    public ProtocolStepTemplate getTemplate(User usr, int id) {
    	
    	
    	ProtocolStepTemplate result = new ProtocolStepTemplate();
	    String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocolsteptemplate_get + "/"+id;
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
					JsonNode perms = jsonNode.get("linkedHomeworkTemplates");
					ArrayList <HomeworkTemplate>homeworks = new ArrayList<HomeworkTemplate>();
					if (perms.isArray()) {
						for (JsonNode element : perms) {
							// Access and print array elements
							if (element != null) {
								HomeworkTemplate curHw = new HomeworkTemplate();
								curHw.setName(element.get("name").asText());
								
								curHw.setId(Integer.parseInt(element.get("id").asText()));
								curHw.setDescription(element.get("description").asText());
								homeworks.add(curHw);
								System.out.println(curHw);
							}
							else {
								System.out.println("No Homework Retrieved");
							}
						}
						System.out.println(homeworks);
						result.setHomework(homeworks);
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
    
    public int deleteStepTemplate(User usr, int stepTemplateId) {
    	int result = 0;
    	String apiUrl = Constants.api_server + Constants.api_ep_protocolsteptemplate_get + stepTemplateId;
        HttpEntity<String> entity = Entity.getEntity(usr, apiUrl);
        try {
			ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.DELETE, entity, String.class);
			if (response.getStatusCode().is2xxSuccessful()) {
			result = 1;
			}
        }
	    catch (Exception e) {
	    	System.out.println("Error in deleteStepTemplate");
	        e.printStackTrace();
	    }
        
    	
    	return result;
    }
    
    
    public int deleteHomeworkTemplate(User usr, int stepId, int homeworkId) {
    	int result = -1;
		String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocolsteptemplate_get +"/"+ stepId + "?homeworkTemplateId="+homeworkId ;
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
			System.out.println("Error in deleteHomeworkTemplate");
	        e.printStackTrace();
		}
    	return result;
    }
    
}