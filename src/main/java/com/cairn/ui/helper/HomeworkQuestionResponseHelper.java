package com.cairn.ui.helper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.cairn.ui.Constants;
import com.cairn.ui.model.AssignedHomeworkResponseList;
import com.cairn.ui.model.Entity;
import com.cairn.ui.model.ExpectedHomeworkResponses;
import com.cairn.ui.model.Homework;
import com.cairn.ui.model.HomeworkQuestion;
import com.cairn.ui.model.HomeworkResponse;
import com.cairn.ui.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class HomeworkQuestionResponseHelper {
	
	 Logger logger = LoggerFactory.getLogger(HomeworkQuestionResponseHelper.class); 
	 
	    private APIHelper apiHelper = new APIHelper();

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
		
	    public List<HomeworkResponse> getHomeworkResponsesByProtocolId(User user, int protocolId) {
	        List<HomeworkResponse> results = new ArrayList<>();
	        String apiUrl = dashboardApiBaseUrl + "homework-response/protocol/" + protocolId;

	        HttpEntity<String> entity = Entity.getEntity(user, apiUrl);
	        try {
	            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, String.class);
	            if (response.getStatusCode().is2xxSuccessful()) {
	                String jsonResponse = response.getBody();
	                ObjectMapper objectMapper = new ObjectMapper();
	                JsonNode rootNode = objectMapper.readTree(jsonResponse);

	                JsonNode responsesNode = rootNode.get("responses");
	                if (responsesNode != null && responsesNode.isArray()) {
	                    for (JsonNode responseNode : responsesNode) {
	                        HomeworkResponse homeworkResponse = new HomeworkResponse();
	                        homeworkResponse.setResponse(responseNode.path("response").asText());
	                        homeworkResponse.setTooltip(responseNode.path("tooltip").asText());
	                        homeworkResponse.setCategoryId(responseNode.path("categoryId").asInt());
	                        homeworkResponse.setProtocolId(responseNode.path("protocolId").asInt());

	                        results.add(homeworkResponse);
	                    }
	                } else {
	                    System.out.println("No 'responses' array found in the JSON response.");
	                }
	            } else {
	                System.out.println("Failed to fetch data. Status code: " + response.getStatusCode());
	            }
	        } catch (Exception e) {
	            System.out.println("Error retrieving Homework Responses: " + e.getMessage());
	        }
	        return results;
	    }
	    
	    public List<HomeworkResponse> getHomeworkResponsesByCategoryId(User user, int categoryId) {
	        List<HomeworkResponse> results = new ArrayList<>();
	        String apiUrl = dashboardApiBaseUrl + "/homework-question-response/category/" + categoryId;

	        HttpEntity<String> entity = Entity.getEntity(user, apiUrl);
	        try {
	            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, String.class);
	            if (response.getStatusCode().is2xxSuccessful()) {
	                String jsonResponse = response.getBody();
	                ObjectMapper objectMapper = new ObjectMapper();
	                JsonNode rootNode = objectMapper.readTree(jsonResponse);

	                JsonNode responsesNode = rootNode.get("responses");
	                if (responsesNode != null && responsesNode.isArray()) {
	                    for (JsonNode responseNode : responsesNode) {
	                        HomeworkResponse homeworkResponse = new HomeworkResponse();
	                        homeworkResponse.setResponse(responseNode.path("response").asText());
	                        homeworkResponse.setTooltip(responseNode.path("tooltip").asText());
	                        homeworkResponse.setCategoryId(responseNode.path("categoryId").asInt());
	                        homeworkResponse.setProtocolId(responseNode.path("protocolId").asInt());

	                        results.add(homeworkResponse);
	                    }
	                } else {
	                    System.out.println("No 'responses' array found in the JSON response.");
	                }
	            } else {
	                System.out.println("Failed to fetch data. Status code: " + response.getStatusCode());
	            }
	        } catch (Exception e) {
	            System.out.println("Error retrieving Homework Responses: " + e.getMessage());
	        }
	        return results;
	    }

		
		
    public ResponseEntity<ByteArrayResource> downloadResponseFile(User usr, String guid) throws IOException, URISyntaxException {
        String apiUrl = this.dashboardApiBaseUrl + Constants.api_homework_response_file + guid;
        HttpEntity<String> entity = Entity.getEntity(usr, apiUrl);

        ResponseEntity<ByteArrayResource> response = getRestTemplate().exchange(apiUrl, HttpMethod.GET, entity, ByteArrayResource.class);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            // Extract filename from Content-Disposition header
            String contentDisposition = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
            String fileName = "downloaded_file";
            if (contentDisposition != null && contentDisposition.contains("filename=")) {
                fileName = contentDisposition.substring(contentDisposition.indexOf("filename=") + 9).replace("\"", "").trim();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(response.getBody().contentLength())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(response.getBody());
        } else {
            throw new IOException("Failed to download file. Status code: " + response.getStatusCode());
        }
    }
    
}