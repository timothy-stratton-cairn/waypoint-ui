package com.cairn.ui.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.cairn.ui.Constants;
import com.cairn.ui.model.ProtocolStepTemplate;
import com.cairn.ui.model.ProtocolTemplate;
import com.cairn.ui.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


@Service 
public class ProtocolStepTemplateHelper{
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
    	
    	HttpHeaders headers = new HttpHeaders();
	    headers.add("Authorization", "Bearer " + usr.getToken());
	    headers.add("Content-Type", "application/json");
	    String requestBody = "{\"linkedHomeworkTemplateIds\": [" + homeworkId + "]}";

		String apiUrl = Constants.api_server + Constants.api_ep_protocolsteptemplate_get +"/"+ stepTemplateId ;
		
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
			System.out.println("Error in addHomeworkTemplate");
	        e.printStackTrace();
		}
    	return result;
    	
    }
    
    
}