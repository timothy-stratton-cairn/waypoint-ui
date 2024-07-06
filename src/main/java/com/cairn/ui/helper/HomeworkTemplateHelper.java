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

    public ArrayList<HomeworkTemplate> getList(User usr) {
		ArrayList<HomeworkTemplate> results = new ArrayList<HomeworkTemplate>();
		
		if (usr == null) {
			return results;
		}

		String apiUrl = this.dashboardApiBaseUrl + Constants.api_homeworktemplate;
	    String jsonResponse = apiHelper.callAPI(apiUrl, usr);
		if (!jsonResponse.isEmpty()) {
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
			logger.info("Failed to fetch data. getList");
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

        String apiUrl = this.dashboardApiBaseUrl + Constants.api_homeworktemplate + "/" + id;
	    String jsonResponse = apiHelper.callAPI(apiUrl, usr);
		if (!jsonResponse.isEmpty()) {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode;
			try {
				rootNode = objectMapper.readTree(jsonResponse);
	            populateTemplateFromJson(rootNode, result);
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        } else {
            logger.info("Failed to fetch data. getTemplate(id)");
        }

        return result;
    }
    
    public String newTemplate(User usr, String templateBody) {
    	String result = "Process not yet set ";
    	String apiUrl = this.dashboardApiBaseUrl + Constants.api_homeworktemplate;
	    int response = apiHelper.postAPI(apiUrl, templateBody, usr);
		if (response == 1) {
			result = "Success";
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
    	String apiUrl = this.dashboardApiBaseUrl + Constants.api_homeworktemplate;
	    result = apiHelper.postAPI(apiUrl, requestBody, usr);
    	
    	return result;
    }
    
    public int deleteHomeworkTemplate(User usr, int homeworkTemplateId) {
    	int result = 0;
    	String apiUrl = this.dashboardApiBaseUrl + Constants.api_homeworktemplate_get + homeworkTemplateId;
    	logger.info(apiUrl);
        result = apiHelper.deleteAPI(apiUrl, usr);
        
    	
    	return result;
    }
    
    public int removeHomeworkTemplateFromStep(User usr, int homeworkTemplateId, int stepId ) {
    	int result = 0;
    	
    	String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocolsteptemplate_get +'/' + homeworkTemplateId;
    	logger.info(apiUrl);
        result = apiHelper.deleteAPI(apiUrl, usr);
        return result;
    }
    
    public ArrayList<HomeworkQuestion> getHomeworkQuestions(User usr){
    	ArrayList<HomeworkQuestion> results = new ArrayList<HomeworkQuestion>();
    	String apiUrl = this.dashboardApiBaseUrl + Constants.api_homework_question ;
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
			logger.info("Failed to fetch data. getHomeworkQuestions");
		}
    	
    	return results;
    	
    }
    
    public int newHomeworkQuestion(User usr, HomeworkQuestion question) {
        int result = 0;
        
        String apiUrl = this.dashboardApiBaseUrl + Constants.api_homework_question;
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


    
    
    
}