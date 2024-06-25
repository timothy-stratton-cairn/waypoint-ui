package com.cairn.ui.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.cairn.ui.Constants;
import com.cairn.ui.model.Entity;
import com.cairn.ui.model.HomeworkQuestion;
import com.cairn.ui.model.HomeworkResponse;
import com.cairn.ui.model.HomeworkTemplate;
import com.cairn.ui.model.ProtocolStepTemplate;
import com.cairn.ui.model.ProtocolTemplate;
import com.cairn.ui.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class HomeworkQuestionHelper{
    Logger logger = LoggerFactory.getLogger(ProtocolTemplateHelper.class); 
    private APIHelper apiHelper = new APIHelper();

	@Value("${waypoint.dashboard-api.base-url}")
	private String dashboardApiBaseUrl;
	
	
	
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
        result = apiHelper.postAPI(apiUrl, requestBody, usr);
		return result;
        
    }
    public ArrayList<HomeworkQuestion> getHomeworkQuestions(User usr){
    	ArrayList<HomeworkQuestion> results = new ArrayList<HomeworkQuestion>();
    	String apiUrl = Constants.api_server + Constants.api_homework_question ;
        HttpEntity<String> entity = Entity.getEntity(usr, apiUrl);
        

        // Create a HttpEntity with the headers
        String jsonResponse = apiHelper.callAPI(apiUrl, usr);
        if (!jsonResponse.isEmpty()) {
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
	            logger.info("Failed to fetch getAllSteps data.");
	        }
		
    	
    	return results;
    	
    }
    
    public HomeworkQuestion getQuestion(User usr, int id) {
    	HomeworkQuestion result = new HomeworkQuestion();
    	String apiUrl = this.dashboardApiBaseUrl + Constants.api_homework_question_get + id;
		String jsonResponse = apiHelper.callAPI(apiUrl, usr);
		if (!jsonResponse.isEmpty()) {
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode jsonNode;
			try {
				jsonNode = objectMapper.readTree(jsonResponse);
				result.setQuestionId(jsonNode.get("id").asInt());
				result.setQuestionAbbreviation(jsonNode.get("questionAbbr").asText());
				result.setQuestion(jsonNode.get("question").asText());
				result.setStatus(jsonNode.get("status").asText());
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		} else {
			logger.info("Failed to fetch data for protocol template.");
		}
			
			
    	return result;
    }
    
    
    public int updateHomeowrkQuestion(User usr, int id, HomeworkQuestion question){
    	int result = -1;
    	String apiUrl = this.dashboardApiBaseUrl+ Constants.api_homework_question_get + id;
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

    	result = apiHelper.patchAPI(apiUrl, requestBody, usr);
    	return result;
    }

}