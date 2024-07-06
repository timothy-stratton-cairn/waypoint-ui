package com.cairn.ui.helper;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cairn.ui.Constants;
import com.cairn.ui.model.ExpectedHomeworkResponses;
import com.cairn.ui.model.HomeworkQuestion;
import com.cairn.ui.model.HomeworkResponse;
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

        String apiUrl = this.dashboardApiBaseUrl + Constants.api_homework_question;
        StringBuilder requestBody = new StringBuilder();
        requestBody.append("{")
            .append("\"questionAbbr\": \"").append(question.getQuestionAbbreviation()).append("\",")
            .append("\"question\": \"").append(question.getQuestion()).append("\",")
            .append("\"questionType\": \"").append(question.getQuestionType()).append("\",")
            .append("\"isRequired\": ").append(question.isRequired()).append(",")
            .append("\"responseOptions\": [");

        if (question.getExpectedHomeworkResponses() != null && question.getExpectedHomeworkResponses().getResponses() != null) {
            List<HomeworkResponse> responses = question.getExpectedHomeworkResponses().getResponses();
            for (int i = 0; i < responses.size(); i++) {
                HomeworkResponse response = responses.get(i);
                requestBody.append("{")
                    .append("\"response\": \"").append(response.getResponse()).append("\",")
                    .append("\"tooltip\": \"").append(response.getTooltip()).append("\"")
                    .append("}");
                if (i < responses.size() - 1) {
                    requestBody.append(",");
                }
            }
        }

        requestBody.append("],")
            .append("\"triggerProtocolCreation\": ").append(question.getIsTriggeringReponse()).append(",")
            .append("\"triggeringResponse\": {")
            .append("\"response\": \"").append(question.getTriggerResponse() != null ? question.getTriggerResponse().getResponse() : "").append("\",")
            .append("\"tooltip\": \"").append(question.getTriggerResponse() != null ? question.getTriggerResponse().getTooltip() : "").append("\"")
            .append("},");

        // Conditional check for triggeredProtocolId
        if (question.getTriggerProtocolId() > 0) {
            requestBody.append("\"triggeredProtocolId\": ").append(question.getTriggerProtocolId());
        } else {
            requestBody.append("\"triggeredProtocolId\": null");
        }

        requestBody.append("}");

        String requestBodyStr = requestBody.toString();
        logger.info(requestBodyStr);
        result = apiHelper.postAPI(apiUrl, requestBodyStr, usr);
        return result;
    }
    
    public ArrayList<HomeworkQuestion> getHomeworkQuestions(User usr){
    	ArrayList<HomeworkQuestion> results = new ArrayList<HomeworkQuestion>();
    	String apiUrl = this.dashboardApiBaseUrl + Constants.api_homework_question ;
    	logger.info("URL: "+ apiUrl);
    	
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
        logger.info("URL: "+ apiUrl);
        String jsonResponse = apiHelper.callAPI(apiUrl, usr);

        if (!jsonResponse.isEmpty()) {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode;
            try {
                jsonNode = objectMapper.readTree(jsonResponse);
                
                JsonNode questionIdNode = jsonNode.get("questionId");
                result.setQuestionId(questionIdNode != null ? questionIdNode.asInt() : 0);
                
                JsonNode questionAbbrNode = jsonNode.get("questionAbbr");
                result.setQuestionAbbreviation(questionAbbrNode != null ? questionAbbrNode.asText() : null);
                
                JsonNode questionNode = jsonNode.get("question");
                result.setQuestion(questionNode != null ? questionNode.asText() : null);
                
                JsonNode statusNode = jsonNode.get("status");
                result.setStatus(statusNode != null ? statusNode.asText() : null);
                
                JsonNode isRequiredNode = jsonNode.get("isRequired");
                result.setRequired(isRequiredNode != null && isRequiredNode.asBoolean());
                
                JsonNode questionTypeNode = jsonNode.get("questionType");
                result.setQuestionType(questionTypeNode != null ? questionTypeNode.asText() : null);
                
                JsonNode triggersProtocolCreationNode = jsonNode.get("triggersProtocolCreation");
                result.setTriggeringReponse(triggersProtocolCreationNode != null && triggersProtocolCreationNode.asBoolean());
                
                // Map triggeringResponse
                JsonNode triggeringResponseNode = jsonNode.get("triggeringResponse");
                if (triggeringResponseNode != null) {
                    HomeworkResponse triggerResponse = new HomeworkResponse();
                    JsonNode responseNode = triggeringResponseNode.get("response");
                    triggerResponse.setResponse(responseNode != null ? responseNode.asText() : null);
                    
                    JsonNode tooltipNode = triggeringResponseNode.get("tooltip");
                    triggerResponse.setTooltip(tooltipNode != null ? tooltipNode.asText() : null);
                    
                    result.setTriggerResponse(triggerResponse);
                }

                // Map triggerProtocolId
                JsonNode triggeredProtocolNode = jsonNode.get("triggeredProtocol");
                if (triggeredProtocolNode != null) {
                    JsonNode idNode = triggeredProtocolNode.get("id");
                    result.setTriggerProtocolId(idNode != null ? idNode.asInt() : 0);
                }

                // Map expectedHomeworkResponses
                JsonNode expectedResponsesNode = jsonNode.get("expectedHomeworkResponses");
                if (expectedResponsesNode != null) {
                    ExpectedHomeworkResponses expectedResponses = new ExpectedHomeworkResponses();
                    List<HomeworkResponse> responsesList = new ArrayList<>();
                    JsonNode responsesArray = expectedResponsesNode.get("responses");
                    if (responsesArray != null && responsesArray.isArray()) {
                        for (JsonNode responseNode : responsesArray) {
                            HomeworkResponse response = new HomeworkResponse();
                            JsonNode responseTextNode = responseNode.get("response");
                            response.setResponse(responseTextNode != null ? responseTextNode.asText() : null);
                            
                            JsonNode tooltipTextNode = responseNode.get("tooltip");
                            response.setTooltip(tooltipTextNode != null ? tooltipTextNode.asText() : null);
                            
                            responsesList.add(response);
                        }
                    }
                    JsonNode numOfResponsesNode = expectedResponsesNode.get("numOfResponses");
                    expectedResponses.setNumOfResponses(numOfResponsesNode != null ? numOfResponsesNode.asInt() : 0);
                    expectedResponses.setResponses(responsesList);
                    result.setExpectedHomeworkResponses(expectedResponses);
                }

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
    
    
    public int updateHomeworkQuestion(User usr, int id, HomeworkQuestion question) {
        int result = -1;
        String apiUrl = this.dashboardApiBaseUrl + Constants.api_homework_question_get + id;
        StringBuilder requestBody = new StringBuilder();
        requestBody.append("{")
            .append("\"questionAbbr\": \"").append(question.getQuestionAbbreviation()).append("\",")
            .append("\"question\": \"").append(question.getQuestion()).append("\",")
            .append("\"questionType\": \"").append(question.getQuestionType()).append("\",")
            .append("\"isRequired\": ").append(question.isRequired()).append(",")
            .append("\"responseOptions\": [");

        if (question.getExpectedHomeworkResponses() != null && question.getExpectedHomeworkResponses().getResponses() != null) {
            List<HomeworkResponse> responses = question.getExpectedHomeworkResponses().getResponses();
            for (int i = 0; i < responses.size(); i++) {
                HomeworkResponse response = responses.get(i);
                requestBody.append("{")
                    .append("\"response\": \"").append(response.getResponse()).append("\",")
                    .append("\"tooltip\": \"").append(response.getTooltip()).append("\"")
                    .append("}");
                if (i < responses.size() - 1) {
                    requestBody.append(",");
                }
            }
        }

        requestBody.append("],")
            .append("\"triggerProtocolCreation\": ").append(question.getIsTriggeringReponse()).append(",")
            .append("\"triggeringResponse\": {")
            .append("\"response\": \"").append(question.getTriggerResponse() != null ? question.getTriggerResponse().getResponse() : "").append("\",")
            .append("\"tooltip\": \"").append(question.getTriggerResponse() != null ? question.getTriggerResponse().getTooltip() : "").append("\"")
            .append("},");

        // Conditional check for triggeredProtocolId
        if (question.getTriggerProtocolId() > 0) {
            requestBody.append("\"triggeredProtocolId\": ").append(question.getTriggerProtocolId());
        } else {
            requestBody.append("\"triggeredProtocolId\": null");
        }

        requestBody.append("}");

        logger.info(requestBody.toString());
        result = apiHelper.patchAPI(apiUrl, requestBody.toString(), usr);
        return result;
    }


}