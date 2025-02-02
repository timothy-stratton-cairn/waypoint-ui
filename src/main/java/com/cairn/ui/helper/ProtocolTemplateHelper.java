package com.cairn.ui.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cairn.ui.Constants;
import com.cairn.ui.model.HomeworkTemplate;
import com.cairn.ui.model.ProtocolStepTemplate;
import com.cairn.ui.model.ProtocolTemplate;
import com.cairn.ui.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ProtocolTemplateHelper {
    Logger logger = LoggerFactory.getLogger(ProtocolTemplateHelper.class); 
    private APIHelper apiHelper = new APIHelper();

	@Value("${waypoint.dashboard-api.base-url}")
	private String dashboardApiBaseUrl;

	/**
	 * Get a list of step templates not assigned to the protocol template.
	 * 
	 * @param usr a User object that is the logged in user. The token from the instance is used to 
	 * authenticate the API call
	 * @param theTemplate A protocol template instance we want to check.
	 * @param type The type of steps to return.
	 * @return ArrayList of protocol steps that we could assign to the protocol.
	 */
	public ArrayList<ProtocolStepTemplate> availableSteps(User usr,ProtocolTemplate theTemplate, int type) {
		ArrayList<ProtocolStepTemplate> results = new ArrayList<ProtocolStepTemplate>();
		
		if (usr == null) {
			return results;
		}

		String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocolsteptemplate;
		String jsonResponse = apiHelper.callAPI(apiUrl, usr);
		if (!jsonResponse.isEmpty()) {
			ObjectMapper objectMapper = new ObjectMapper();

			JsonNode jsonNode;
			try {
				jsonNode = objectMapper.readTree(jsonResponse);
				JsonNode prots = jsonNode.get("stepTemplates");
				// Iterate through the array elements
				ProtocolStepTemplate entry = null;
				if (prots.isArray()) {
					for (JsonNode element : prots) {
						// Access and print array elements
						if (element != null) {
							entry = new ProtocolStepTemplate();
							entry.setName(element.get("name").asText());
							entry.setId(Integer.valueOf(element.get("id").toString()));
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
			logger.warn("Failed to fetch available steps data.");
		}

		return results;
	}
	
	public int newProtocolTemplate (User usr, ProtocolTemplate template) {
		int result = -1;
		String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocoltemplate;
		
		StringBuilder associatedStepTemplateIds = new StringBuilder("\"associatedStepTemplateIds\":[");
		ArrayList<ProtocolStepTemplate>stepList = template.getSteps();

		for (int i = 0; i < stepList.size(); i++) {
			ProtocolStepTemplate step = stepList.get(i);
			associatedStepTemplateIds.append(step.getId());
			if (i < stepList.size() - 1) {
				associatedStepTemplateIds.append(", ");  // Append a comma and a space if not the last element
            }
		}
		associatedStepTemplateIds.append(']');
	    String requestBody = "{" +
	            "\"name\": \"" + template.getName() + "\"," +
	            "\"description\": \"" + template.getDescription() + "\"," +
	            "\"defaultDueByInDays\": \"" + template.getDueByDay() + "\"," +
	            "\"defaultDueByInMonths\": \"" + template.getDueByMonth() + "\"," +
	            "\"defaultDueByInYears\": \"" + template.getDueByYear() + "\"," +
	            "\"templateCategoryValue\":\"" + template.getType() + "\"," +
	            associatedStepTemplateIds.toString() + "," +
	            "\"recurrenceTypeValue\": \"MANUAL\"," +  // hard coding these in for now 
	            "\"defaultTriggeringStatusValue\": null," +  // hard coding these in for now 
	            "\"defaultReoccurInYears\": " + template.getYearSchedule() + "," +
	            "\"defaultReoccurInMonths\": " + template.getMonthSchedule() + "," +
	            "\"defaultReoccurInDays\": " + template.getDaySchedule() +
	            "}";
	    
	    logger.info(requestBody);
		result = apiHelper.postAPI(apiUrl, requestBody, usr);
		return result;
	}

	//same as above but instead grabbing all types 
    public ArrayList<ProtocolStepTemplate> getAllSteps(User usr) {
        ArrayList<ProtocolStepTemplate> results = new ArrayList<ProtocolStepTemplate>();

        if (usr == null) {
            return results;
        }

        String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocolsteptemplate;
        // Create a HttpEntity with the headers
        String jsonResponse = apiHelper.callAPI(apiUrl, usr);
        if (!jsonResponse.isEmpty()) {
            ObjectMapper objectMapper = new ObjectMapper();

            JsonNode jsonNode;
            try {
                jsonNode = objectMapper.readTree(jsonResponse);
                JsonNode prots = jsonNode.get("stepTemplates");
                // Iterate through the array elements
                if (prots.isArray()) {
                    int idx = 1;
                    for (JsonNode element : prots) {
                        // Access and print array elements
                        if (element != null) {
                            ProtocolStepTemplate entry = new ProtocolStepTemplate();
                            entry.setName(element.get("name").asText());
                            entry.setId(element.get("id").asInt());
                            entry.setDescription(element.get("description").asText());
                            entry.setStatus(element.get("status").asText());

                            // Set category information
                            JsonNode category = element.get("category");
                            if (category != null) {
                                entry.setCategoryId(category.get("id").asInt());
                                entry.setCategoryName(category.get("name").asText());
                                entry.setCategoryDescription(category.get("description").asText());
                            }

                            // Test data, fix this later
                            entry.setType(idx++);
                            if (idx > 4) {
                                idx = 1;
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
            logger.info("Failed to fetch getAllSteps data.");
        }

        return results;
    }

	public Map<String,String> getStepTypes() {
        HashMap<String, String> retVal = new HashMap<String, String>();
        retVal.put("0", "-- Select A Value --");
        retVal.put("1", "Gather Data");
        retVal.put("2", "Run Analysis");
        retVal.put("3", "Craft Recommendation");
        retVal.put("4", "Share Education");

        return retVal;
	}

	/**
	 * Save a protocol template
	 * 
	 * @param usr a User object that is the logged in user. The token from the instance is used to 
	 * authenticate the API call
	 * @param theTemplate A protocol template instance we want to save (create or update).
	 * @return int. Return code is the protocol id on successful save or less than 1 for an error.
	 */
	public int saveProtocolTemplate(User usr, int tempId, String requestBody) {

		int result = -1;
	    String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocoltemplate + "/" + tempId;
	    result = apiHelper.patchAPI(apiUrl, requestBody, usr);
		
		return result;
	}

	public int updateProtocolTemplateName(User usr, int tempId, String name) {
		int result = -1;
		String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocoltemplateget + tempId;
		String requestBody = "{\"name\": \""+name+"\"}";
		
	    result = apiHelper.patchAPI(apiUrl, requestBody, usr);
	
		return result;
		
	}
	
	public int updateProtocolTemplateDescription(User usr, int tempId, String description) {
		int result = -1;
		
		String apiUrl = this.dashboardApiBaseUrl +  Constants.api_ep_protocoltemplateget + tempId;
		String requestBody = "{\"description\": \""+description+"\"}";
	    result = apiHelper.patchAPI(apiUrl, requestBody, usr);
		return result;
		
	}
	public int updateProtocolTemplateDueDate(User usr, int tempId, int years, int months, int days) {
		int result = -1;
		
		String apiUrl = this.dashboardApiBaseUrl +  Constants.api_ep_protocoltemplateget + tempId;
		String requestBody = "{" +
				"\"defaultDueByInYears\": " + years + "," +
				"\"defaultDueByInMonths\": " + months + "," +
	            "\"defaultDueByInDays\": " + days +
		"}";
		logger.info(apiUrl);
		logger.info(requestBody);
	    result = apiHelper.patchAPI(apiUrl, requestBody, usr);
		return result;
		
	}
	public int updateProtocolTemplateScheduleDate(User usr, int tempId, int years, int months, int days) {
		int result = -1;
		
		String apiUrl = this.dashboardApiBaseUrl +  Constants.api_ep_protocoltemplateget + tempId;
		String requestBody = "{" +
				  "\"defaultProtocolRecurrence\": {" +
				    "\"recurrenceType\": \"MANUAL\"," +
				    "\"defaultTriggeringStatus\": null," +
				    "\"defaultReoccurInYears\": " + years + "," +
				    "\"defaultReoccurInMonths\": " + months + "," +
				    "\"defaultReoccurInDays\": " + days +
				  "}" +
				"}";


		logger.info(apiUrl);
		logger.info(requestBody);
	    result = apiHelper.patchAPI(apiUrl, requestBody, usr);
		return result;
		
	}
	public int updateProtocolTemplateStatus(User usr, int tempId, String status) {
		int result = -1;
		
		String apiUrl = this.dashboardApiBaseUrl +  Constants.api_ep_protocoltemplateget + tempId;
		String requestBody = "{\"status\": \""+status+"\"}";
		logger.info("URL: "+ apiUrl);
		logger.info("requestBody:"+ requestBody);
	    result = apiHelper.patchAPI(apiUrl, requestBody, usr);
		return result;
		
	}

	public int updateProtocolTemplate(User usr, int tempId, ProtocolTemplate newTemp) {
	    int result = -1;

	    String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocoltemplateget + tempId;
	    
	    StringBuilder requestBodyBuilder = new StringBuilder();
	    requestBodyBuilder.append("{");
	    
	    if (newTemp.getName() != null) {
	        requestBodyBuilder.append("\"name\": \"").append(newTemp.getName()).append("\",");
	    }
	    if (newTemp.getDescription() != null) {
	        requestBodyBuilder.append("\"description\": \"").append(newTemp.getDescription()).append("\",");
	    }
	    if (newTemp.getStatus() != null) {
	        requestBodyBuilder.append("\"status\": \"").append(newTemp.getStatus()).append("\",");
	    }
		if (newTemp.getType() != null) {
	        requestBodyBuilder.append("\"templateCategoryValue\": \"").append(newTemp.getType()).append("\",");
	    }
	    if (newTemp.getDueByYear() > 0) {
	        requestBodyBuilder.append("\"defaultDueByInYears\": ").append(newTemp.getDueByYear()).append(",");
	    }
	    if (newTemp.getDueByMonth() > 0) {
	        requestBodyBuilder.append("\"defaultDueByInMonths\": ").append(newTemp.getDueByMonth()).append(",");
	    }
	    if (newTemp.getDueByDay() > 0) {
	        requestBodyBuilder.append("\"defaultDueByInDays\": ").append(newTemp.getDueByDay()).append(",");
	    }
	    //if (newTemp.getType() != null) {
	    //    requestBodyBuilder.append("\"recurrenceTypeValue\": \"").append(newTemp.getType()).append("\",");
	    //}
	    if (newTemp.getSchedule() != null) {
	        requestBodyBuilder.append("\"defaultTriggeringStatusValue\": \"").append(newTemp.getSchedule()).append("\",");
	    }
	    if (newTemp.getYearSchedule() > 0) {
	        requestBodyBuilder.append("\"defaultReoccurInYears\": ").append(newTemp.getYearSchedule()).append(",");
	    }
	    if (newTemp.getMonthSchedule() > 0) {
	        requestBodyBuilder.append("\"defaultReoccurInMonths\": ").append(newTemp.getMonthSchedule()).append(",");
	    }
	    if (newTemp.getDaySchedule() > 0) {
	        requestBodyBuilder.append("\"defaultReoccurInDays\": ").append(newTemp.getDaySchedule()).append(",");
	    }

	    // Remove the trailing comma if present
	    if (requestBodyBuilder.charAt(requestBodyBuilder.length() - 1) == ',') {
	        requestBodyBuilder.deleteCharAt(requestBodyBuilder.length() - 1);
	    }
	    
	    requestBodyBuilder.append("}");

	    String requestBody = requestBodyBuilder.toString();
	    
	    logger.info(apiUrl);
	    logger.info(requestBody);
	    
	    result = apiHelper.patchAPI(apiUrl, requestBody, usr);
	    return result;
	}

	/**
	 * Save a protocol template
	 * 
	 * @param usr a User object that is the logged in user. The token from the instance is used to 
	 * authenticate the API call
	 * @param theStep A protocol template step instance we want to save (create or update).
	 * @return int. Return code is the step id on successful save or less than 1 for an error.
	 */
	public int saveTemplateStep(User usr, int tempId, String requestBody ) {
		int result = -1;

	    // Prepare the request body
	    String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocolsteptemplate_get;
	    result = apiHelper.patchAPI(apiUrl, requestBody, usr);
	    
		return result;
	}

	/**
	 * Save a protocol template step to a protocol template.
	 * 
	 * @param usr a User object that is the logged in user. The token from the instance is used to 
	 * authenticate the API call
	 * @param theTemplate A protocol template instance we want to add the step to.
	 * @param theStep A protocol template step instance we want to add to the template.
	 * @return int. Return code is 1 on successful addition or less than 1 for an error. 0 if the step is already assigned to the template.
	 */
	public int addTemplateStep(User usr, int theTemplate, int theStep) {
	    // Initial result indicating failure
	    int result = -1;

	    String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocolsteptemplate_assign + theTemplate + "/"+ theStep;
	    logger.info(apiUrl);
	    result = apiHelper.patchAPI(apiUrl, null, usr);

	    return result;
	}

	/**
	 * Remove a protocol template step from a protocol template.
	 * 
	 * @param usr a User object that is the logged in user. The token from the instance is used to 
	 * authenticate the API call
	 * @param theTemplate A protocol template instance we want to remove the step from.
	 * @param theStep A protocol template step instance we want to remove from the template.
	 * @return int. Return code is 1 on successful remove or less than 1 for an error. 0 if the step was not assigned to the template.
	 */
	
	
	public int removeTemplateStep(User usr, int tempId, ArrayList<ProtocolStepTemplate >steps) {
		int result = 0;
	    
		String requestBody = "{\"associatedSteps\":[" + "{";
		for (ProtocolStepTemplate step : steps) {
			
			String bodyString = "{\"id\": " + step.getId() + ", " +
	                "\"name\": \"" + step.getName() + "\", " +
	                "\"description\": \"" + step.getDescription() + "\", " +
	                "\"category\": {" +
	                "\"id\": " + step.getCategoryId() + ", " +
	                "\"name\": \"" + step.getCategoryName() + "\", " +
	                "\"description\": \"" + step.getCategoryDescription() + "\"" +
	                "}}";
			
			requestBody = requestBody+bodyString;
		}

		String apiUrl = this.dashboardApiBaseUrl +  Constants.api_ep_protocoltemplateget + tempId;
	    result = apiHelper.patchAPI(apiUrl, requestBody, usr);
		
		return result;
	}

	
	/**
	 * Get an individual step Template.
	 * 
	 * @param usr User object that is the logged in user. The token from the instance is used to 
	 * authenticate the API call
	 * @param id Step Id used to retrieve the step template 
	 * @return
	 */
	public ProtocolStepTemplate getStep(User usr, int id) {
		ProtocolStepTemplate result = new ProtocolStepTemplate();

		// Prepare the request body

		String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocolsteptemplate_get + id;
		String jsonResponse = apiHelper.callAPI(apiUrl, usr);
		if (!jsonResponse.isEmpty()) {
			ObjectMapper objectMapper = new ObjectMapper();

			JsonNode jsonNode;
			try {
				jsonNode = objectMapper.readTree(jsonResponse);
				result.setName(jsonNode.get("name").asText());
				result.setDescription(jsonNode.get("description").asText());
				result.setId(Integer.valueOf(jsonNode.get("id").toString()));
				result.setStatus(jsonNode.get("status").asText());
			    JsonNode stepTemplateCategoryNode = jsonNode.path("category");
			    if (!stepTemplateCategoryNode.isMissingNode() && !stepTemplateCategoryNode.path("id").isMissingNode()) {
			        result.setCategoryId(stepTemplateCategoryNode.path("id").asInt());
			        result.setCategoryName(stepTemplateCategoryNode.path("name").asText());
			        result.setCategoryDescription(stepTemplateCategoryNode.path("description").asText());
			    } else {
			        result.setType(0); // Set type to 0 if "stepTemplateCategory" or "id" is missing.
			    }
			    JsonNode linkedHomeworkTemplatesNode = jsonNode.get("linkedHomeworkTemplates");
			    ArrayList<HomeworkTemplate> homeworks = new ArrayList<HomeworkTemplate>();
			    if (linkedHomeworkTemplatesNode != null) {
			        JsonNode homeworkTemplatesNode = linkedHomeworkTemplatesNode.get("homeworkTemplates");
			        if (homeworkTemplatesNode != null && homeworkTemplatesNode.isArray()) {
			            for (JsonNode element : homeworkTemplatesNode) {
			                // Check if the current element is not null
			                if (element != null) {
			                    HomeworkTemplate curHw = new HomeworkTemplate();
			                    curHw.setName(element.get("name").asText());
			                    curHw.setId(Integer.parseInt(element.get("id").asText()));
			                    homeworks.add(curHw);
			                }
			            }
			        }
			    }
			    result.setHomework(homeworks);


			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		} else {
			logger.info("Failed to fetch data getStep");
		}

		return result;
	}
	/**
	 * Get a list of protocol templates
	 * 
	 * @param usr a User object that is the logged in user. The token from the instance is used to 
	 * authenticate the API call
	 * @return ArrayList of ProtocolTemplates. Empty list on error.
	 */
	public ArrayList<ProtocolTemplate> getList(User usr) {
		ArrayList<ProtocolTemplate> results = new ArrayList<ProtocolTemplate>();
		
		if (usr == null) {
			return results;
		}

		String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocoltemplate;
		String jsonResponse = apiHelper.callAPI(apiUrl, usr);
		if (!jsonResponse.isEmpty()) {
			ObjectMapper objectMapper = new ObjectMapper();

			JsonNode jsonNode;
			try {
				jsonNode = objectMapper.readTree(jsonResponse);
				JsonNode prots = jsonNode.get("protocolTemplates");
				// Iterate through the array elements
				ProtocolTemplate entry = null;
				int idx = 1;
				if (prots.isArray()) {
					for (JsonNode element : prots) {
						// Access and print array elements
						if (element != null) {
							entry = new ProtocolTemplate();
							entry.setName(element.get("name").asText());
							entry.setId(Integer.valueOf(element.get("id").toString()));
							entry.setStatus(element.get("status").asText());
							
							// Test data, fix this later

							results.add(entry);
							if (idx > 4) {
								idx = 1;
							}
						}
					}
				}
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		} else {
			logger.info("Failed to fetch data for getList(protocol template)");
		}

		return results;

	}

	/**
	 * Get a list of protocol templates
	 * 
	 * @param usr a User object that is the logged in user. The token from the instance is used to 
	 * authenticate the API call
	 * @return ArrayList of ProtocolTemplates. Empty list on error.
	 */
	public ArrayList<ProtocolTemplate> getActiveList(User usr) {
		ArrayList<ProtocolTemplate> results = new ArrayList<ProtocolTemplate>();
		
		if (usr == null) {
			return results;
		}

		String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocoltemplate;
		String jsonResponse = apiHelper.callAPI(apiUrl, usr);
		if (!jsonResponse.isEmpty()) {
			ObjectMapper objectMapper = new ObjectMapper();

			JsonNode jsonNode;
			try {
				jsonNode = objectMapper.readTree(jsonResponse);
				JsonNode prots = jsonNode.get("protocolTemplates");
				// Iterate through the array elements
				ProtocolTemplate entry = null;
				int idx = 1;
				if (prots.isArray()) {
					for (JsonNode element : prots) {
						// Access and print array elements
						if (element != null) {
							entry = new ProtocolTemplate();
							entry.setName(element.get("name").asText());
							entry.setId(Integer.valueOf(element.get("id").toString()));
							entry.setStatus(element.get("status").asText());
							
							if (element.get("status").asText().equals("LIVE")) { 
								results.add(entry);
								if (idx > 4) {
									idx = 1;
								}
							}
						}
					}
				}
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		} else {
			logger.info("Failed to fetch data for getList(protocol template)");
		}

		return results;

	}

	/**
	 * Get a list of protocol template steps
	 * 
	 * @param usr a User object that is the logged in user. The token from the instance is used to 
	 * authenticate the API call.
	 * @param id The id of the protocol template.
	 * @return ArrayList of ProtocolStepTemplates. Empty list on error.
	 */
	public ArrayList<ProtocolStepTemplate> getStepList(User usr, int id) {
	    ArrayList<ProtocolStepTemplate> results = new ArrayList<ProtocolStepTemplate>();

	    String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocoltemplateget + id;
	    String jsonResponse = apiHelper.callAPI(apiUrl, usr);
	    if (!jsonResponse.isEmpty()) {
	        ObjectMapper objectMapper = new ObjectMapper();

	        JsonNode jsonNode;
	        try {
	            jsonNode = objectMapper.readTree(jsonResponse);
	            JsonNode temp = jsonNode.get("associatedSteps");
	            JsonNode prots = temp.get("steps");
	            // Iterate through the array elements
	            ProtocolStepTemplate entry = null;
	            if (prots.isArray()) {
	                int idx = 1;
	                for (JsonNode element : prots) {
	                    // Access and print array elements
	                    if (element != null) {
	                        entry = new ProtocolStepTemplate();
	                        entry.setDescription(element.get("description").asText());
	                        entry.setName(element.get("name").asText());
	                        entry.setId(element.get("id").asInt());

	                        // Retrieve and set category ID and name
	                        JsonNode categoryNode = element.get("category");
	                        if (categoryNode != null) {
	                            entry.setCategoryId(categoryNode.get("id").asInt());
	                            entry.setCategoryName(categoryNode.get("name").asText());
	                            entry.setCategoryDescription(categoryNode.get("description").asText());
	                        }
	                        
	                        // Test data, fix this later
	                        entry.setType(idx++);
	                        if (idx > 4) {
	                            idx = 1;
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
	        logger.info("Failed to fetch data for getStepList");
	    }

	    return results;
	}

		
	/**
	 * Get a specific protocol template.
	 * 
	 * @param usr a User object that is the logged in user. The token from the instance is used to 
	 * authenticate the API call.
	 * @param id The id of the protocol template.
	 * @return ProtocolTemplate instance. New/Blank is returned if an error occurs.
	 */
	public ProtocolTemplate getTemplate(User usr, int id) {
		ProtocolTemplate result = new ProtocolTemplate();

		String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocoltemplate + "/" + id;
		String jsonResponse = apiHelper.callAPI(apiUrl, usr);
		if (!jsonResponse.isEmpty()) {
			ObjectMapper objectMapper = new ObjectMapper();
	
			JsonNode jsonNode;
			try {
				jsonNode = objectMapper.readTree(jsonResponse);
				result.setName(jsonNode.get("name").asText());
				result.setDescription(jsonNode.get("description").asText());
				result.setId(Integer.valueOf(jsonNode.get("id").asText()));
				result.setStatus(jsonNode.get("status").asText());
				result.setType(jsonNode.get("templateCategory").asText());
				if(jsonNode.has("defaultDueByInYears")&& !jsonNode.get("defaultDueByInYears").isNull()) {
					result.setDueByYear(jsonNode.get("defaultDueByInYears").asInt());
				}
				else
				{
					result.setDueByYear(0);
				}
				if(jsonNode.has("defaultDueByInMonths")&& !jsonNode.get("defaultDueByInMonths").isNull()) {
					result.setDueByMonth(jsonNode.get("defaultDueByInMonths").asInt());
				}
				else
				{
					result.setDueByMonth(0);
				}
				if(jsonNode.has("defaultDueByInDays")&& !jsonNode.get("defaultDueByInDays").isNull()) {
					result.setDueByDay(jsonNode.get("defaultDueByInDays").asInt());
				}
				else
				{
					result.setDueByDay(0);
				}
	            JsonNode defaultProtocolRecurrence = jsonNode.get("defaultProtocolRecurrence");
	            if (defaultProtocolRecurrence != null) {
	                if (defaultProtocolRecurrence.has("defaultReoccurInYears") && !defaultProtocolRecurrence.get("defaultReoccurInYears").isNull()) {
	                    result.setYearSchedule(defaultProtocolRecurrence.get("defaultReoccurInYears").asInt());
	                } else {
	                    result.setYearSchedule(0);
	                }
	                
	                if (defaultProtocolRecurrence.has("defaultReoccurInMonths") && !defaultProtocolRecurrence.get("defaultReoccurInMonths").isNull()) {
	                    result.setMonthSchedule(defaultProtocolRecurrence.get("defaultReoccurInMonths").asInt());
	                } else {
	                    result.setMonthSchedule(0);
	                }
	                
	                if (defaultProtocolRecurrence.has("defaultReoccurInDays") && !defaultProtocolRecurrence.get("defaultReoccurInDays").isNull()) {
	                    result.setDaySchedule(defaultProtocolRecurrence.get("defaultReoccurInDays").asInt());
	                } else {
	                    result.setDaySchedule(0);
	                }
	            }
				//if (jsonNode.get("dueDate") != null) {
				//	result.setDueDate(jsonNode.get("dueDate").asText());
				//}
				/* Add in the steps */
				JsonNode perms = jsonNode.get("associatedSteps");
				// Iterate through the array elements
				ArrayList<ProtocolStepTemplate> steps = new ArrayList<ProtocolStepTemplate>();
				if (perms.isArray()) {
					for (JsonNode element : perms) {
						// Access and print array elements
						if (element != null) {
							ProtocolStepTemplate curStep = new ProtocolStepTemplate();
							curStep.setName(element.get("name").asText());
							curStep.setId(Integer.parseInt(element.get("id").asText()));
							curStep.setDescription(element.get("description").asText());
							steps.add(curStep);
						}
					}
					result.setSteps(steps);
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
	
    public int deleteProtocolStepTemplate(User usr, int templateId, int stepId) {
    	int result = -1;
    	logger.info("Calling deleteProtocolStepTemplate");
		String apiUrl = this.dashboardApiBaseUrl + Constants. api_ep_protocoltemplateget +templateId + "/step-template?stepTemplateId="+stepId ;
    	result = apiHelper.deleteAPI(apiUrl,usr);
    	logger.info( "Url: " + apiUrl);
    	return result;
    }
    
    public int deleteProtocolTemplate(User usr, int protocolTemplateId) {
    	int result = 0;
    	logger.info("Calling deleteProtocolTemplate");
    	String apiUrl = this.dashboardApiBaseUrl+ Constants.api_ep_protocoltemplateget + protocolTemplateId;
    	logger.info("Api Url: "+apiUrl);
    	result = apiHelper.deleteAPI(apiUrl,usr);
    	logger.info("Result: " + result);
    	
    	return result;
    }
    
    public int updateHomeworkTemplate(User usr, int id, HomeworkTemplate template) {
    	int result = -1;
    	return result;
    }

    public int changeStatus(User usr, int pcolId, String status) {
			int result = -1;

			String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocoltemplateget + pcolId;

			StringBuilder requestBodyBuilder = new StringBuilder();
			requestBodyBuilder.append("{");
			requestBodyBuilder.append("\"status\": \"").append(status).append("\"");
			requestBodyBuilder.append("}");
			String requestBody = requestBodyBuilder.toString();
			logger.info("apiUrl: " + apiUrl);
			logger.info("requestBody: " + requestBody);
			result = apiHelper.patchAPI(apiUrl, requestBody, usr);

			return result;
		}

			public String linkHomeworkQuestionAndProtocolTemplate(User usr, int templateId, int questionId){
				String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocoltemplateget + templateId+ "/link-question/" + questionId;
				logger.info("apiUrl: " + apiUrl);
				String result = apiHelper.postWithoutBodyAPI(apiUrl,usr);
				logger.info("Result: " + result);
				return result;
			}





}
