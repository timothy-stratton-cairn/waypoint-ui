package com.cairn.ui.helper;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cairn.ui.Constants;
import com.cairn.ui.model.HomeworkTemplate;
import com.cairn.ui.model.ProtocolStepTemplate;
import com.cairn.ui.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


@Service 
public class ProtocolStepTemplateHelper{
    Logger logger = LoggerFactory.getLogger(ProtocolTemplateHelper.class); 
    private APIHelper apiHelper = new APIHelper();

	@Value("${waypoint.dashboard-api.base-url}")
	private String dashboardApiBaseUrl;

    public ArrayList<ProtocolStepTemplate> getStepList(User usr) {
    	ArrayList<ProtocolStepTemplate> stepTemplateList = new ArrayList<ProtocolStepTemplate>();
	    String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocolsteptemplate;
		String jsonResponse = apiHelper.callAPI(apiUrl, usr);
		if (!jsonResponse.isEmpty()) {
            ObjectMapper objectMapper = new ObjectMapper();

            JsonNode rootNode;
			try {
				rootNode = objectMapper.readTree(jsonResponse);
	            if (rootNode.isArray()) {
	                for (JsonNode jsonNode : rootNode) {
	                    ProtocolStepTemplate template = new ProtocolStepTemplate();
	                    template.setName(jsonNode.get("name").asText());
	                    template.setDescription(jsonNode.get("description").asText());
	                    template.setId(jsonNode.get("id").asInt());
	                    template.setStatus(jsonNode.get("status").asText());
	                    
	                    JsonNode stepTemplateCategoryNode = jsonNode.path("category");
	                    if (!stepTemplateCategoryNode.isMissingNode() && !stepTemplateCategoryNode.path("id").isMissingNode()) {
	                        template.setCategoryId(stepTemplateCategoryNode.path("id").asInt());
	                        template.setCategoryName(stepTemplateCategoryNode.path("name").asText());
	                        template.setCategoryDescription(stepTemplateCategoryNode.path("description").asText());
	                    } else {
	                        template.setType(0); // Set type to 0 if "stepTemplateCategory" or "id" is missing.
	                    }
	                    
	                    JsonNode perms = jsonNode.get("linkedHomeworkTemplates");
	                    ArrayList<HomeworkTemplate> homeworks = new ArrayList<>();
	                    if (perms.isArray()) {
	                        for (JsonNode element : perms) {
	                            if (element != null) {
	                                HomeworkTemplate curHw = new HomeworkTemplate();
	                                curHw.setName(element.get("name").asText());
	                                curHw.setId(element.get("id").asInt());
	                                curHw.setDescription(element.get("description").asText());
	                                homeworks.add(curHw);
	                            }
	                        }
	                        template.setHomework(homeworks);
	                    }
	                    
	                    stepTemplateList.add(template);
	                }
	            }
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        } else {
            logger.info("Failed to fetch data. getStepList");
        }   	
    	
    	return stepTemplateList;
    }
    
    
   

    public int addStepTemplate(User usr, ProtocolStepTemplate newStep) {
        int result = -1;
        List<HomeworkTemplate> homeworkList = newStep.getHomework();
        StringBuilder homeworkIds = new StringBuilder("\"linkedHomeworkTemplateIds\":");
        
        if (homeworkList == null || homeworkList.isEmpty()) {
            homeworkIds.append("null"); 
        } else {
            homeworkIds.append("[");
            for (int i = 0; i < homeworkList.size(); i++) {
                HomeworkTemplate hw = homeworkList.get(i);
                homeworkIds.append(hw.getId());
                if (i < homeworkList.size() - 1) {
                    homeworkIds.append(", ");  // Append a comma and a space if not the last element
                }
            }
            homeworkIds.append("]");  // Close the array outside the loop
        }
        
        // Construct the final JSON string
        String requestBody = "{"
                + "\"name\": \"" + newStep.getName() + "\","
                + "\"description\": \"" + newStep.getDescription() + "\","
                + "\"linkedStepTaskId\": null,"  
                + homeworkIds.toString() + ","
                + "\"stepTemplateCategoryId\": " + newStep.getCategoryId() + "}";
        
        String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocolsteptemplate;
		String jsonResponse = apiHelper.postAPIResponse(apiUrl, requestBody, usr);
		if (!jsonResponse.isEmpty()) {		
			ObjectMapper objectMapper = new ObjectMapper();

			JsonNode jsonNode;
			try {
				jsonNode = objectMapper.readTree(jsonResponse);
	            if (jsonNode.has("stepTemplateId")) {
	                result = jsonNode.get("stepTemplateId").asInt();
	            } else {
	                result = -1; // Set to -1 if stepTemplateId is not present in the response
	            }
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        } else {
            result = -1;
            logger.info("Failed to Post data. addStepTemplate");
        }  
        logger.info("Add template response:" + result);
        return result;
    }
    
    
    public int addHomeworkTemplate(User usr, int stepTemplateId, int homeworkId) {
    	int result = 0;
    	
	    String requestBody = "{\"linkedHomeworkTemplateIds\": [" + homeworkId + "]}";
		String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocolsteptemplate_get + stepTemplateId ;
	    result = apiHelper.patchAPI(apiUrl, requestBody, usr);
    	return result;
    	
    }
    
    public int updateStepTemplate(User usr,int id ,ProtocolStepTemplate step) {
    	int result = -1;
		String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocolsteptemplate_get +"/"+ id ;
		logger.info(apiUrl);
		String requestBody = "{"
		    + "\"name\": \"" + step.getName() + "\","
		    + "\"description\": \"" + step.getDescription() + "\","
		    + "\"stepTemplateCategoryId\": " + step.getCategoryId() + "}";
		
		logger.info(requestBody);
		    
	    result = apiHelper.patchAPI(apiUrl, requestBody, usr);
    	return result;

    }
    
    
    public ProtocolStepTemplate getTemplate(User usr, int id) {
    	
    	
    	ProtocolStepTemplate result = new ProtocolStepTemplate();
	    String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocolsteptemplate_get + "/"+id;
	    String jsonResponse = apiHelper.callAPI(apiUrl, usr);
		if (!jsonResponse.isEmpty()) {
			ObjectMapper objectMapper = new ObjectMapper();

			JsonNode jsonNode;
			try {
				jsonNode = objectMapper.readTree(jsonResponse);
				result.setName(jsonNode.get("name").asText());
				result.setDescription(jsonNode.get("description").asText());
				result.setId(Integer.valueOf(jsonNode.get("id").toString()));
				JsonNode stepTemplateCategoryNode = jsonNode.path("category");
			    if (!stepTemplateCategoryNode.isMissingNode() && !stepTemplateCategoryNode.path("id").isMissingNode()) {
			        result.setCategoryId(stepTemplateCategoryNode.path("id").asInt());
			        result.setCategoryName(stepTemplateCategoryNode.path("name").asText());
			        result.setCategoryDescription(stepTemplateCategoryNode.path("description").asText());
			    } else {
			        result.setType(0); // Set type to 0 if "stepTemplateCategory" or "id" is missing.
			    }
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
						}
						else {
							logger.warn("No Homework Retrieved");
						}
					}
					logger.info(homeworks.toString());
					result.setHomework(homeworks);
				}
				} catch (JsonMappingException e) {
					e.printStackTrace();
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}
			} else {
				logger.error("Failed to fetch data. getTemplate");
			}
	    
    	return result;
    	
    }
    
    public int deleteStepTemplate(User usr, int stepTemplateId) {
    	int result = 0;
    	String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocolsteptemplate_get + stepTemplateId;
    	result = apiHelper.deleteAPI(apiUrl,usr);
    	
    	return result;
    }
    
    
    public int deleteHomeworkTemplate(User usr, int stepId, int homeworkId) {
    	int result = -1;
		String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocolsteptemplate_get + stepId + "/homework-template?homeworkTemplateId="+homeworkId ;
    	result = apiHelper.deleteAPI(apiUrl,usr);
    	return result;
    }
    
    public int changeStatus(User usr, int Id, String status) {
    	int result = -1;

	    String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocoltemplateget + Id;
	    
	    StringBuilder requestBodyBuilder = new StringBuilder();
	    requestBodyBuilder.append("{");
	    requestBodyBuilder.append("\"status\": \"").append(status).append("\"");
	    requestBodyBuilder.append("}");
	    String requestBody = requestBodyBuilder.toString();
	    logger.info("apiUrl: "+ apiUrl);
	    logger.info("requestBody: " + requestBody);
	    result = apiHelper.patchAPI(apiUrl, requestBody, usr);
	    
	    return result;
    	
    }
    
}