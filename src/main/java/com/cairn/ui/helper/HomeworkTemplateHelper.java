package com.cairn.ui.helper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.cairn.ui.Constants;
import com.cairn.ui.model.Entity;
import com.cairn.ui.model.ExpectedHomeworkResponses;
import com.cairn.ui.model.HomeworkQuestion;
import com.cairn.ui.model.HomeworkTemplate;
import com.cairn.ui.model.ProtocolStepTemplate;
import com.cairn.ui.model.HomeworkTemplate;
import com.cairn.ui.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.oauth2.sdk.Response;

@Service
public class HomeworkTemplateHelper{

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
				System.out.println("Failed to fetch data. Status code: " + response.getStatusCode());
			}
		} catch (Exception e) {
			System.out.println("No Homeworks returned");
		}

		return results;

	}

    public class HomeworkTemplateService {

        public HomeworkTemplate getTemplate(User usr, int id) {
            HomeworkTemplate result = new HomeworkTemplate();

            String apiUrl = "http://96.61.158.12:8083" + Constants.api_homeworktemplate + "/" + id;
            HttpEntity<String> entity = Entity.getEntity(usr, apiUrl);
            System.out.println(apiUrl);

            try {
                ResponseEntity<String> response = new RestTemplate().exchange(apiUrl, HttpMethod.GET, entity, String.class);
                if (response.getStatusCode().is2xxSuccessful()) {
                    String jsonResponse = response.getBody();
                    ObjectMapper objectMapper = new ObjectMapper();

                    JsonNode jsonNode = objectMapper.readTree(jsonResponse);
                    result.setId(jsonNode.get("id").asInt());
                    result.setName(jsonNode.get("name").asText());
                    result.setDescription(jsonNode.get("description").asText());

                    JsonNode questionsNode = jsonNode.path("homeworkQuestions").path("questions");
                    List<HomeworkQuestion> questions = new ArrayList<>();
                    for (JsonNode questionNode : questionsNode) {
                        HomeworkQuestion question = new HomeworkQuestion();
                        question.setQuestionAbbreviation(questionNode.path("questionAbbreviation").asText());
                        question.setQuestion(questionNode.path("question").asText());
                        question.setQuestionType(questionNode.path("questionType").asText());
                        question.setRequired(questionNode.path("isRequired").asBoolean());

                        JsonNode responsesNode = questionNode.path("expectedHomeworkResponses").path("responses");
                        List<Response> responses = new ArrayList<>();
                        for (JsonNode responseNode : responsesNode) {
                            Response response1 = new Response();
                            response1.setReponse(responseNode.path("response").asText());
                            response1.setTooltip(responseNode.path("tooltip").asText());
                            responses.add(response1);
                        }

                        ExpectedHomeworkResponses expectedResponses = new ExpectedHomeworkResponses();
                        expectedResponses.setResponses(responses);
                        expectedResponses.setNumOfResponses(questionNode.path("expectedHomeworkResponses").path("numOfResponses").asInt());

                        question.setExpectedHomeworkResponses(expectedResponses);
                        questions.add(question);
                    }
                    result.setQuestions(questions);
                } else {
                    System.out.println("Failed to fetch data. Status code: " + response.getStatusCode());
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                System.out.println("Error processing JSON");
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
            }

            return result;
        }
    }
}