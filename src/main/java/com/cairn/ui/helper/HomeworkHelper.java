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
public class HomeworkHelper {
    Logger logger = LoggerFactory.getLogger(HomeworkHelper.class); 
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

	public ArrayList<Homework> getHomeworkByProtocolId(User usr, int protocolId) {

		ArrayList<Homework> results = new ArrayList<Homework>();
		if (usr == null) {
			logger.info("No User found");
			return results;
		}

		String apiUrl = this.dashboardApiBaseUrl + Constants.api_homework + "protocol/" + protocolId;
		HttpEntity<String> entity = Entity.getEntity(usr, apiUrl);
		logger.info(apiUrl);
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
				logger.info("Failed to fetch data. Status code: " + response.getStatusCode());
			}
		} catch (Exception e) {

			logger.info("No Homeworks Returned");

		}
		return results;
	}

	public Homework getHomeworkByProtocol(User usr, int id) {

		Homework result = new Homework();

		String apiUrl = this.dashboardApiBaseUrl + Constants.api_homework + "protocol/" + id;

		logger.info(apiUrl);

		HttpEntity<String> entity = Entity.getEntity(usr, apiUrl);
		try {
			ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.GET, entity, String.class);
			// Process the response
			if (response.getStatusCode().is2xxSuccessful()) {
				String jsonResponse = response.getBody();
				ObjectMapper objectMapper = new ObjectMapper();
				result = objectMapper.readValue(jsonResponse, Homework.class);

			} else {
				logger.info("Failed to fetch data. Status code: " + response.getStatusCode());
			}
		} catch (Exception e) {
			logger.info("No Testing");
		}

		return result;
	}
	
	public ArrayList<Homework> getHomeworkByUser(User usr, int protocolId) {

		ArrayList<Homework> results = new ArrayList<Homework>();
		if (usr == null) {
			logger.info("No User found");
			return results;
		}

		String apiUrl = this.dashboardApiBaseUrl + Constants.api_homework + "household/" + protocolId;
		HttpEntity<String> entity = Entity.getEntity(usr, apiUrl);
		logger.info(apiUrl);
		// Make the GET request and retrieve the response
		try {
			ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.GET, entity, String.class);
			// Process the response
            if (response.getStatusCode().is2xxSuccessful()) {
                String jsonResponse = response.getBody();
                ObjectMapper objectMapper = new ObjectMapper();

                try {
                    JsonNode jsonNode = objectMapper.readTree(jsonResponse);
                    JsonNode hwork = jsonNode.get("homeworks");

                    // Iterate through the array elements
                    if (hwork != null && hwork.isArray()) {
                        for (JsonNode element : hwork) {
                            if (element != null) {
                                Homework entry = new Homework();
                                entry.setId(element.get("homeworkId").asInt());
                                entry.setName(element.get("name").asText());
                                entry.setDescription(element.has("description") && !element.get("description").isNull()
                                        ? element.get("description").asText()
                                        : "No Description Given");

                                JsonNode homeworkQuestionsNode = element.get("homeworkQuestions");
                                ArrayList<HomeworkQuestion> homeworkQuestions = new ArrayList<>();
                                
                                if (homeworkQuestionsNode != null && homeworkQuestionsNode.isArray()) {
                                    for (JsonNode questionNode : homeworkQuestionsNode) {
                                        HomeworkQuestion curQuestion = new HomeworkQuestion();
                                        curQuestion.setQuestionId(questionNode.path("questionId").asInt());
                                        curQuestion.setQuestionAbbreviation(questionNode.path("questionAbbr").asText());
                                        curQuestion.setQuestion(questionNode.path("question").asText());
                                        curQuestion.setQuestionType(questionNode.path("questionType").asText());
                                        curQuestion.setRequired(questionNode.path("isRequired").asBoolean());
                                        curQuestion.setUserResponse(questionNode.path("userResponse").asText());

                                        homeworkQuestions.add(curQuestion);
                                    }
                                } else {
                                    logger.info("No 'homeworkQuestions' array found in the JSON response.");
                                }
                                entry.setQuestions(homeworkQuestions);

                                results.add(entry);
                            }
                        }
                    } else {
                        logger.info("No 'homeworks' array found in the JSON response.");
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

	public Homework getHomeworkByHomeworkId(User usr, int id) {
		Homework result = new Homework();
		if (usr == null) {
			logger.info("No User found");
			return null; // Returning null to indicate user not found, handle accordingly
		}

		String apiUrl = this.dashboardApiBaseUrl + Constants.api_homework + id;
		HttpEntity<String> entity = Entity.getEntity(usr, apiUrl);
		logger.info(apiUrl);

		try {
			ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.GET, entity, String.class);
			// Process the response
			if (response.getStatusCode().is2xxSuccessful()) {
				String jsonResponse = response.getBody();
				logger.info("API response: " + jsonResponse);
				ObjectMapper objectMapper = new ObjectMapper();

				JsonNode jsonNode;
				try {
					jsonNode = objectMapper.readTree(jsonResponse);
					result.setName(jsonNode.get("name").asText());
					result.setId(jsonNode.get("homeworkId").asInt());
					result.setDescription(jsonNode.get("description").asText());

					JsonNode homeworkQuestionsNode = jsonNode.get("homeworkQuestions");
					ArrayList<HomeworkQuestion> homeworkQuestion = new ArrayList<HomeworkQuestion>();
					if (!homeworkQuestionsNode.isNull()) {
						JsonNode questionsNode = jsonNode.path("homeworkQuestions").path("questions");
						if (questionsNode.isArray()) {
							for (JsonNode questionNode : questionsNode) {
								HomeworkQuestion curQuestion = new HomeworkQuestion();

								curQuestion.setQuestionId(questionNode.path("questionId").asInt());

								curQuestion.setQuestionAbbreviation(questionNode.path("questionAbbr").asText());

								curQuestion.setQuestion(questionNode.path("question").asText());

								curQuestion.setQuestionType(questionNode.path("questionType").asText());

								curQuestion.setRequired(questionNode.path("isRequired").asBoolean());

								curQuestion.setUserResponse(questionNode.path("userResponse").asText());

								JsonNode expectedHomeworkResponsesNode = questionNode.get("expectedHomeworkResponses");
								ArrayList<HomeworkResponse> homeworkQuestionExpectedResponses = new ArrayList<>();
								if (!expectedHomeworkResponsesNode.isNull()) {
									JsonNode expectedResponses = questionNode.path("expectedHomeworkResponses").path("responses");
									if (!expectedResponses.isNull()) {
										for (JsonNode responseNode : expectedResponses) {
											HomeworkResponse curResponse = new HomeworkResponse();
											curResponse.setResponse(responseNode.path("response").asText());
											curResponse.setTooltip(responseNode.path("tooltip").asText());
											homeworkQuestionExpectedResponses.add(curResponse);
										}
									}

									ExpectedHomeworkResponses expectedHomeworkResponses = new ExpectedHomeworkResponses();
									expectedHomeworkResponses.setResponses(homeworkQuestionExpectedResponses);
									expectedHomeworkResponses.setNumOfResponses(questionNode.path("expectedHomeworkResponses").path("numOfResponses").intValue());

									curQuestion.setExpectedHomeworkResponses(expectedHomeworkResponses);
								}


								homeworkQuestion.add(curQuestion);

							}
						} else {
							logger.info("No 'questions' array found in the JSON response.");
						}
					}
					result.setQuestions(homeworkQuestion);

				} catch (Exception e) {
					e.printStackTrace();
					logger.info("Error " + e);
				}
			} else {
				logger.info("Failed to fetch data. Status code: " + response.getStatusCode());
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("Error processing the homework fetch request: " + e.getMessage());
		}
		return result; // Return null to indicate that no homework was fetched
	}

	public int submitHomeworkResponses(User user, int homeworkId,
			AssignedHomeworkResponseList homeworkResponses, List<MultipartFile> files)
      throws IOException {
		final String apiUrl = this.dashboardApiBaseUrl + Constants.api_homework + homeworkId;
		MultiValueMap<String, Object> multipartRequest = new LinkedMultiValueMap<>();

		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

		requestHeaders.setBearerAuth(user.getAuthToken());

		HttpHeaders requestHeadersAttachment = new HttpHeaders();

		requestHeadersAttachment.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		HttpEntity<ByteArrayResource> attachmentPart;

		if (files != null && !files.isEmpty()) {
			for (MultipartFile file : files) {
				try (InputStream is = file.getInputStream()) {
					ByteArrayResource fileAsResource = new ByteArrayResource(is.readAllBytes()) {
						@Override
						public String getFilename() {return file.getOriginalFilename();}
					};

					attachmentPart = new HttpEntity<>(fileAsResource, requestHeadersAttachment);
					multipartRequest.add("files", attachmentPart);
				}
			}
		} else {
			attachmentPart = new HttpEntity<>(new ByteArrayResource(new byte[0]));
			multipartRequest.add("files", attachmentPart);
		}

		HttpHeaders requestHeadersJSON = new HttpHeaders();
		requestHeadersJSON.setContentType(MediaType.APPLICATION_JSON);
		String requestBody = new ObjectMapper().writeValueAsString(homeworkResponses);
		HttpEntity<String> requestEntityJSON = new HttpEntity<>(requestBody, requestHeadersJSON);

		multipartRequest.set("json", requestEntityJSON);
		HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(multipartRequest, requestHeaders);// final request

		ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.PATCH, requestEntity, String.class);

		return response.getStatusCode().is2xxSuccessful() ? 1 : -1;
	}

	public int assignAnswerToHomework(User usr, int homeworkId, int questionId, String userResponse, String filePath)
			throws IOException {
		final String apiUrl = this.dashboardApiBaseUrl + Constants.api_homework + homeworkId;
		MultiValueMap<String, Object> multipartRequest = new LinkedMultiValueMap<>();

		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

		requestHeaders.setBearerAuth(usr.getAuthToken());

		HttpHeaders requestHeadersAttachment = new HttpHeaders();

		requestHeadersAttachment.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		HttpEntity<ByteArrayResource> attachmentPart;

		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(filePath)) {
			final String filename = filePath.split("/")[filePath.split("/").length - 1];
			ByteArrayResource fileAsResource = new ByteArrayResource(
					filePath == null || filePath.isEmpty() ? new byte[0]
							: Objects.requireNonNull(is).readAllBytes()) {

				@Override
				public String getFilename() {
					return filename;
				}
			};

			logger.info("File Path: " + filePath + " fileAsResource: " + fileAsResource);
			attachmentPart = new HttpEntity<>(fileAsResource, requestHeadersAttachment);

			multipartRequest.set("files", attachmentPart);
		}

		HttpHeaders requestHeadersJSON = new HttpHeaders();
		requestHeadersJSON.setContentType(MediaType.APPLICATION_JSON);
		String requestBody = "{\"responses\": [{\"questionId\": " + questionId + ", \"userResponse\": \"" + userResponse.split(
				Pattern.quote("\\"))[userResponse.split(Pattern.quote("\\")).length-1]
				+ "\"}]}";
		HttpEntity<String> requestEntityJSON = new HttpEntity<>(requestBody, requestHeadersJSON);

		multipartRequest.set("json", requestEntityJSON);
		HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(multipartRequest, requestHeaders);// final request

		ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.PATCH, requestEntity, String.class);

		return response.getStatusCode().is2xxSuccessful() ? 1 : -1;
	}
	
	public int assigneFileUpload(User usr, int homeworkId, int questionId, String userResponse, MultipartFile file)
	        throws IOException {
		logger.info("Calling AssignAnswerToHomeworkTest");
		logger.info("File Uploaded: " + file.getOriginalFilename() + " User Response: "+ userResponse);
	    final String apiUrl = this.dashboardApiBaseUrl + Constants.api_homework + homeworkId;
	    MultiValueMap<String, Object> multipartRequest = new LinkedMultiValueMap<>();

	    HttpHeaders requestHeaders = new HttpHeaders();
	    requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

	    requestHeaders.setBearerAuth(usr.getAuthToken());

	    // Add the file as a part of the multipart request
	    byte[] fileBytes = file.getBytes();
	    ByteArrayResource fileAsResource = new ByteArrayResource(fileBytes) {
	        @Override
	        public String getFilename() {
	            return file.getOriginalFilename();
	        }
	    };
	    HttpHeaders requestHeadersAttachment = new HttpHeaders();
	    requestHeadersAttachment.setContentType(MediaType.APPLICATION_OCTET_STREAM);
	    HttpEntity<ByteArrayResource> attachmentPart = new HttpEntity<>(fileAsResource, requestHeadersAttachment);
	    multipartRequest.add("files", attachmentPart);

	    // Construct the JSON body containing the user response data
	    String requestBody = "{\"responses\": [{\"questionId\": " + questionId + ", \"userResponse\": \"" + userResponse + "\"}]}";
	    HttpHeaders requestHeadersJSON = new HttpHeaders();
	    requestHeadersJSON.setContentType(MediaType.APPLICATION_JSON);
	    HttpEntity<String> requestEntityJSON = new HttpEntity<>(requestBody, requestHeadersJSON);
	    multipartRequest.add("json", requestEntityJSON);

	    // Create the final HTTP request entity with the multipart request and headers
	    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(multipartRequest, requestHeaders);

	    // Execute the HTTP PATCH request
	    ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.PATCH, requestEntity, String.class);

	    return response.getStatusCode().is2xxSuccessful() ? 1 : -1;
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

    public int deleteHomeworkQuestion(User usr, int homeworkQuestionId) {
    	int result = -1;
    	String apiUrl = this.dashboardApiBaseUrl + Constants.api_homework_question_get + homeworkQuestionId;
    	result = apiHelper.deleteAPI(apiUrl,usr);
        
    	
    	return result;
    }
    public int deleteHomework(User usr, int homeworkId) {
    	int result = -1;
    	String apiUrl = this.dashboardApiBaseUrl + Constants.api_homework + homeworkId;
    	result = apiHelper.deleteAPI(apiUrl,usr);    	
    	
    	return result;
    }
    
    

}