package com.cairn.ui.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cairn.ui.Constants;
import com.cairn.ui.model.Protocol;
import com.cairn.ui.model.ProtocolComments;
import com.cairn.ui.model.ProtocolStep;
import com.cairn.ui.model.ProtocolStepNote;
import com.cairn.ui.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ProtocolHelper {
    Logger logger = LoggerFactory.getLogger(ProtocolHelper.class);
    private APIHelper apiHelper = new APIHelper();

    @Value("${waypoint.dashboard-api.base-url}")
	private String dashboardApiBaseUrl;
	
	/**
	 * Get a list of available protocols
	 * 
	 * @return
	 */
	public ArrayList<Protocol> getList(User usr) {
		ArrayList<Protocol> results = new ArrayList<Protocol>();
		String jsonResponse = apiHelper.callAPI(this.dashboardApiBaseUrl + Constants.api_ep_protocol, usr);
		if (!jsonResponse.isEmpty()) {
			ObjectMapper objectMapper = new ObjectMapper();

			JsonNode jsonNode;
			try {
				jsonNode = objectMapper.readTree(jsonResponse);
				JsonNode prots = jsonNode.get("protocols");
				// Iterate through the array elements
				Protocol entry = null;
				if (prots.isArray()) {
					for (JsonNode element : prots) {
						// Access and print array elements
						if (element != null) {
							entry = new Protocol();
							entry.setName(element.get("name").asText());
							entry.setId(Integer.valueOf(element.get("id").toString()));
							entry.setStartDate(
									new SimpleDateFormat("yyyy-MM-dd").parse(element.get("createdAt").asText()));
							if (element.has("completedOn") && !element.get("completedOn").isNull()) {
								entry.setCompletionDate(
										new SimpleDateFormat("yyyy-MM-dd").parse(element.get("completedOn").asText()));
							} else {
								entry.setCompletionDate(null);
							}

							results.add(entry);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return results;

	}

	public ArrayList<Protocol> getListbyTemplateId(User usr, int tempId) {
	    ArrayList<Protocol> results = new ArrayList<>();

	    String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocol + "/protocol-template/" + tempId;
	    String jsonResponse = apiHelper.callAPI(apiUrl, usr);
	    if (!jsonResponse.isEmpty()) {
	        ObjectMapper objectMapper = new ObjectMapper();
	        
	        try {
	            JsonNode jsonNode = objectMapper.readTree(jsonResponse);
	            JsonNode prots = jsonNode.get("protocols");
	            if (prots != null && prots.isArray()) {
	                for (JsonNode element : prots) {
	                    Protocol entry = new Protocol();

	                    // Extract and set Protocol properties with null checks
	                    entry.setId((element.has("id") && !element.get("id").isNull()) ? element.get("id").asInt() : -1);
	                    entry.setName((element.has("name") && !element.get("name").isNull()) ? element.get("name").asText() : "null");
	                    entry.setDescription((element.has("description") && !element.get("description").isNull()) ? element.get("description").asText() : "null");
	                    entry.setGoal((element.has("goal") && !element.get("goal").isNull()) ? element.get("goal").asText() : "No Goal Set");
	                    entry.setProgress((element.has("goalProgress") && !element.get("goalProgress").isNull()) ? element.get("goalProgress").asText() : "None");
	                    entry.setNeedsAttention((element.has("needsAttention") && !element.get("needsAttention").isNull()) ? element.get("needsAttention").asBoolean() : false);
	                    entry.setCompletionPercent((element.has("completionPercentage") && !element.get("completionPercentage").isNull()) ? element.get("completionPercentage").asText() : "0.0");
	                    entry.setStatus((element.has("status") && !element.get("status").isNull()) ? element.get("status").asText() : "null");

	                    entry.setStartDate((element.has("createdAt") && !element.get("createdAt").isNull()) ? new SimpleDateFormat("yyyy-MM-dd").parse(element.get("createdAt").asText()) : null);
	                    entry.setCompletionDate((element.has("completedOn") && !element.get("completedOn").isNull()) ? new SimpleDateFormat("yyyy-MM-dd").parse(element.get("completedOn").asText()) : null);
	                    entry.setUserId((element.has("associatedHouseholdId") && !element.get("associatedHouseholdId").isNull()) ? element.get("associatedHouseholdId").asInt() : 0);

	                    // Parse steps
	                    JsonNode assoc = element.get("associatedSteps");
	                    if (assoc != null && assoc.has("steps")) {
	                        JsonNode asteps = assoc.get("steps");
	                        ArrayList<ProtocolStep> steps = new ArrayList<>();
	                        if (asteps.isArray()) {
	                            for (JsonNode stepElement : asteps) {
	                                ProtocolStep curStep = new ProtocolStep();

	                                // Extract and set ProtocolStep properties with null checks
	                                curStep.setId((stepElement.has("id") && !stepElement.get("id").isNull()) ? stepElement.get("id").asInt() : -1);
	                                curStep.setName((stepElement.has("name") && !stepElement.get("name").isNull()) ? stepElement.get("name").asText() : "null");
	                                curStep.setDescription((stepElement.has("description") && !stepElement.get("description").isNull()) ? stepElement.get("description").asText() : "null");
	                                curStep.setStartDate(entry.getStartDate());

	                                // Parse step notes
	                                JsonNode stepNotesNode = stepElement.path("stepNotes").path("notes");
	                                ArrayList<ProtocolStepNote> stepNotes = new ArrayList<>();
	                                if (stepNotesNode.isArray()) {
	                                    for (JsonNode noteElement : stepNotesNode) {
	                                        ProtocolStepNote stepNote = new ProtocolStepNote();

	                                        // Extract and set ProtocolStepNote properties with null checks
	                                        stepNote.setNote((noteElement.has("note") && !noteElement.get("note").isNull()) ? noteElement.get("note").asText() : "null");
	                                        stepNote.setTakenBy((noteElement.has("takenBy") && !noteElement.get("takenBy").isNull()) ? noteElement.get("takenBy").asText() : "null");
	                                        stepNote.setType((noteElement.has("type") && !noteElement.get("type").isNull()) ? noteElement.get("type").asText() : "null");

	                                        // Parse takenAt date
	                                        try {
	                                            stepNote.setTakenAt((noteElement.has("takenAt") && !noteElement.get("takenAt").isNull()) ? new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(noteElement.get("takenAt").asText()) : null);
	                                            if ("COMPLETED".equals(stepNote.getType()) || "CONDITIONAL COMPLETION".equals(stepNote.getType())) {
	                                                curStep.setCompletionDate(stepNote.getTakenAt());
	                                            }
	                                        } catch (ParseException e) {
	                                            e.printStackTrace();
	                                        }

	                                        stepNotes.add(stepNote);
	                                    }
	                                }
	                                curStep.setStepNotes(stepNotes);

	                                // Extract and set step category properties with null checks
	                                JsonNode stepCategoryNode = stepElement.path("category");
	                                if (!stepCategoryNode.isMissingNode() && !stepCategoryNode.path("id").isMissingNode()) {
	                                    curStep.setCategoryId(stepCategoryNode.path("id").asInt());
	                                    curStep.setCategoryName(stepCategoryNode.path("name").asText());
	                                    curStep.setCategoryDescription(stepCategoryNode.path("description").asText());
	                                }

	                                curStep.setStatus((stepElement.has("status") && !stepElement.get("status").isNull()) ? stepElement.get("status").asText() : "null");

	                                steps.add(curStep);
	                            }
	                            entry.setSteps(steps);
	                        }
	                    }

	                    entry.setStepCount();

	                    results.add(entry);
	                }
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	            logger.info("Error fetching protocols: " + e.getMessage());
	        }
	    }
	    return results;
	}



	/**
	 * Get a specific protocol.
	 * 
	 * @param usr a User object that is the logged in user. The token from the
	 *            instance is used to authenticate the API call.
	 * @param id  The id of the protocol.
	 * @return Protocol instance. New/Blank is returned if an error occurs.
	 */
	public Protocol getProtocol(User usr, int id) {
	    Protocol result = new Protocol();

	    String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocol + "/" + id;
	    String jsonResponse = apiHelper.callAPI(apiUrl, usr);
	    if (!jsonResponse.isEmpty()) {
	        ObjectMapper objectMapper = new ObjectMapper();
	        JsonNode jsonNode;
	        try {
	            jsonNode = objectMapper.readTree(jsonResponse);
	            result.setName(jsonNode.get("name").asText());
	            result.setDescription(jsonNode.get("description").asText());
	            result.setCompletionPercent(jsonNode.get("completionPercentage").asText());
	            if (jsonNode.has("dueBy") && !jsonNode.get("dueBy").isNull()) {
	                result.setDueDate(jsonNode.get("dueBy").asText());
	            } else {
	                result.setDueDate("No Due Date");
	            }

	            JsonNode commentsNode = jsonNode.path("protocolComments").path("comments");
	            ArrayList<ProtocolComments> comments = new ArrayList<>();
	            if (commentsNode.isArray()) {
	                for (JsonNode commentElement : commentsNode) {
	                    ProtocolComments comment = new ProtocolComments();
	                    comment.setComment(commentElement.has("comment") && !commentElement.get("comment").isNull()
	                            ? commentElement.get("comment").asText()
	                            : "No Comment");
	                    try {
	                        comment.setTakenAt(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
	                                .parse(commentElement.get("takenAt").asText()));
	                    } catch (ParseException e) {
	                        e.printStackTrace();
	                    }
	                    comment.setTakenBy(commentElement.get("takenBy").asText());
	                    comment.setCommentType(commentElement.get("commentType").asText());
	                    comments.add(comment);
	                }
	            }
	            result.setComments(comments);

	            result.setStatus(jsonNode.path("status").asText());
	            result.setId(Integer.valueOf(jsonNode.get("id").toString()));
	            result.setNeedsAttention(Boolean.valueOf(jsonNode.get("needsAttention").toString()));
	            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	            Calendar calendar = Calendar.getInstance();
	            try {
	                calendar.setTime(sdf.parse(jsonNode.get("lastStatusUpdateTimestamp").asText()));
	                result.setLastStatus(calendar.getTime());
	            } catch (ParseException e) {
	                e.printStackTrace();
	            }

	            if (jsonNode.has("goal") && !jsonNode.get("goal").isNull()) {
	                result.setGoal(jsonNode.get("goal").asText());
	            } else {
	                result.setGoal("No Goal Set");
	            }

	            if (jsonNode.has("goalProgress") && !jsonNode.get("goalProgress").isNull()) {
	                result.setProgress(jsonNode.get("goalProgress").asText());
	            } else {
	                result.setProgress("None");
	            }

	            JsonNode assoc = jsonNode.get("associatedSteps");
	            JsonNode asteps = assoc.get("steps");
	            ArrayList<ProtocolStep> steps = new ArrayList<ProtocolStep>();
	            if (asteps.isArray()) {
	                for (JsonNode element : asteps) {
	                    if (element != null) {
	                        ProtocolStep curStep = new ProtocolStep();
	                        curStep.setId(Integer.parseInt(element.get("id").asText()));
	                        curStep.setName(element.get("name").asText());
	                        curStep.setDescription(element.get("description").asText());

	                        JsonNode stepNotesNode = element.path("stepNotes").path("notes");
	                        ArrayList<ProtocolStepNote> stepNotes = new ArrayList<>();
	                        if (stepNotesNode.isArray()) {
	                            for (JsonNode noteElement : stepNotesNode) {
	                                ProtocolStepNote stepNote = new ProtocolStepNote();
	                                stepNote.setNote(noteElement.get("note").asText());
	                                stepNote.setTakenBy(noteElement.get("takenBy").asText());
	                                stepNote.setType(noteElement.get("type").asText());
	                                try {
	                                    stepNote.setTakenAt(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
	                                            .parse(noteElement.get("takenAt").asText()));
	                                    if (stepNote.getType().equals("COMPLETED") || stepNote.getType().equals("CONDITIONAL COMPLETION")) {
	                                        curStep.setCompletionDate(stepNote.getTakenAt());
	                                    }
	                                } catch (ParseException e) {
	                                    e.printStackTrace();
	                                }
	                                stepNotes.add(stepNote);
	                            }
	                        }
	                        curStep.setStepNotes(stepNotes);
	                        
	                        curStep.setNotes(element.get("stepNotes").path("notes").asText());
	                        curStep.setStartDate(result.getStartDate());

	                        JsonNode stepCategoryNode = element.path("category");
	                        if (!stepCategoryNode.isMissingNode()) {
	                            curStep.setCategoryId(stepCategoryNode.asInt());
	                        }
	                        curStep.setStatus(element.get("status").asText());
	                        steps.add(curStep);
	                    }
	                }
	                result.setSteps(steps);
	            }
	            result.setStepCount();
	            logger.info("Retrieved " + result.getStepCount() + " steps.");

	        } catch (JsonMappingException e) {
	            e.printStackTrace();
	        } catch (JsonProcessingException e) {
	            e.printStackTrace();
	        }
	    } else {
	        //logger.warn("Failed to fetch protocol data for user");
	    }

	    return result;
	}


	/**
	 * Provides list of Protocols that have been assigned clientId to their user
	 * array
	 * 
	 * @param usr
	 * @param clientId
	 * @return
	 */
	public ArrayList<Protocol> getAssignedProtocols(User usr, int clientId) {
	    ArrayList<Protocol> results = new ArrayList<Protocol>();
	    String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocolaccount + clientId;
	    String jsonResponse = apiHelper.callAPI(apiUrl, usr);
	    if (!jsonResponse.isEmpty()) {
	        ObjectMapper objectMapper = new ObjectMapper();

	        JsonNode jsonNode;
	        try {
	            jsonNode = objectMapper.readTree(jsonResponse);
	            JsonNode prots = jsonNode.get("protocols");
	            if (prots != null && prots.isArray()) {
	                for (JsonNode element : prots) {
	                    Protocol entry = new Protocol();
	                    entry.setName(
	                            element.has("name") && !element.get("name").isNull() ? element.get("name").asText()
	                                    : "null");
	                    entry.setDescription(element.has("description") && !element.get("description").isNull()
	                            ? element.get("description").asText()
	                            : "null");
	                    entry.setId(
	                            element.has("id") && !element.get("id").isNull() ? element.get("id").intValue() : -1);
	                    
	                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	                    entry.setStatus(element.has("status") && !element.get("status").isNull()
	                            ? element.get("status").asText()
	                            : "null");
	                    entry.setCompletionPercent(
	                            element.has("completionPercentage") && !element.get("completionPercentage").isNull()
	                                    ? element.get("completionPercentage").asText()
	                                    : "null");
	                    entry.setLastStatus(sdf.parse(element.get("lastStatusUpdateTimestamp").asText()));
	                    if (element.has("dueBy") && !element.get("dueBy").isNull()) {
	                        entry.setDueDate(element.get("dueBy").asText());
	                    }
	                    if (element.has("goal") && !element.get("goal").isNull()) {
	                        entry.setGoal(element.get("goal").asText());
	                    } else {
	                        entry.setGoal("No Goal Set");
	                    }
	                    if (element.has("goalProgress") && !element.get("goalProgress").isNull()) {
	                        entry.setProgress(element.get("goalProgress").asText());
	                    } else {
	                        entry.setProgress("None");
	                    }
	                    entry.setStartDate(new SimpleDateFormat("yyyy-MM-dd").parse(element.get("createdAt").asText()));
	                    if (element.has("completedOn") && !element.get("completedOn").isNull()) {
	                        entry.setCompletionDate(new SimpleDateFormat("yyyy-MM-dd").parse(element.get("completedOn").asText()));
	                    } else {
	                        entry.setCompletionDate(null);
	                    }

	                    JsonNode assoc = element.get("associatedSteps");
	                    ArrayList<ProtocolStep> steps = new ArrayList<ProtocolStep>();
	                    if (assoc != null) {
	                        JsonNode asteps = assoc.get("steps");
	                        if (asteps != null && asteps.isArray()) {
	                            for (JsonNode stepElement : asteps) {
	                                ProtocolStep curStep = new ProtocolStep();
	                                curStep.setId(stepElement.has("id") && !stepElement.get("id").isNull()
	                                        ? stepElement.get("id").intValue()
	                                        : -1);
	                                curStep.setTemplateId(stepElement.has("stepTemplateId") && !stepElement.get("stepTemplateId").isNull()
	                                        ? stepElement.get("stepTemplateId").intValue()
	                                        : -1);
	                                
	                                curStep.setName(stepElement.has("name") && !stepElement.get("name").isNull()
	                                        ? stepElement.get("name").asText()
	                                        : "null");
	                                
	                                logger.info("Step: " + curStep.getName()+ " Template ID: " + curStep.getTemplateId());
	                                curStep.setDescription(
	                                        stepElement.has("description") && !stepElement.get("description").isNull()
	                                                ? stepElement.get("description").asText()
	                                                : "null");
	                                curStep.setStartDate(entry.getStartDate());
	                                JsonNode stepCategoryNode = stepElement.path("category");
	                                if (!stepCategoryNode.isMissingNode()
	                                        && !stepCategoryNode.path("id").isMissingNode()) {
	                                    curStep.setCategoryId(stepCategoryNode.path("id").asInt());
	                                    curStep.setCategoryName(stepCategoryNode.path("name").asText());
	                                    curStep.setCategoryDescription(stepCategoryNode.path("description").asText());
	                                }

	                                JsonNode stepNotesNode = stepElement.path("stepNotes").path("notes");
	                                ArrayList<ProtocolStepNote> stepNotes = new ArrayList<>();
	                                if (stepNotesNode.isArray()) {
	                                    for (JsonNode noteElement : stepNotesNode) {
	                                        ProtocolStepNote stepNote = new ProtocolStepNote();
	                                        stepNote.setNote(noteElement.has("note") && !noteElement.get("note").isNull()
	                                                ? noteElement.get("note").asText()
	                                                : "null");
	                                        stepNote.setTakenBy(noteElement.has("takenBy") && !noteElement.get("takenBy").isNull()
	                                                ? noteElement.get("takenBy").asText()
	                                                : "null");
	                                        stepNote.setType(noteElement.has("type") && !noteElement.get("type").isNull()
	                                                ? noteElement.get("type").asText()
	                                                : "null");
	                                        try {
	                                            stepNote.setTakenAt(noteElement.has("takenAt") && !noteElement.get("takenAt").isNull()
	                                                    ? new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(noteElement.get("takenAt").asText())
	                                                    : null);
	                                            if (stepNote.getNote().equals("COMPLETED") || stepNote.getNote().equals("CONDITIONAL COMPLETION")) {
	                                                curStep.setCompletionDate(stepNote.getTakenAt());
	                                                logger.info(stepNote.getNote()+ " Start Date: "+curStep.getStartDate() +" Completed Date: " +curStep.getCompletionDate()+ " "+ curStep.getDaysToComplete());
	                                            }
	                                        } catch (ParseException e) {
	                                            e.printStackTrace();
	                                        }
	                                        stepNotes.add(stepNote);
	                                    }
	                                }
	                                curStep.setStepNotes(stepNotes);

	                                curStep.setCategoryName(
	                                        stepElement.has("category") && !stepElement.get("category").isNull()
	                                                ? stepElement.get("category").asText()
	                                                : "null");
	                                curStep.setStatus(stepElement.has("status") && !stepElement.get("status").isNull()
	                                        ? stepElement.get("status").asText()
	                                        : "null");
	                                steps.add(curStep);
	                            }
	                        }
	                        entry.setSteps(steps);
	                        entry.setStepCount();
	                        entry.setCompletedSteps();
	                    }
	                    results.add(entry);
	                }
	            }
	        } catch (JsonMappingException e) {
	            e.printStackTrace();
	        } catch (JsonProcessingException e) {
	            e.printStackTrace();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    } else {
	        logger.info("Failed to fetch data. getAssignedProtocols");
	    }

	    return results;
	}

	
	
	public ArrayList<ProtocolStep> getStepList(User usr, int id) {
		ArrayList<ProtocolStep> results = new ArrayList<ProtocolStep>();

		String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocol + '/' + id;
		String jsonResponse = apiHelper.callAPI(apiUrl, usr);
		if (!jsonResponse.isEmpty()) {
			ObjectMapper objectMapper = new ObjectMapper();

			JsonNode jsonNode;
			try {
				jsonNode = objectMapper.readTree(jsonResponse);
				JsonNode temp = jsonNode.get("associatedSteps");
				JsonNode prots = temp.get("steps");
				// Iterate through the array elements
				ProtocolStep entry = null;
				if (prots.isArray()) {
					for (JsonNode element : prots) {
						// Access and print array elements
						if (element != null) {
							entry = new ProtocolStep();
							entry.setDescription(element.get("description").asText());
							entry.setName(element.get("name").asText());
							entry.setStatus(element.path("status").asText());
							entry.setId(Integer.valueOf(element.get("id").toString()));
							entry.setCategoryName(element.path("category").asText());
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
			logger.info("Failed to fetch data. getStepList");
		}

		return results;
	}

	/*
	 * Assigns a clientId to a protocol
	 * 
	 * @param usr
	 * 
	 * @param pcol
	 */

	public int addClient(User usr, int clientid, int protocolTemplateId, Protocol protocolRequest) {
		int result = 0;
		String requestBody = String.format(
				"{\"protocolTemplateId\": %d, \"comment\": \"\", \"assignedHouseholdId\": %d, \"protocolName\": \"%s\", \"dueDate\": \"%s\"}",
				protocolTemplateId, clientid, protocolRequest.getName(), protocolRequest.getDueDate());
		String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocol;// retrieves all protocols assigned to clientId
		logger.info(apiUrl);
		logger.info(requestBody);
		result = apiHelper.postAPI(apiUrl, requestBody, usr);
		
		
		
		

		return result;
	}

	public int updateStepStatus(User usr, int protocolId, int stepId, String status) {
		int result = 0;
		String requestBody = "{\"status\": \"" + status + "\", \"completionCondition\": \"" + status + "\"}";
		String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocol + '/' + protocolId + "/protocol-step/"
				+ stepId + "/status";
		result = apiHelper.patchAPI(apiUrl, requestBody, usr);

		return result;

	}

	public int updateStepNote(User usr, int protocolId, int stepId, String note) {
		int result = 0;
		String requestBody = "{\"note\": \"" + note + "\"}";
		String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocol + '/' + protocolId + "/protocol-step/"
				+ stepId + "/note";
		result = apiHelper.postAPI(apiUrl, requestBody, usr);

		return result;

	}

	public int updateProtocolGoal(User usr, int protocolId, String goal) {
		int result = 0;
		String requestBody = "{\"goal\": \"" + goal + "\"}";
		String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocol + '/' + protocolId;
		result = apiHelper.patchAPI(apiUrl, requestBody, usr);

		return result;

	}

	public int updateProtocolComment(User usr, int protocolId, String comment) {
		int result = 0;
		String requestBody = "{\"comment\": \"" + comment + "\"}";
		String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocol + '/' + protocolId;
		result = apiHelper.patchAPI(apiUrl, requestBody, usr);

		return result;

	}

	public int updateProtocolProgress(User usr, int protocolId, String progress) {
		int result = 0;

		String requestBody = "{\"goalProgress\": \"" + progress + "\"}";
		String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocol + '/' + protocolId;
		result = apiHelper.patchAPI(apiUrl, requestBody,usr);

		return result;

	}

	public int postProtocolComment(User usr, int protocolId, String commentType, String comment) {
		int result = 0;
		String requestBody = String.format("{\"comment\": \"%s\", \"commentType\": \"%s\"}", comment, commentType);
		String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocol + '/' + protocolId + '/' + "comment";
		result = apiHelper.postAPI(apiUrl, requestBody, usr);

		return result;
	}

	public int updateProtocolDueDate(User usr, int protocolId, String dueDate) {
		int result = -1;
		String requestBody = "{\"dueDate\": \"" + dueDate + "\"}";
		String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocol + '/' + protocolId;
		result = apiHelper.patchAPI(apiUrl, requestBody, usr);

		return result;
	}

	public int updateProtocolStatus(User usr, int protocolId, String status) {
		int result = -1;
		String requestBody = "{\"newProtocolStatus\": \"" + status + "\"}";
		String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocol + '/' + protocolId + '/' + "status";
		result = apiHelper.patchAPI(apiUrl, requestBody, usr);
		return result;
		}
	
	public int updateProtocol(User usr, int protocolId, Protocol newProtocol) {
	    int result = -1;

	    // Build the base URL
	    String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocol + '/' + protocolId;

	    // Initialize the request body builder
	    StringBuilder requestBodyBuilder = new StringBuilder();
	    requestBodyBuilder.append("{");

	    // Append each non-null and non-zero field to the request body
	    if (newProtocol.getName() != null) {
	        requestBodyBuilder.append("\"protocolName\": \"").append(newProtocol.getName()).append("\",");
	    }
	    if (newProtocol.getGoal() != null) {
	        requestBodyBuilder.append("\"goal\": \"").append(newProtocol.getGoal()).append("\",");
	    }
	    if (newProtocol.getProgress() != null) {
	        requestBodyBuilder.append("\"goalProgress\": \"").append(newProtocol.getProgress()).append("\",");
	    }
	    if (newProtocol.getComments() != null && !newProtocol.getComments().isEmpty()) {
	        ProtocolComments comment = newProtocol.getComments().get(0); // Assuming there's at least one comment
	        requestBodyBuilder.append("\"comment\": \"").append(comment.getComment()).append("\",");
	        requestBodyBuilder.append("\"commentType\": \"").append(comment.getCommentType()).append("\",");
	    }
	    requestBodyBuilder.append("\"markForAttention\": ").append(newProtocol.isNeedsAttention()).append(",");
	    if (newProtocol.getDueDate() != null) {
	        requestBodyBuilder.append("\"dueDate\": \"").append(newProtocol.getDueDate()).append("\",");
	    }

	    // Remove the trailing comma if present
	    if (requestBodyBuilder.charAt(requestBodyBuilder.length() - 1) == ',') {
	        requestBodyBuilder.deleteCharAt(requestBodyBuilder.length() - 1);
	    }

	    requestBodyBuilder.append("}");

	    // Convert the StringBuilder to a String
	    String requestBody = requestBodyBuilder.toString();

	    // Log the URL and request body
	    logger.info(apiUrl);
	    logger.info(requestBody);

	    // Send the PATCH request
	    result = apiHelper.patchAPI(apiUrl, requestBody, usr);
	    
	    return result;
	}
	
    public int deleteProtocol(User usr, int protocolId) {
    	int result = 0;
    	String apiUrl = this.dashboardApiBaseUrl + Constants.api_ep_protocol+'/' + protocolId;
    	result = apiHelper.deleteAPI(apiUrl,usr);
    	
    	return result;
    }


	
	

}
