package com.cairn.ui.helper;

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
    
    
}