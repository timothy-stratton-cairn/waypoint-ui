package com.cairn.ui.helper;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.cairn.ui.Constants;
import com.cairn.ui.model.Entity;
import com.cairn.ui.model.ExpectedHomeworkResponses;
import com.cairn.ui.model.HomeworkQuestion;
import com.cairn.ui.model.HomeworkQuestionsTemplate;
import com.cairn.ui.model.HomeworkResponse;
import com.cairn.ui.model.HomeworkTemplate;
import com.cairn.ui.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class HomeworkTemplateHelper{
    Logger logger = LoggerFactory.getLogger(HomeworkTemplateHelper.class); 
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
    
    
    
    public ArrayList<HomeworkTemplate> getList(User usr) {
		ArrayList<HomeworkTemplate> results = new ArrayList<HomeworkTemplate>();
		
		if (usr == null) {
			return results;
		}

		String apiUrl = this.dashboardApiBaseUrl + Constants.api_homeworktemplate;
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
					JsonNode hwork = jsonNode.get("homeworkTemplates");
					// Iterate through the array elements
					HomeworkTemplate entry = null;
					if (hwork.isArray()) {
						for (JsonNode element : hwork) {
							// Access and print array elements
							if (element != null) {
								entry = new HomeworkTemplate();
								entry.setName(element.get("name").asText());
								entry.setId(Integer.valueOf(element.get("id").toString()));
								entry.setStatus(element.get("status").asText());
								if (element.has("description") && !element.get("description").isNull()) {
	                                entry.setDescription(element.get("description").asText());
	                            } else {
	                                entry.setDescription("No Description Given");
	                            }
								entry.getNumClients();
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
				logger.info("Failed to fetch data. Status code: " + response.getStatusCode());
			}
		} catch (Exception e) {
			logger.info("testing");
		}

		return results;

		
	}
    
    
    private void populateTemplateFromJson(JsonNode rootNode, HomeworkTemplate template) { //helper to populate the template properly from a json 
        template.setId(rootNode.path("id").asInt());
        template.setName(rootNode.path("name").asText());
        template.setDescription(rootNode.path("description").asText());
        template.setStatus(rootNode.path("status").asText());

        JsonNode questionsNode = rootNode.path("homeworkQuestions").path("questions");
        if (!questionsNode.isMissingNode()) {
            List<HomeworkQuestionsTemplate> questions = new ArrayList<>();
            questionsNode.forEach(questionNode -> {
                HomeworkQuestionsTemplate question = new HomeworkQuestionsTemplate();
                question.setQuestionAbbreviation(questionNode.path("questionAbbreviation").asText());
                question.setQuestion(questionNode.path("question").asText());
                question.setQuestionType(questionNode.path("questionType").asText());
                question.setRequired(questionNode.path("isRequired").asBoolean());

                ExpectedHomeworkResponses responses = new ExpectedHomeworkResponses();
                JsonNode responsesNode = questionNode.path("expectedHomeworkResponses").path("responses");
                List<HomeworkResponse> responseList = new ArrayList<>();

                if (!responsesNode.isMissingNode() && responsesNode.isArray()) {
                    responsesNode.forEach(responseNode -> {
                        HomeworkResponse response = new HomeworkResponse();
                        response.setResponse(responseNode.path("response").asText());
                        response.setTooltip(responseNode.path("tooltip").asText());
                        responseList.add(response);
                    });
                    responses.setResponses(responseList);
                    responses.setNumOfResponses(responseList.size());
                }
                question.setExpectedHomeworkResponses(responses);
                questions.add(question);
            });
            template.setQuestions(questions);
        }
    }
    
    
    public HomeworkTemplate getTemplate(User usr, int id) {
        HomeworkTemplate result = new HomeworkTemplate();

        String apiUrl = Constants.api_server + Constants.api_homeworktemplate + "/" + id;
        HttpEntity<String> entity = Entity.getEntity(usr, apiUrl);
        logger.info(apiUrl);
        
        try {
            ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                String jsonResponse = response.getBody();
                ObjectMapper objectMapper = new ObjectMapper();

                JsonNode rootNode = objectMapper.readTree(jsonResponse);
                populateTemplateFromJson(rootNode, result);
            } else {
                logger.info("Failed to fetch data. Status code: " + response.getStatusCode());
            }
        } catch (Exception e) {
            logger.info("Error retrieving homework template: " + e.getMessage());
        }

        return result;
    }
    
    public String newTemplate(User usr, String templateBody) {
    	String result = "Process not yet set ";
    	String apiUrl = Constants.api_server + Constants.api_homeworktemplate;
    	HttpEntity<String> entity = Entity.getEntityWithBody(usr, apiUrl, templateBody);
    	logger.info("Template Body: " + templateBody);
    	
		try {
	        ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.POST, entity, String.class);
	        if (response.getStatusCode().is2xxSuccessful()) {

	            result = "Success";
	        } else {
	        	result = "Failed to fetch data. Status code: " + response.getStatusCode();
	            logger.info("Failed to fetch data. Status code: " + response.getStatusCode());
	            // Update result to indicate a specific type of failure
	        }  
			
		} catch (HttpClientErrorException.Conflict e) {
            // Specifically handle the conflict exception
            String errorResponse = e.getResponseBodyAsString();
            System.err.println("Conflict Error: " + errorResponse);
            result = "Error"+ errorResponse;
		}
		catch(Exception e) {
			logger.info("Error in updating Comment");
	        e.printStackTrace();
	        result = "Error in creating new Template: " + e;
		}
		
			
		return result;

	}
    
    
    
    public int removeQuestionFromTemplate(User usr,int tempId, List<HomeworkQuestionsTemplate> questions) {
    	int result = -1;
    	String requestBody = "{\"homeworkQuestions\": {";
    	
    	for (HomeworkQuestionsTemplate question: questions) {
    		String bodyString = "\"questions\": [" +
                    "{" +
                    "\"questionAbbreviation\": \"" + question.getQuestionAbbreviation() + "\"," +
                    "\"question\": \"" + question.getQuestion() + "\"," +
                    "\"questionType\": \"" + question.getQuestionType() + "\"," +
                    "\"isRequired\": " + question.isRequired() + "," +
                    "}}";
    		
    		requestBody = requestBody+bodyString;
    		
    	}
    	String apiUrl = Constants.api_server + Constants.api_homeworktemplate;
    	HttpEntity<String> entity = Entity.getEntityWithBody(usr, apiUrl, requestBody);
    	
		try {
	        ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.POST, entity, String.class);
	        if (response.getStatusCode().is2xxSuccessful()) {

	            result = 1;
	        } else {
	        	result = -1;
	            logger.info("Failed to fetch data. Status code: " + response.getStatusCode());
	            // Update result to indicate a specific type of failure
	        }  
			
		}
		catch(Exception e) {
			logger.info("Error in updating Comment");
	        e.printStackTrace();
		}
    	
    	return result;
    }
    
    public int deleteHomeworkTemplate(User usr, int homeworkTemplateId) {
    	int result = 0;
    	String apiUrl = Constants.api_server + Constants.api_homeworktemplate_get + homeworkTemplateId;
    	logger.info(apiUrl);
        result = apiHelper.deleteAPI(apiUrl, usr);
        
    	
    	return result;
    }
    
    public int removeHomeworkTemplateFromStep(User usr, int homeworkTemplateId, int stepId ) {
    	int result = 0;
    	
    	String apiUrl = Constants.api_server + Constants.api_ep_protocolsteptemplate_get +'/' + homeworkTemplateId;
    	logger.info(apiUrl);
        result = apiHelper.deleteAPI(apiUrl, usr);
        return result;
    }
    public ArrayList<HomeworkQuestion> getHomeworkQuestions(User usr){
    	ArrayList<HomeworkQuestion> results = new ArrayList<HomeworkQuestion>();
    	String apiUrl = Constants.api_server + Constants.api_homework_question ;
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
					JsonNode hwork = jsonNode.get("questions");
					// Iterate through the array elements
					HomeworkQuestion entry = null;
					if (hwork.isArray()) {
						for (JsonNode element : hwork) {
							// Access and print array elements
							if (element != null) {
								entry = new HomeworkQuestion();
								entry.setQuestionId(element.get("questionId").asInt());
								entry.setQuestionAbbreviation(element.get("questionAbbr").asText());
								entry.setQuestion(element.get("question").asText());
								entry.setStatus(element.get("status").asText());
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
				logger.info("Failed to fetch data. Status code: " + response.getStatusCode());
			}
		} catch (Exception e) {

			logger.info("No Homeworks Returned");

		}
    	
    	return results;
    	
    }
    
    public int newHomeworkQuestion(User usr, HomeworkQuestion question) {
        int result = 0;
        
        String apiUrl = Constants.api_server + Constants.api_homework_question;
        String requestBody = "{" +
            "\"questionAbbr\": \"" + question.getQuestionAbbreviation() + "\"," +
            "\"question\": \"" + question.getQuestion() + "\"," +
            "\"questionType\": \"" + question.getQuestionType() + "\"," +
            "\"isRequired\": " + question.isRequired() + "," +
            "\"responseOptions\": [";

        if (question.getExpectedHomeworkResponses() != null && question.getExpectedHomeworkResponses().getResponses() != null) {
            List<HomeworkResponse> responses = question.getExpectedHomeworkResponses().getResponses();
            for (int i = 0; i < responses.size(); i++) {
                HomeworkResponse response = responses.get(i);
                requestBody += "{" +
                    "\"response\": \"" + response.getResponse() + "\"," +
                    "\"tooltip\": \"" + response.getTooltip() + "\"" +
                    "}";
                if (i < responses.size() - 1) {
                    requestBody += ",";
                }
            }
        }

        requestBody += "]," +
            "\"triggerProtocolCreation\": " + question.getIsTriggeringReponse() + "," +
            "\"triggeringResponse\": {" +
            "\"response\": \"" + (question.getTriggerResponse() != null ? question.getTriggerResponse().getResponse() : "") + "\"," +
            "\"tooltip\": \"" + (question.getTriggerResponse() != null ? question.getTriggerResponse().getTooltip() : "") + "\"" +
            "}," +
            "\"triggeredProtocolId\": " + question.getTriggerProtocolId() +
            "}";

        logger.info(requestBody);
        HttpEntity<String> entity = Entity.getEntityWithBody(usr, apiUrl, requestBody);
        
		try {
	        ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.POST, entity, String.class);
	        if (response.getStatusCode().is2xxSuccessful()) {

	            result = 1;
	        } else {
	        	result = -1;
	            logger.info("Failed to fetch data. Status code: " + response.getStatusCode());
	            // Update result to indicate a specific type of failure
	        }  
			
		}
		catch(Exception e) {
			logger.info("Error in Posting Question");
	        e.printStackTrace();
		}
        

        return result;
    }


    
    
    
}