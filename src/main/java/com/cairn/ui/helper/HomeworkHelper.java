package com.cairn.ui.helper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.cairn.ui.Constants;
import com.cairn.ui.dto.HomeworkDto;
import com.cairn.ui.dto.HomeworkListDto;
import com.cairn.ui.model.Entity;
import com.cairn.ui.model.Homework;
import com.cairn.ui.model.HomeworkQuestion;
import com.cairn.ui.model.HomeworkTemplate;
import com.cairn.ui.model.Protocol;
import com.cairn.ui.model.ProtocolStats;
import com.cairn.ui.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HomeworkHelper{
	
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
    

    public ArrayList<Homework> getHomeworkByProtocolId(User usr, int protocolId){

    	ArrayList<Homework> results = new ArrayList<Homework>();
		if (usr == null) {
			System.out.println("No User found");
			return results;
		}

		String apiUrl = Constants.api_server + Constants.api_homework+ "protocol/" + protocolId;
		HttpEntity<String> entity = Entity.getEntity(usr, apiUrl);
		System.out.println(apiUrl);
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
							JsonNode hwork = jsonNode.get("homeworks");
							// Iterate through the array elements
							Homework entry = null;
							if (hwork.isArray()) {
								for (JsonNode element : hwork) {
									// Access and print array elements
									if (element != null) {
										entry = new Homework();
										entry.setId(element.get("homeworkId").asInt());
										entry.setName(element.get("name").asText());
										if (element.has("description") && !element.get("description").isNull()) {
			                                entry.setDescription(element.get("description").asText());
			                            } else {
			                                entry.setDescription("No Description Given");
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

					System.out.println("No Homeworks Returned");

				}
				return results;
    }
    
    public HomeworkListDto getHomeworkByProtocol(User usr, int id) {

    	HomeworkListDto result = new HomeworkListDto();

		String apiUrl = Constants.api_server + Constants.api_homework+ "protocol/" + id;
		
		System.out.println(apiUrl);
		
		HttpEntity<String> entity = Entity.getEntity(usr, apiUrl);
		try {
			ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.GET, entity, String.class);
			// Process the response
			if (response.getStatusCode().is2xxSuccessful()) {
				String jsonResponse = response.getBody();
				ObjectMapper objectMapper = new ObjectMapper();
				result = objectMapper.readValue(jsonResponse, HomeworkListDto.class);

			} else {
				System.out.println("Failed to fetch data. Status code: " + response.getStatusCode());
			}
		} catch (Exception e) {
			System.out.println("No Testing");
		}
    	
    	return result;
    }
    
    public Homework getHomeworkByHomeworkId(User usr, int id) {
    	Homework result = new Homework();
        if (usr == null) {
            System.out.println("No User found");
            return null; // Returning null to indicate user not found, handle accordingly
        }

        String apiUrl = Constants.api_server + Constants.api_homework + id;
        HttpEntity<String> entity = Entity.getEntity(usr, apiUrl);
        System.out.println(apiUrl);

        try {
			ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.GET, entity, String.class);
			// Process the response
			if (response.getStatusCode().is2xxSuccessful()) {
				String jsonResponse = response.getBody();
				System.out.println("API response: " + jsonResponse);
				ObjectMapper objectMapper = new ObjectMapper();

				JsonNode jsonNode;
				try {
					jsonNode = objectMapper.readTree(jsonResponse);
					result.setName(jsonNode.get("name").asText());
					result.setId(jsonNode.get("homeworkId").asInt());
					result.setDescription(jsonNode.get("description").asText());
			
					JsonNode homeworkQuestionsNode = jsonNode.get("homeworkQuestions");
					ArrayList<HomeworkQuestion> homeworkQuestion = new ArrayList<HomeworkQuestion>();
					if (homeworkQuestionsNode != null) {
						JsonNode questionsNode = jsonNode.path("homeworkQuestions").path("questions");
						if ( questionsNode.isArray()) {
							for( JsonNode questionNode: questionsNode) {
									HomeworkQuestion curQuestion = new HomeworkQuestion();
						
									curQuestion.setQuestionId(questionNode.path("questionId").asInt());
									
									curQuestion.setQuestionAbbreviation(questionNode.path("questionAbbr").asText());
								
									curQuestion.setQuestion(questionNode.path("question").asText());
									
									curQuestion.setQuestionType(questionNode.path("questionType").asText());
									
									curQuestion.setRequired(questionNode.path("isRequired").asBoolean());
									
									curQuestion.setUserResponse(questionNode.path("userResponse").asText());
									
									homeworkQuestion.add(curQuestion);
								
								
							}
						} else {
						    System.out.println("No 'questions' array found in the JSON response.");
						}
					}
					result.setQuestions(homeworkQuestion);
					
					
					
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Error "+ e);
				}
            } else {
                System.out.println("Failed to fetch data. Status code: " + response.getStatusCode());
            }
        } catch (Exception e) {
        	e.printStackTrace();
            System.out.println("Error processing the homework fetch request: " + e.getMessage());
        }
        return result; // Return null to indicate that no homework was fetched
    }
    
    
    /*public int assignAnswerToHomework(User usr, int homeworkId, int questionId, String userResponse) {
    	int result = -1;
    	if (usr == null) {
            System.out.println("No User found");
            return result; // Returning null to indicate user not found, handle accordingly
        }

        String apiUrl = Constants.api_server + Constants.api_homework + homeworkId;
        System.out.println("ID "+questionId + " Response " + userResponse);
        String requestBody = "{\"responses\": [" +
                "{" +
                "\"questionId\": " + questionId + "," +
                "\"userResponse\": \"" + userResponse + "\"" +
                "}" +
                "]}";

        System.out.println(requestBody);
        HttpEntity<String> entity = Entity.getEntityWithBody(usr, apiUrl, requestBody);
        System.out.println(apiUrl);
        
        try {
			ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.PATCH, entity, String.class);
			if (response.getStatusCode().is2xxSuccessful()) {

	            result = 1;
	        } else {
	        	result = -1;
	            System.out.println("Failed to fetch data. Status code: " + response.getStatusCode());
	            // Update result to indicate a specific type of failure
	        }  
        }catch(Exception e) {
        	e.printStackTrace();
            System.out.println("Error assigning user response: " + e.getMessage());
        	
        }
    	
    	return result;
    }*/
    public int assignAnswerToHomework(User usr, int homeworkId, int questionId, String userResponse, String filePath) {
        int result = -1;
        
        String apiUrl = Constants.api_server + Constants.api_homework + homeworkId;

        // Prepare the JSON part of the request
        String requestBody = "{\"responses\": [{\"questionId\": " + questionId + ", \"userResponse\": \"" + userResponse + "\"}]}";
        HttpHeaders headersJson = new HttpHeaders();
        headersJson.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> partJson = Entity.getEntityWithBody(usr, apiUrl, requestBody);

        // Prepare the MultiValueMap to be the body of the request
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("json", partJson);
        HttpEntity<Resource> partFile;
        
        if (filePath==null) {

        if (filePath != null && !filePath.trim().isEmpty()) {
            Resource fileResource = new FileSystemResource(filePath);
            if (fileResource.exists() && fileResource.isReadable()) {
                System.out.println("File Path readable: " + filePath);
                
                partFile = Entity.getFileEntity(usr, apiUrl, fileResource);
            } else {
                System.out.println("File path does not exist or is not readable: " + filePath);
                partFile = Entity.getFileEntity(usr, apiUrl, null);
            }
        } else {
            System.out.println("No file will be attached as the file path is either null or empty.");
            partFile = Entity.getFileEntity(usr, apiUrl, null);
        }
        body.add("files", partFile);
        // Set up headers for the multipart request
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + usr.getToken());
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        System.out.println(body);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.PATCH, requestEntity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                result = 1; // Indicates success
            } else {
                result = -1;
                System.out.println("Failed to update the homework response. Status code: " + response.getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error assigning user response: " + e.getMessage());
        }

        return result;
    }

}