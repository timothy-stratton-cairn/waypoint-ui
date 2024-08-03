package com.cairn.ui;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.PermissionCollection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cairn.ui.helper.DashboardHelper;
import com.cairn.ui.helper.HomeworkHelper;
import com.cairn.ui.helper.HomeworkQuestionHelper;
import com.cairn.ui.helper.HomeworkTemplateHelper;
import com.cairn.ui.helper.ProtocolHelper;
import com.cairn.ui.helper.ProtocolStepTemplateHelper;
import com.cairn.ui.helper.ProtocolTemplateHelper;
import com.cairn.ui.helper.ReportHelper;
import com.cairn.ui.helper.UserHelper;
import com.cairn.ui.model.AssignedHomeworkResponse;
import com.cairn.ui.model.AssignedHomeworkResponseList;
import com.cairn.ui.model.ExpectedHomeworkResponses;
import com.cairn.ui.model.Homework;
import com.cairn.ui.model.HomeworkQuestion;
import com.cairn.ui.model.HomeworkQuestionsTemplate;
import com.cairn.ui.model.HomeworkResponse;
import com.cairn.ui.model.HomeworkTemplate;
import com.cairn.ui.model.Household;
import com.cairn.ui.model.PasswordRequest;
import com.cairn.ui.model.Protocol;
import com.cairn.ui.model.ProtocolComments;
import com.cairn.ui.model.ProtocolReport;
import com.cairn.ui.model.ProtocolStats;
import com.cairn.ui.model.ProtocolStep;
import com.cairn.ui.model.ProtocolStepTemplate;
import com.cairn.ui.model.ProtocolTemplate;
import com.cairn.ui.model.ReportStat;
import com.cairn.ui.model.User;
import com.cairn.ui.model.UserDAO;

import jakarta.servlet.http.HttpSession;

@Controller
public class MainController {
	Logger logger = LoggerFactory.getLogger(MainController.class);

	@Autowired
	UserDAO userDAO;

	@Autowired
	DashboardHelper helper;

	@Autowired
	HomeworkTemplateHelper homeworkTemplateHelper;

	@Autowired
	ProtocolHelper protocolHelper;

	@Autowired
	ProtocolStepTemplateHelper protocolStepTemplateHelper;

	@Autowired
	ProtocolTemplateHelper protocolTemplateHelper;

	@Autowired
	UserHelper userHelper;

	@Autowired
	ReportHelper reportHelper;
	
	@Autowired
	HomeworkHelper homeworkHelper;
	
	@Autowired
	HomeworkQuestionHelper questionHelper;

	@Autowired
	private JavaMailSender mailSender;

	@GetMapping("/")
	public String startPage() {
		return "home";
	}

	@GetMapping("/dashboard")
	public String homePage(@RequestParam(name = "msg", required = false) String msg, Model model, HttpSession session) {
		User usr = userDAO.getUser();
		if (usr == null) {
			return "home";
		}

		ArrayList<Protocol> pcolList = protocolHelper.getAssignedProtocols(usr, userHelper.getHouseholdId(usr));

		logger.info("Household Id: "+ userHelper.getHouseholdId(usr));
		ArrayList<Protocol> upcomingPcol = new ArrayList<Protocol>();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date currentDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		calendar.add(Calendar.DAY_OF_YEAR, 7);
		Date upcomingWeek = calendar.getTime();
		if (pcolList.isEmpty()) {
			logger.warn("No Protocols Returned");
			// logger.info("No Protocols Returned");
		} else {
			logger.info("Returned " + pcolList.size() + " protocols.");
			// logger.info(pcolList.size());
		}
		for (Protocol pcol : pcolList) {
			String dueDateStr = pcol.getDueDate();
			if (dueDateStr != null) {
				try {
					Date dueDate = dateFormat.parse(dueDateStr);
					if (dueDate.after(currentDate) && dueDate.before(upcomingWeek)) {
						upcomingPcol.add(pcol);
					}
				} catch (ParseException e) {
					e.printStackTrace(); // Handle parse exception
				}
			}
		}
		ArrayList<ProtocolStats> stats = helper.getDashboard(usr);
logger.info("Empty List");
			
			// logger.info("Temp ID: " + stat.getTemplateId() + " Number Of Steps: "+
			// stat.getNumSteps() + " Progress: " + stat.getProgress());
	

		session.setAttribute("me", usr);
		model.addAttribute("msg", msg);
		model.addAttribute("user", usr);
		model.addAttribute("stats", stats);
		model.addAttribute("upcomingProtocols", upcomingPcol);

		return "UserDashboard";
	}

	@GetMapping("/login")
	public String loginPage() {

		return "home";
	}
	
    @GetMapping("/forgotPassword")
    public String forgotPassowrd() {
    	
    	return "forgotPassword";
    }
    
    @GetMapping("/password-reset")
    public String changePassword(@RequestParam String passwordResetToken, @RequestParam String username, Model model) {
    	logger.info(username);
    	logger.info(passwordResetToken);
    	String user = username;
    	model.addAttribute("username", user);
    	model.addAttribute("token",passwordResetToken);
    	return "changePassword";
    }
    
    @PostMapping("/newUserPassword/")
    public ResponseEntity<Object> newUserPassword(@RequestBody PasswordRequest request) {
        String call = "";
        try {
            call = userHelper.newUserPassword(request.getUsername(), request.getKey(), request.getPassword());
            logger.info(call);
            
            if (call.startsWith("Success")) {
                return ResponseEntity.ok(Collections.singletonMap("message", "Client added successfully"));
            } else {
                return generateErrorResponse(call);
            }
        } catch (Exception e) {
            logger.error("Exception occurred while processing newUserPassword: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    private ResponseEntity<Object> generateErrorResponse(String call) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Error processing the request");

        if (call.contains("error")) {
            try {
                String errorMessage = call.substring(call.indexOf("\"error\":\"") + 8, call.indexOf("\",", call.indexOf("\"error\":\"")));
                errorResponse.put("error", errorMessage);
            } catch (Exception e) {
                logger.error("Error extracting error message: ", e);
                errorResponse.put("error", "An unexpected error occurred while extracting error details.");
            }
        }

        logger.warn("Error response: " + errorResponse.toString());
        return ResponseEntity.badRequest().body(errorResponse);
    }



	/**
	 * Handle a request to view the Protocol details.
	 * 
	 * @return
	 */
	@GetMapping("/viewProtocol/{pcolId}/{userId}")
	public String viewProtocol(Model model, @PathVariable int pcolId, @PathVariable int userId) {
		User currentUser = userDAO.getUser();
		Protocol protocol = protocolHelper.getProtocol(currentUser, pcolId);
		
		logger.info("Calling getHomeworkByProtocol");
		ArrayList<Homework> allHomeworks = homeworkHelper.getHomeworkByProtocolId(currentUser, pcolId);
		if (allHomeworks != null && !allHomeworks.isEmpty()) {
			for (Homework homework : allHomeworks) {
				logger.info("Homework Name: " + homework.getName() + " Homework ID: " + homework.getId()
						+ ", Description: " + homework.getDescription());
			}
		} else {
			logger.info("No homeworks found or list is empty");
		}
		ArrayList<ProtocolStep> steps = protocolHelper.getStepList(currentUser, pcolId);
		for (ProtocolStep step: steps) {
			logger.info("Step: "+step.getName()+ " Status: "+step.getStatus());
		}
		//steps.removeIf(step -> !step.getStatus().equals("LIVE"));
		ProtocolComments mostRecentComment = protocol.getComments().stream()
				.filter(comment -> "COMMENT".equals(comment.getCommentType()))
				.max(Comparator.comparing(ProtocolComments::getTakenAt)).orElse(null);
		if (mostRecentComment == null) {
			mostRecentComment = new ProtocolComments();
			mostRecentComment.setComment("No Comments Have been made");
		}

		model.addAttribute("mostRecentComment", mostRecentComment.getComment());
		model.addAttribute("protocol", protocol);
		model.addAttribute("steps", steps);
		model.addAttribute("protocolId", pcolId);
		model.addAttribute("userId", userId);
		model.addAttribute("homeworks", allHomeworks);

		return "protocolDetail";
	}

	@GetMapping("/analysis/{id}")
	public String analysis(@PathVariable int id, Model model) {
		User currentUser = userDAO.getUser();
		ArrayList<ProtocolStep> stepList = protocolHelper.getStepList(currentUser, id);
		ArrayList<Integer> stepIds = new ArrayList<Integer>();
		
		ArrayList<Homework> allHomeworks = homeworkHelper.getHomeworkByProtocolId(currentUser, id);
		
		for (ProtocolStep step : stepList) {
			logger.info("Step " + step.getName() + " Category: " + step.getCategoryName());
			if (step.getCategoryName() == "Run Analysis") {
				stepIds.add(step.getId());
			}
		}
		// Using removeIf to directly filter elements
		stepList.removeIf(step -> step.getCategoryName() != "Run Analysis");

		for (ProtocolStep step : stepList) {
			logger.info("Step " + step.getName() + " Catagory: " + step.getCategoryName());
		}

		if (allHomeworks != null && !allHomeworks.isEmpty()) {
			for (Homework homework : allHomeworks) {
				logger.info(
						"Homework Name: " + homework.getName() + " Homework ID: " + homework.getId() + ", Description: "
								+ homework.getDescription() + " Parent Step Id: " + homework.getParentStepId());
			}
		} if(allHomeworks == null) {
			logger.info("allHomeworks is Null");
		} else {
			logger.info("allHomeworks is Empty");
		}
		if (allHomeworks != null && !allHomeworks.isEmpty()) {
			allHomeworks.removeIf(homework -> !stepIds.contains(homework.getParentStepId())); // Remove homeworks whose
																								// ParentStepId is not
																								// in stepIds
		}

		model.addAttribute("homeworks", allHomeworks);
		model.addAttribute("steps", stepList);

		return "analysis";
	}

	@GetMapping("/recommendations/{id}")
	public String recomendations(@PathVariable int id, Model model) {
		User currentUser = userDAO.getUser();
		ArrayList<ProtocolStep> stepList = protocolHelper.getStepList(currentUser, id);
		ArrayList<Integer> stepIds = new ArrayList<Integer>();

		ArrayList<Homework> allHomeworks = homeworkHelper.getHomeworkByProtocolId(currentUser, id);

		for (ProtocolStep step : stepList) {
			logger.info("Step " + step.getName() + " Category: " + step.getCategoryId());
			if (step.getCategoryId() == 2) {
				stepIds.add(step.getId());
			}
		}
		// Using removeIf to directly filter elements
		stepList.removeIf(step -> step.getCategoryId() != 3);
		for (ProtocolStep step : stepList) {
			logger.info("Step " + step.getName() + " Catagory: " + step.getCategoryId());
		}
		if (allHomeworks != null && !allHomeworks.isEmpty()) {
			for (Homework homework : allHomeworks) {
				logger.info(
						"Homework Name: " + homework.getName() + " Homework ID: " + homework.getId() + ", Description: "
								+ homework.getDescription() + " Parent Step Id: " + homework.getParentStepId());
			}
		} else {
			logger.info("No homeworks found or list is empty");
		}
		if (allHomeworks != null && !allHomeworks.isEmpty()) {
			allHomeworks.removeIf(homework -> !stepIds.contains(homework.getParentStepId())); // Remove homeworks whose
																								// ParentStepId is not
																								// in stepIds
		}
		logger.info("No homeworks found or list is empty");

		model.addAttribute("homeworks", allHomeworks);
		model.addAttribute("steps", stepList);
		return "recommendations";
	}

	@PatchMapping("/updateProtocolComment/{protocolId}/{comment}")
	public ResponseEntity<Object> updateProtocolComment(@PathVariable int protocolId, @PathVariable String comment,
			Model model) {
		User currentUser = userDAO.getUser();

		try {
			protocolHelper.updateProtocolComment(currentUser, protocolId, comment);
		} catch (Exception e) {
			logger.info("Error in addClientToProtocol:");
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error updating Protocol Comment: " + e.getMessage());

		}
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/updateProtocolGoal/{protocolId}/{goal}")
	public ResponseEntity<Object> updateProtocolGoal(@PathVariable int protocolId, @PathVariable String goal,
			Model model) {
		User currentUser = userDAO.getUser();

		try {
			protocolHelper.updateProtocolGoal(currentUser, protocolId, goal);
		} catch (Exception e) {
			logger.info("Error in addClientToProtocol:");
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error updating Protocol Comment: " + e.getMessage());

		}
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/updateProtocol/{protocolId}")
	public ResponseEntity<Object> updateProtocolComment(@PathVariable int protocolId, @RequestBody Protocol pcol, Model model) {

		User currentUser = userDAO.getUser();
		logger.info("Name: " + pcol.getName());
		logger.info("Needs Attention: " +pcol.isNeedsAttention());
		logger.info("Goal: " + pcol.getGoal() );
		logger.info("Status: "+ pcol.getStatus());
		try {
			protocolHelper.updateProtocol(currentUser, protocolId, pcol);
			//protocolHelper.postProtocolComment(currentUser, protocolId, "COMMENT", comment);
			//protocolHelper.updateProtocolGoal(currentUser, protocolId, goal);
			//protocolHelper.updateProtocolProgress(currentUser, protocolId, progress);
			protocolHelper.updateProtocolStatus(currentUser, protocolId, pcol.getStatus());
			//protocolHelper.updateProtocolDueDate(currentUser, protocolId, date);
		} catch (Exception e) {
			logger.info("Error in addClientToProtocol:");
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error updating Protocol Comment: " + e.getMessage());
		}
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/updateStepStatus/{protocolId}/{stepId}/{status}")
	public ResponseEntity<Object> updateStepStatus(@PathVariable int protocolId, @PathVariable int stepId,
			@PathVariable String status, Model model) {
		User currentUser = userDAO.getUser();

		logger.info(status);
		try {
			protocolHelper.updateStepStatus(currentUser, protocolId, stepId, status);
			if(status.equals("DONE")) {
				protocolHelper.updateStepNote(currentUser, protocolId, stepId, "COMPLETED");
				
			}
			if(status.equals("CONDITIONAL_COMPLETION")) {
				protocolHelper.updateStepNote(currentUser, protocolId, stepId, "CONDITIONAL COMPLETION");
			}
			
		} catch (Exception e) {
			logger.info("Error in addClientToProtocol:");
			e.printStackTrace(); // Print the stack trace to the console
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error updating Status: " + e.getMessage());
		}

		return ResponseEntity.ok().build();
	}

	@PostMapping("/updateStepNote/{protocolId}/{stepId}/{note}")
	public ResponseEntity<Object> updateStepNote(@PathVariable int protocolId, @PathVariable int stepId,
			@PathVariable String note, Model model) {
		User currentUser = userDAO.getUser();

		logger.info(note);
		try {
			protocolHelper.updateStepNote(currentUser, protocolId, stepId, note);
		} catch (Exception e) {
			logger.info("Error in addClientToProtocol:");
			e.printStackTrace(); // Print the stack trace to the console
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error updating Note : " + e.getMessage());
		}
		return ResponseEntity.ok().build();
	}

	@GetMapping("/protocols")
	public String protocolListPage(HttpSession session, Model model) {
		User usr = (User) userDAO.getUser();

		List<Protocol> listProtocols = protocolHelper.getList(usr);
		model.addAttribute("listProtocols", listProtocols);
		return "protocolList";
	}

	@GetMapping("/protocolsByTemplate/{tempId}/")
	public String protocolsByTemplate(HttpSession session, @PathVariable int tempId, Model model) {
		User usr = (User) userDAO.getUser();
		List<Protocol> listProtocols = protocolHelper.getListbyTemplateId(usr, tempId);
		Map<Integer, String> userNames = new HashMap<>();
		Map<Integer, Integer> userIds = new HashMap<>();

		for (Protocol protocol : listProtocols) {
			ArrayList<Integer> userList = protocol.getUsers();
			// Check if the user list is not empty before accessing it
			if (!userList.isEmpty()) {
				User user = userHelper.getUser(usr, userList.get(0));
				if (user != null) {
					userNames.put(protocol.getId(), user.getFirstName() + " " + user.getLastName());
					userIds.put(protocol.getId(), user.getId());
				} else {
					userNames.put(protocol.getId(), "user is null");
					userIds.put(protocol.getId(), 0);
				}
			} else {
				User user = userHelper.getUser(usr, protocol.getId());
				if (user != null) {
					userNames.put(protocol.getId(), user.getFirstName() + " " + user.getLastName());
					userIds.put(protocol.getId(), user.getId());
				} else {
					userNames.put(protocol.getId(), "user is null");
					userIds.put(protocol.getId(), 0);
				}
			}
		}

		model.addAttribute("listProtocols", listProtocols);
		model.addAttribute("userNames", userNames);
		model.addAttribute("userIds", userIds);
		return "protocolList";
	}

	@GetMapping("/editProtocol/{id}")
	public String editProtocolTemplate(@PathVariable int id, Model model) {
		User usr = (User) userDAO.getUser();
		ProtocolTemplate pcol = protocolTemplateHelper.getTemplate(usr, id);
		//logger.info("Protocol Template DueDate: " + pcol.getDueDate());
		logger.info("Protocol TEmplate Status: "+ pcol.getStatus());
		ArrayList<ProtocolStepTemplate> allSteps = protocolTemplateHelper.getAllSteps(usr);
		List<ProtocolStepTemplate> listSteps = protocolTemplateHelper.getStepList(usr, id);
		List<Household> clientList = userHelper.getHouseholdList(usr);
		List<ProtocolStepTemplate> fullStepList = new ArrayList<ProtocolStepTemplate>(); // there's a method to this
																							// madness. getStepList
																							// doesn't get homeworks
		int dueByDays = pcol.getDueByDay();

		for (ProtocolStepTemplate step : listSteps) {
			int stepId = step.getId();
			ProtocolStepTemplate fullStep = protocolTemplateHelper.getStep(usr, stepId);
			if (fullStep.getStatus().equals("LIVE")) {
			fullStepList.add(fullStep);
			}
		}

		for (ProtocolStepTemplate step: listSteps) {
			logger.info("Step: "+ step.getName() + " CatagoryID: "+ step.getCategoryName());
		}
		
		model.addAttribute("clientList", clientList);
		
		model.addAttribute("dueBy", dueByDays);
		model.addAttribute("protocolId", id);
		model.addAttribute("protocol", pcol);
		model.addAttribute("steps", fullStepList);
		model.addAttribute("allSteps", allSteps);
		return "displayProtocol";
	}

	// Place holders for creating new and saving changes to steps and protocols
	@GetMapping("/newStep/")
	public String newStep(Model model) {
		ProtocolStepTemplate step = new ProtocolStepTemplate();
		User usr = (User) userDAO.getUser();
		ArrayList<HomeworkTemplate> templatelist = this.homeworkTemplateHelper.getList(usr);
		for (HomeworkTemplate hw : templatelist) {
			logger.info("Homework ID: " + hw.getId() + " Homework Name: " + hw.getName());
		}
		
		model.addAttribute("homework", templatelist);
		model.addAttribute("step", step);
		model.addAttribute("pcolId", null);
		return "newStep";
	}

	@GetMapping("/newStep/{id}")
	public String newStep(@PathVariable int id, Model model) {
		ProtocolStepTemplate step = new ProtocolStepTemplate();
		User usr = (User) userDAO.getUser();
		ArrayList<HomeworkTemplate> templatelist = this.homeworkTemplateHelper.getList(usr);
		for (HomeworkTemplate hw : templatelist) {
			logger.info("Homework ID: " + hw.getId() + " Homework Name: " + hw.getName());
		}
		model.addAttribute("pcolId", id);
		model.addAttribute("homework", templatelist);
		model.addAttribute("step", step);

		return "newStep";
	}

	@PostMapping("/saveStep/")
	public ResponseEntity<String> saveStep(@RequestBody ProtocolStepTemplate newStep) {
		User usr = (User) userDAO.getUser();

		try {
			int stepId = protocolStepTemplateHelper.addStepTemplate(usr, newStep);
			if (stepId > 0) {
				return ResponseEntity
						.ok("{\"message\": \"Template Successfully Created!\", \"stepId\": " + stepId + "}");
			} else {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body("{\"message\": \"Failed to create Step Template.\"}");
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
					"{\"message\": \"An error occurred while saving the Step Template: " + e.getMessage() + "\"}");
		}
	}

	@PatchMapping("/updateStep/{id}")
	public ResponseEntity<String> updateStep(@PathVariable int id, @RequestBody ProtocolStepTemplate step) {
		User usr = (User) userDAO.getUser();

		try {
			protocolStepTemplateHelper.updateStepTemplate(usr, id, step);
			return ResponseEntity.ok("{\"message\": \"Template Successfully Saved!\"}");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("An error occurred while saving the Step Template: " + e.getMessage());
		}

	}

	@GetMapping("/newProtocol/")
	public String newProtocol(Model model) {
		User usr = (User) userDAO.getUser();
		ArrayList<ProtocolStepTemplate> allSteps = protocolTemplateHelper.getAllSteps(usr);
		ArrayList<HomeworkTemplate> templatelist = this.homeworkTemplateHelper.getList(usr);
		for (HomeworkTemplate hw : templatelist) {
			logger.info("Homework ID: " + hw.getId() + " Homework Name: " + hw.getName());
		}
		
		model.addAttribute("homework", templatelist);
		model.addAttribute("allSteps", allSteps);
		return "newProtocolTemplate";
	}
	
	
    @GetMapping("getStep/{id}")
    public ResponseEntity<?> getStep(@PathVariable int id) {
        User usr = (User) userDAO.getUser();
        ProtocolStepTemplate step = protocolStepTemplateHelper.getTemplate(usr, id);

        if (step != null) {
            return ResponseEntity.ok(step);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\": \"Step not found.\"}");
        }
    }
    
    
	@PostMapping("/createNewProtocolTemplate/")
	public ResponseEntity<String> createNewProtocolTemplate(@RequestBody ProtocolTemplate requestBody) {
		logger.info("Calling createNewProtocolTemplate");

		try {
			User usr = (User) userDAO.getUser();
			//logger.info("Due Date: " + requestBody.getDueDate());

			int call = protocolTemplateHelper.newProtocolTemplate(usr, requestBody);
			if (call == 1) {
				logger.info("Success!");
				return ResponseEntity.ok("Template processed successfully");
			} else {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body("An error occurred while creating the protocol.");
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("An error occurred while creating the protocol: " + e.getMessage());
		}
	}

	@PatchMapping("/saveProtocol/{id}")
	public ResponseEntity<String> saveProtocol(@PathVariable int id, @RequestBody String requestBody) {
		logger.info("Save Protocol called");
		try {
			User usr = (User) userDAO.getUser();
			String decodedBody = URLDecoder.decode(requestBody, StandardCharsets.UTF_8.toString());
			protocolTemplateHelper.saveProtocolTemplate(usr, id, decodedBody);
			return ResponseEntity.ok("Template processed successfully");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("An error occurred while saving the protocol: " + e.getMessage());
		}
	}

	@PatchMapping("/updateProtocolTemplate/{id}")
	public ResponseEntity<String> updateProtocolTemplate(@PathVariable int id, @RequestBody ProtocolTemplate updateRequest) {
	    User usr = (User) userDAO.getUser();
	    if (usr == null) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
	    }
	    logger.info("Status: " + updateRequest.getStatus());
	    String description = updateRequest.getDescription();
	    String name = updateRequest.getName();
	    String type = updateRequest.getType();
	    int years = updateRequest.getDueByYear();
	    int months = updateRequest.getDueByMonth();
	    int days = updateRequest.getDueByDay();
	    int sYears = updateRequest.getYearSchedule();
	    int sMonths = updateRequest.getMonthSchedule();
	    int sDays = updateRequest.getDaySchedule();
	  
	    String status = updateRequest.getStatus();
	    logger.info("years: " + years + " months: " + months + " days: " + days);
	    logger.info("Type: "+ type);
	    try {
	    	protocolTemplateHelper.updateProtocolTemplate(usr, id, updateRequest);
	        //protocolTemplateHelper.updateProtocolTemplateDescription(usr, id, description);
	        //protocolTemplateHelper.updateProtocolTemplateName(usr, id, name);
	        //protocolTemplateHelper.updateProtocolTemplateDueDate(usr, id, years, months, days);
	        //protocolTemplateHelper.updateProtocolTemplateStatus(usr, id, status);
	        //protocolTemplateHelper.updateProtocolTemplateScheduleDate(usr, id, sYears, sMonths, sDays);
	        return ResponseEntity.ok("Protocol updated successfully");
	    } catch (Exception e) {
	        logger.info("Error in updateProtocol:");
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("Error updating protocol: " + e.getMessage());
	    }
	}


	@PatchMapping("/saveStep/{stepId}")
	public ResponseEntity<String> saveStep(@PathVariable int stepId, @PathVariable String requestBody) {
		try {
			User usr = (User) userDAO.getUser();

			ProtocolStepTemplate pstep = protocolTemplateHelper.getStep(usr, stepId);

			if (pstep != null) {
				protocolTemplateHelper.saveTemplateStep(usr, stepId, requestBody);
				return ResponseEntity.ok().body("Step with ID " + stepId + " updated successfully.");
			} else {
				return ResponseEntity.notFound().build();
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("An error occurred while saving the step.");
		}
	}

	@GetMapping("/displayProtocol/{id}")
	public String displayProtocolPage(@PathVariable int id, Model model) {
		User usr = (User) userDAO.getUser();
		List<ProtocolStepTemplate> associatedSteps = new ArrayList<>();
		ProtocolTemplate protocol = new ProtocolTemplate();
		if (id > 0) {
			// Fetch the protocol by its ID
			protocol = protocolTemplateHelper.getTemplate(usr, id);
			associatedSteps = protocolTemplateHelper.getStepList(usr, id);
		}
		List<ProtocolStepTemplate> allSteps = protocolTemplateHelper.getAllSteps(usr); // used for the drop down
		for (ProtocolStepTemplate step: associatedSteps) {
			logger.info("Step: "+ step.getName() + " CatagoryID: "+ step.getCategoryId());
		}
		
		ArrayList<HomeworkTemplate> templatelist = homeworkTemplateHelper.getList(usr);

		
		model.addAttribute("homework", templatelist);
		model.addAttribute("protocolId", id);
		model.addAttribute("protocol", protocol);
		model.addAttribute("steps", associatedSteps);
		model.addAttribute("allSteps", allSteps);
		return "displayProtocol";
	}

	@GetMapping("/protocolTemplates")
	public String protocolTemplateListPage(HttpSession session, Model model) {
		User usr = (User) userDAO.getUser();
		List<ProtocolTemplate> listProtocols = protocolTemplateHelper.getList(usr);
		model.addAttribute("listProtocols", listProtocols);
		return "protocolTemplateList";
	}

	@GetMapping("/protocolStepTemplates")
	public String protocolStepTemplateList(Model model) {
		User usr = (User) userDAO.getUser();
		ArrayList<ProtocolStepTemplate> listSteps = protocolTemplateHelper.getAllSteps(usr);
		model.addAttribute("listSteps", listSteps);
		return "protocolStepTemplateList";
	}

	@PatchMapping("/addStepToProtocol/{protocolId}/{stepId}")
	public ResponseEntity<String> addStepToProtocol(@PathVariable Integer protocolId, @PathVariable Integer stepId) {
	    try {
	        User usr = (User) userDAO.getUser();

	        int result = protocolTemplateHelper.addTemplateStep(usr, protocolId, stepId);
	        if (result == 1) {
	            // Success
	            return ResponseEntity.ok().build();
	        } else if (result == 0) {
	            // Failed operation
	            logger.warn("Failed to assign step to template. Protocol ID: {}, Step ID: {}", protocolId, stepId);
	            return ResponseEntity.badRequest().body("Failed to assign step to template");
	        } else {
	            // Error
	            logger.error("Error in assigning step. Protocol ID: {}, Step ID: {}", protocolId, stepId);
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error in assigning step");
	        }
	    } catch (Exception e) {
	        logger.error("Error adding step to protocol. Protocol ID: {}, Step ID: {}, Error: {}", protocolId, stepId, e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding step to protocol");
	    }
	}


	@GetMapping("/editStep/{stepId}")
	public String edit_step(@PathVariable int stepId, Model model) {
		User usr = (User) userDAO.getUser();
		ArrayList<HomeworkTemplate> templatelist = this.homeworkTemplateHelper.getList(usr);
		if (stepId == 0) {
			// Create a new step with default values
			ProtocolStepTemplate step = new ProtocolStepTemplate();

			model.addAttribute("step", step);
		} else {
			ProtocolStepTemplate step = protocolTemplateHelper.getStep(usr, stepId);
			model.addAttribute("step", step);
		}

		model.addAttribute("homework", templatelist);
		model.addAttribute("stepId", stepId);
		return "edit_step";
	}

	@GetMapping("/profile")
	public String userProfile(Model model) {
		User usr = userDAO.getUser();

		int id = userHelper.getUserId(usr);
		User currentUser = userHelper.getUser(usr, id);
		model.addAttribute("user", currentUser);
		return "userProfile";
	}

	@GetMapping("/changeUserInfo")
	public String showChangeUserInfoForm(Model model) {
		User usr = userDAO.getUser();

		int id = userHelper.getUserId(usr);
		User currentUser = userHelper.getUser(usr, id);
		ArrayList<User> dependants = currentUser.getDependents();
		ArrayList<Household> householdList = userHelper.getHouseholdList(usr);
		

		model.addAttribute(dependants);
		model.addAttribute("user", currentUser);// Adds the object to the model to be accessed by the form
		model.addAttribute("id", id);
		return "changeUserInfo";
	}

	@GetMapping("/changeClientInfo/{id}") 
	public String changeClientInfo(@PathVariable int id, Model model) {
	    User usr = userDAO.getUser();
	    User client = userHelper.getUser(usr, id);
	    int clientId = client.getId();
	    String roles = String.join(",", client.getRoles());
	    logger.info(roles);
	    
	    ArrayList<User> dependents = client.getDependents();
	    ArrayList<User> userList = userHelper.getUserList(usr);
	    for (User dependent : dependents) {
	        logger.info("Id: " + dependent.getId() + " Name: " + dependent.getFirstName() + " " + dependent.getLastName());
	    }
		ArrayList<Household> householdList = userHelper.getHouseholdList(usr);
		Household myHousehold = userHelper.getHouseholdById(usr, client.getHouseholdId());
		
		
		
		
		for (Household household: householdList) {
			if (household.getHouseholdAccountsIds().contains(id)) {
				myHousehold = household;
				logger.info("My Household: "+ myHousehold.getName());
			}
			else {
				logger.info("No Household");
			}
		}
		
		
		ArrayList<Household> allHouseholds = userHelper.getHouseholdList(usr);
	    ArrayList<Integer> householdUserIds = new ArrayList<Integer>();

	    for (Household currentClient : allHouseholds) {
	        for (User user : currentClient.getHouseholdAccounts()) {
	            int userId = user.getId();
	            householdUserIds.add(userId);
	            logger.info("Adding Household: " + userId + "to list" );
	        }
	    }

	    // Retrieve all users to find those with CLIENT in roles
	    ArrayList<User> allUsers = userHelper.getUserList(usr);
	    ArrayList<Integer> clientRoleUserIds = new ArrayList<>();

	    for (User user : allUsers) {
	        if (user.getRoles().contains("CLIENT")) {
	            clientRoleUserIds.add(user.getId());

	        }
	    }

	    
	    ArrayList<User> usersToRemove = new ArrayList<>();

	    for (User user : userList) {
	        if (householdUserIds.contains(user.getId())) {
	            logger.info("Removing user: " + usr.getFirstName() + " " + usr.getLastName() + " because they are part of a household.");
	            usersToRemove.add(user);
	        } else if (!clientRoleUserIds.contains(user.getId())) {
	            logger.info("Removing user: " + usr.getFirstName() + " " + usr.getLastName() + " because they do not have the CLIENT role.");
	            usersToRemove.add(user);
	        }
	    }

	    String removalList = usersToRemove.stream()
	    		.map(user -> user.getFirstName() + " " + user.getLastName())
	            .collect(Collectors.joining(","));
	    
	    logger.info("Users Removed: "+ removalList);
	    userList.removeAll(usersToRemove);

	    String listPostFilter = userList.stream()
	            .map(user -> user.getFirstName() + " " + user.getLastName())
	            .collect(Collectors.joining(","));
	
	    logger.info("User List after filtering: " + listPostFilter);
		model.addAttribute("userList", userList);
		model.addAttribute("myHousehold", myHousehold);
		model.addAttribute("householdList",householdList);
	    model.addAttribute("dependents", dependents);
	    model.addAttribute("roles", roles);
	    model.addAttribute("user", client);
	    model.addAttribute("id", clientId);

	    return "changeUserInfo";
	}


	@GetMapping("reports")
	public String reports(Model model) {
		User usr = userDAO.getUser();
		ArrayList<ReportStat> rpt1 = new ArrayList<ReportStat>();
		rpt1 = reportHelper.getStatsReport(usr);

		model.addAttribute("rpt1", rpt1.get(0));
		return "reports";
	}

	@GetMapping("clients")
	public String displayClients(Model model) {
		User currentUser = userDAO.getUser();
		ArrayList<Household> households = userHelper.getHouseholdList(currentUser);

		model.addAttribute("UserList", households);

		return "displayClients";
	}

	@GetMapping("userAdminList")
	public String userAdminList(Model model) {
		User currentUser = userDAO.getUser();
		ArrayList<User> userList = userHelper.getUserList(currentUser);
		model.addAttribute("userList", userList);
		return "userAdminList";

	}

	@GetMapping("createDependant/{clientId}")
	public String createDependant(@PathVariable int clientId, Model model) {
		model.addAttribute("clientId", clientId);
		;
		return "addDependant";
	}

	public static int extractIdFromString(String response) {
		// Define a regex pattern to capture the numeric value of "id"
		Pattern pattern = Pattern.compile("\"id\":(\\d+)");
		Matcher matcher = pattern.matcher(response);

		// If the pattern matches, return the captured numeric value
		if (matcher.find()) {
			return Integer.parseInt(matcher.group(1));
		}

		// Return -1 if no ID is found
		return -1;
	}
	
	@RequestMapping(value = "addDependant/{clientId}", method = { RequestMethod.POST, RequestMethod.PATCH })
	public ResponseEntity<Object> addDependant(@PathVariable int clientId, @RequestBody User dependantUser) {
		logger.info("Calling addDependent from controller");
		User currentUser = userDAO.getUser();
		Map<String, String> errorResponse = new HashMap<>();
		String call = userHelper.addUser(currentUser, dependantUser);
		Household mainHouse = userHelper.getHouseholdById(currentUser, clientId);
		ArrayList<Integer> dependantList = new ArrayList<Integer>();

		int primaryContactId = 0;
		ArrayList<User> primaryContactList = mainHouse.getPrimaryContacts();
		if (!primaryContactList.isEmpty()) {
		    User primaryContact = primaryContactList.get(0);
		    primaryContactId = primaryContact.getId();
		    logger.info("PC ID: "+ primaryContactId);
		    primaryContact = userHelper.getUser(currentUser, primaryContactId);
		    for(User usr: primaryContact.getDependents()) {
		    	dependantList.add(usr.getId());
		    }
		    for(int id: dependantList) {
		    	logger.info("ID: " + id);
		    }
		   
		}
		else {
			errorResponse.put("error", "Household does not have a primary contact");
			return ResponseEntity.badRequest().body(errorResponse);
		}
		logger.info(call);
		if (call.startsWith("success")) {
			String[] parts = call.split(" ");
	        int dependantId = Integer.parseInt(parts[2]);
	        dependantList.add(dependantId);
	        logger.info("Id: "+ dependantId);
	        
	        Map<String, Object> response = new HashMap<>();
	        String dependantCall = userHelper.addDependant(currentUser, primaryContactId, dependantList);
	        if (dependantCall.startsWith("success")) {
	        	response.put("message", "Client added successfully");
	        	return ResponseEntity.ok(response);
	        }
	        
		} else {
			
			errorResponse.put("error", "Error processing the request");

			// Customize this logic to parse the actual error message
			if (call.contains("error")) {
				String errorMessage = "Error in Creating New User";
				errorResponse.put("error", errorMessage);
			}
			logger.warn(errorResponse.toString());
			return ResponseEntity.badRequest().body(errorResponse);
		}
		errorResponse.put("error", "Error Creating Dependat");
		return ResponseEntity.badRequest().body(errorResponse);
	}
	
	@PatchMapping("assignDependentToPrimaryContact/{clientId}/{dependantId}")
	public ResponseEntity<String> assignDepenedentToPrimaryContact(@PathVariable int clientId, @PathVariable int dependantId){
		User currentUser = userDAO.getUser();
		
		Household mainHouse = userHelper.getHouseholdById(currentUser, clientId);
		ArrayList<Integer> dependantList = new ArrayList<Integer>();

		int primaryContactId = 0;
		ArrayList<User> primaryContactList = mainHouse.getPrimaryContacts();
		if (!primaryContactList.isEmpty()) {
		    User primaryContact = primaryContactList.get(0);
		    primaryContactId = primaryContact.getId();
		    logger.info("PC ID: "+ primaryContactId);
		    primaryContact = userHelper.getUser(currentUser, primaryContactId);
		    for(User usr: primaryContact.getDependents()) {
		    	dependantList.add(usr.getId());
		    }
		    for(int id: dependantList) {
		    	logger.info("ID: " + id);
		    }
		   
		}
			dependantList.add(dependantId);

		
		try {
             String call2 = userHelper.addDependant(currentUser,primaryContactId, dependantList);
			return ResponseEntity.ok().body("Dependent Successfully Added!");

		} catch (Exception e) {
			System.err.println("Error uploading file: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error adding CoClient");
		}
	}
	@PatchMapping("assignDependentToUser/{clientId}/{dependantId}")
	public ResponseEntity<String> assignDepenedentToUser(@PathVariable int clientId, @PathVariable int dependantId){
		User currentUser = userDAO.getUser();
		ArrayList<Integer> dependantList = new ArrayList<Integer>();
		User client = userHelper.getUser(currentUser, clientId);
		
		for(User usr: client.getDependents()) {
	    	dependantList.add(usr.getId());
	    }
	    for(int id: dependantList) {
	    	logger.info("ID: " + id);
	    }
	   
	
		dependantList.add(dependantId);
		
		try {
            String call = userHelper.addDependant(currentUser,clientId, dependantList);
			return ResponseEntity.ok().body("Dependent Successfully Added!");

		} catch (Exception e) {
			System.err.println("Error uploading file: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error adding CoClient");
		}
		
	}
	
	
	@PatchMapping("addCoClient/{householdId}/{coClientId}")
	public ResponseEntity<String> addCoClient(@PathVariable int householdId, @PathVariable int coClientId) {
		logger.info("Calling Add CoClient for Household: " + householdId + " and CoClient ID: " + coClientId);
		User currentUser = userDAO.getUser();
		Household household = userHelper.getHouseholdById(currentUser, householdId);
		ArrayList<User> householdMemembers = household.getHouseholdAccounts();
		
		ArrayList<Integer> householdIds = new ArrayList<Integer>();
		
		for (User user: householdMemembers) {
			int uId = user.getId();
			householdIds.add(uId);
			}
		householdIds.add(coClientId);
		for (int id : householdIds) {
			logger.info("id: " + id);
		}
		
		try {
			userHelper.addHouseholdAccount(currentUser, household, householdIds);
			return ResponseEntity.ok().body("{\"message\": \"CoClient Successfully Added!\"}");

		} catch (Exception e) {
			System.err.println("Error uploading file: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("{\"error\": \"Error adding CoClient\"}");
		}

	}
	


	@GetMapping("protocolClients/{protocolId}")
	public String ProtocolClients(@PathVariable int ProtocolId, Model model) {
		User currentUser = userDAO.getUser();

		Protocol protocol = protocolHelper.getProtocol(currentUser, ProtocolId);
		ArrayList<Integer> userIds = protocol.getUsers();

		List<User> userList = new ArrayList<>();
		for (Integer id : userIds) {
			User user = userHelper.getUser(currentUser, id);
			userList.add(user);
		}
		model.addAttribute("UserList", userList);
		return "ProtocolClients";
	}

	@GetMapping("clientProfile/{clientId}")
	public String clientProfile(@PathVariable int clientId, Model model) {
	    User currentUser = userDAO.getUser();
	    Household household = userHelper.getHouseholdById(currentUser, clientId);
	    logger.info(household.getName());
	    ArrayList<User> primaryContact = household.getPrimaryContacts();
	    for(User pc: primaryContact) {
	        logger.info("PC Contact:"+ pc.getFirstName()+ " "+pc.getLastName());
	    }
	    ArrayList<ProtocolTemplate> pcolList = protocolTemplateHelper.getList(currentUser);
	    pcolList.removeIf(protocol -> !"LIVE".equals(protocol.getStatus())); 
	    ArrayList<Protocol> assignedProtocols = protocolHelper.getAssignedProtocols(currentUser, clientId);
	    ArrayList<User> userList = userHelper.getUserList(currentUser);
	    int userId = userHelper.getUserId(currentUser);
	    ArrayList<User> clientList = household.getHouseholdAccounts();
	    ArrayList<User> primaryContacts = household.getPrimaryContacts();
	    User primaryContactUser = !primaryContacts.isEmpty() ? primaryContacts.get(0) : null;

	    User pcUser = null;
	    int pcId = 0;
	    if (primaryContactUser != null) {
	        pcId = primaryContactUser.getId();
	        pcUser = userHelper.getUser(currentUser, pcId);
	        logger.info("Primary Contact User: " + pcId);
	    } else {
	    	
	        logger.info("No primary contact user found");
	    }
	    
	    ArrayList<User> dependantList = new ArrayList<User>();
	    ArrayList<Integer> dependantIds = new ArrayList<Integer>();

	    for (User client : clientList) {
	        logger.info("Client: " + client.getFirstName() + " " + client.getLastName() + " Id: " + client.getId());
	        int id = client.getId();
	        User detailedUser = userHelper.getUser(currentUser, id);
	        if (detailedUser.getDependents().isEmpty()) {
	            logger.info("No dependents");
	        } else {
	            for (User dependant : detailedUser.getDependents()) {
	                logger.info("Dependant Id:" + dependant.getId());
	                dependantIds.add(dependant.getId());
	            }
	        }
	    }

	    String dependantIdString = dependantIds.stream().map(String::valueOf).collect(Collectors.joining(","));
	    logger.info(dependantIdString);

	    Iterator<User> clientIterator = clientList.iterator();
	    while (clientIterator.hasNext()) {
	        User client = clientIterator.next();
	        if (dependantIds.contains(client.getId())) {
	            dependantList.add(client);
	            clientIterator.remove();
	        }
	    }

	    String dependantListString = dependantList.stream()
	            .map(dependant -> dependant.getFirstName() + " " + dependant.getLastName())
	            .collect(Collectors.joining(","));
	    logger.info(dependantListString);

	    ArrayList<Household> allHouseholds = userHelper.getHouseholdList(currentUser);
	    ArrayList<Integer> householdUserIds = new ArrayList<Integer>();

	    for (Household client : allHouseholds) {
	        for (User usr : client.getHouseholdAccounts()) {
	            int id = usr.getId();
	            householdUserIds.add(id);
	        }
	    }

	    // Retrieve all users to find those with CLIENT in roles
	    ArrayList<User> allUsers = userHelper.getUserList(currentUser);
	    ArrayList<Integer> clientRoleUserIds = new ArrayList<>();

	    for (User user : allUsers) {
	        if (user.getRoles().contains("CLIENT")) {
	            clientRoleUserIds.add(user.getId());

	        }
	    }

	    String listPreFilter = userList.stream()
	            .map(usr -> usr.getFirstName() + " " + usr.getLastName())
	            .collect(Collectors.joining(","));
	    
	    ArrayList<User> usersToRemove = new ArrayList<>();

	    for (User usr : userList) {
	        if (householdUserIds.contains(usr.getId())) {
	            //logger.info("Removing user: " + usr.getFirstName() + " " + usr.getLastName() + " because they are part of a household.");
	            usersToRemove.add(usr);
	        } else if (!clientRoleUserIds.contains(usr.getId())) {
	            //logger.info("Removing user: " + usr.getFirstName() + " " + usr.getLastName() + " because they do not have the CLIENT role.");
	            usersToRemove.add(usr);
	        }
	    }

	    userList.removeAll(usersToRemove);

	    String listPostFilter = userList.stream()
	            .map(usr -> usr.getFirstName() + " " + usr.getLastName())
	            .collect(Collectors.joining(","));
	
	    logger.info("User List after filtering: " + listPostFilter);
	    
	    
	    model.addAttribute("dependants", dependantList);
	    model.addAttribute("primaryContact", pcUser);
	    model.addAttribute("pcId", pcId);
	    model.addAttribute("primaryContactUser", primaryContactUser); // Fixed duplicate attribute key
	    model.addAttribute("userList", userList);
	    model.addAttribute("coClientList", clientList);
	    model.addAttribute("userId", userId);
	    model.addAttribute("client", household);
	    model.addAttribute("clientId", clientId);
	    model.addAttribute("protocolList", pcolList);
	    model.addAttribute("assignedProtocols", assignedProtocols);

	    return "clientProfile";
	}
	
/*	@GetMapping("/getHomeworkList/{id}")
	public String getHomeworkList(@PathVariable int id, Model model) {
	    User currentUser = userDAO.getUser();
	    ArrayList<Homework> homeworks = homeworkHelper.getHomeworkByProtocolId(currentUser, id);
	    model.addAttribute("homeworks", homeworks);
	    return "fragments/homeworkList :: homeworkListFragment";
	}*/

	@GetMapping("/getAllClientProtocols/{clientId}")
	public String getAllClientProtocols(@PathVariable int clientId, Model model) {
	    User currentUser = userDAO.getUser();
	    ArrayList<Protocol> listProtocols = protocolHelper.getAssignedProtocols(currentUser, clientId);
	    model.addAttribute("listProtocols", listProtocols);
	    return "fragments/allClientProtocols :: allClientProtocolsFragment";
	}

	@GetMapping("/getAnalysis/{id}")
	public String getAnalysis(@PathVariable int id, Model model) {
	    User currentUser = userDAO.getUser();
	    ArrayList<ProtocolStep> stepList = protocolHelper.getStepList(currentUser, id);
	    stepList.removeIf(step -> !step.getCategoryName().equals("Run Analysis"));
	    model.addAttribute("steps", stepList);
	    return "fragments/analysis :: analysisFragment";
	}

	@GetMapping("/getEducation/{id}")
	public String getEducation(@PathVariable int id, Model model) {
	    User currentUser = userDAO.getUser();
	    ArrayList<ProtocolStep> steps = protocolHelper.getStepList(currentUser, id);
	    model.addAttribute("steps", steps);
	    model.addAttribute("protocolId", id);
	    return "fragments/education :: educationFragment";
	}
	


	@PostMapping("/addClientToProtocol/{clientId}/{protocolTemplateId}")
	public ResponseEntity<Object> addClientToProtocol(@PathVariable int clientId, @PathVariable int protocolTemplateId,
			@RequestBody Protocol protocolRequest) {
		logger.info("Protocol Name: " + protocolRequest.getName() + " Protocol Due Date: " + protocolRequest.getDueDate());
		try {
			User currentUser = userDAO.getUser();
			int call = protocolHelper.addClient(currentUser, clientId, protocolTemplateId, protocolRequest); // Perform the
			logger.info("new protocolId" + call);// operation
			ArrayList<ProtocolStep> steps = protocolHelper.getStepList(currentUser, call);
			for(ProtocolStep step: steps) {
				logger.info("Step Id: " + step.getId());
				protocolHelper.updateStepNote(currentUser, call, step.getId() ,"CREATED");
			}

		} catch (Exception e) {
			logger.info("Error in addClientToProtocol:");
			e.printStackTrace(); // Print the stack trace to the console
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error adding client to protocol: " + e.getMessage());
		}
		return ResponseEntity.ok().build();
	}

	@GetMapping("clientProtocol/{pcolId}")
	public String clientProtocol(@PathVariable int pcolId, Model model) {
		User currentUser = userDAO.getUser();

		Protocol protocol = protocolHelper.getProtocol(currentUser, pcolId);
		model.addAttribute("protocol", protocol);
		return "clientProtocol";
	}

	@PatchMapping("addHomeworkTemplateToStep/{stepTemplateId}/{homeworkTemplateId}/")
	public ResponseEntity<Object> addHomeworkTemplateToStep(@PathVariable int stepTemplateId,
			@PathVariable int homeworkTemplateId, Model model) {
		User currentUser = userDAO.getUser();
		try {
			protocolStepTemplateHelper.addHomeworkTemplate(currentUser, stepTemplateId, homeworkTemplateId);
		} catch (Exception e) {
			logger.info("Error in addHomeworkTemplateToStep:");
			e.printStackTrace(); // Print the stack trace to the console
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error Homework Template to Step: " + e.getMessage());
		}
		return ResponseEntity.ok().build();
	}

	@GetMapping("/newClient/")
	public String newClient(Model model) {
		User currentUser = userDAO.getUser();
		ArrayList<User> list = userHelper.getUserList(currentUser);
		ArrayList<Household> allHouseholds = userHelper.getHouseholdList(currentUser);
		ArrayList<Integer> householdUserIds = new ArrayList<Integer>();
		for (Household household: allHouseholds) {
			for (User usr: household.getHouseholdAccounts()) {
				int id = usr.getId();
				householdUserIds.add(id);

			}
		}
		String listPreFilter = "";
		for (User usr: list) {
			listPreFilter += usr.getFirstName()+" "+usr.getLastName()+",";
		}

		list.removeIf(usr -> householdUserIds.contains(usr.getId()));
		list.removeIf(usr -> !usr.getRoles().contains("CLIENT"));
		String listPostFilter ="";
		for (User usr: list) {
			listPostFilter += usr.getFirstName()+" "+usr.getLastName()+",";
		}
		logger.info("User List before filtering: "+listPreFilter);
		logger.info("User List afer filtering: "+ listPostFilter);
		model.addAttribute("userList",list);
		return "newClient";
	}

	@GetMapping("/newUser/")
	public String newUser(Model model) {
		User currentUser = userDAO.getUser();
		ArrayList<Household> households = userHelper.getHouseholdList(currentUser);
		model.addAttribute("householdList",households);
		return "newUser";
	}

	@PostMapping("/addClient/")
	public ResponseEntity<Object> addClient(@RequestBody User requestBody) {
		User currentUser = userDAO.getUser();
		String call = userHelper.addUser(currentUser, requestBody);
		logger.info(call);
		if (call.startsWith("success")) {
			String[] parts = call.split(" ");
	        int clientId = Integer.parseInt(parts[2]);
	        logger.info("Id: "+ clientId);
	        Map<String, Object> response = new HashMap<>();
	        response.put("message", "Client added successfully");
	        response.put("clientId", clientId);

	        return ResponseEntity.ok(response);
		} else {
			Map<String, String> errorResponse = new HashMap<>();
			errorResponse.put("error", "Error processing the request");

			// Customize this logic to parse the actual error message
			if (call.contains("error")) {
				String errorMessage = "Error in Creating New User";
				errorResponse.put("error", errorMessage);
			}
			logger.warn(errorResponse.toString());
			return ResponseEntity.badRequest().body(errorResponse);
		}
	}
	
	@PostMapping("/addHousehold/")
	public ResponseEntity<Object>addHousehold(@RequestBody Household newHousehold){
		User currentUser = userDAO.getUser();
		String call = userHelper.newHousehold(currentUser, newHousehold); 
		if ( call.startsWith("Success")){
			return ResponseEntity.ok(Collections.singletonMap("message", "Client added successfully"));
		}
		else {
			return ResponseEntity.badRequest().body(null);
		}
	}
	@PostMapping("/addHousehold/{id}")
	public ResponseEntity<Object>addHousehold(@RequestBody Household newHousehold, @PathVariable int id){
			User currentUser = userDAO.getUser();

			ArrayList<Integer> primaryUser = new ArrayList<Integer>();
			primaryUser.add(id);

			newHousehold.setPrimaryContactsIds(primaryUser);
			
			logger.info(newHousehold.getName() + " "+ newHousehold.getDescription());
			String call = userHelper.newHousehold(currentUser, newHousehold); 
			if ( call.startsWith("Success")){
				return ResponseEntity.ok(Collections.singletonMap("message", "Client added successfully"));
			}
			else {
				return ResponseEntity.badRequest().body(null);
			}
		
	}

	@PostMapping("/updateUserPassword/{id}")
	public ResponseEntity<Object> updateUserPassword(@PathVariable int id, @RequestBody User requestBody) {
		User currentUser = userDAO.getUser();

		String call = userHelper.changeUserPassword(currentUser, id, requestBody.getPassword(),
				requestBody.getVerifypassword());
		if (call.startsWith("Success")) {
			return ResponseEntity.ok(Collections.singletonMap("message", "Client added successfully"));
		} else {
			Map<String, String> errorResponse = new HashMap<>();
			errorResponse.put("error", "Error processing the request");

			if (call.contains("error")) {

				String errorMessage = call.substring(call.indexOf("\"error\":\"") + 8,
						call.indexOf("\",", call.indexOf("\"error\":\"")));
				errorResponse.put("error", errorMessage);
			}
			logger.warn(errorResponse.toString());
			return ResponseEntity.badRequest().body(errorResponse);
		}
	}
	
	

	@PatchMapping("/updateUserDetails/{id}/{firstName}/{lastName}/{email}/{role}")
	public ResponseEntity<Object> updateUserDetails(@PathVariable int id, @PathVariable String firstName,
			@PathVariable String lastName, @PathVariable String email, @PathVariable int role, Model model) {
		User currentUser = userDAO.getUser();

		logger.info("Calling updateUserDetails");

		try {
			userHelper.updateUserDetails(currentUser, id, firstName, lastName, email, role); // if we don't want to
																								// change role, send
																								// role = 0

		} catch (Exception e) {
			logger.info("Error in updateUserDetails:");
			e.printStackTrace(); // Print the stack trace to the console
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error Update User Details: " + e.getMessage());
		}

		return ResponseEntity.ok().build();
	}

	@GetMapping("/getClientName/{id}/")
	@ResponseBody
	public ResponseEntity<String> getClientName(@PathVariable int id) {
		try {
			User currentUser = userDAO.getUser();
			if (currentUser == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Current user not found");
			}

			User assignedUser = userHelper.getUser(currentUser, id);

			if (assignedUser == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Assigned user not found");
			}

			String result = assignedUser.getFirstName() + " " + assignedUser.getLastName();
			logger.info(result);
			return ResponseEntity.ok(result);
		} catch (Exception e) {
			System.err.println("Error retrieving user name: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving user name");
		}
	}

	@GetMapping("/homeworkTemplates/")
	public String homeworkTemplates(Model model) {
		User currentUser = userDAO.getUser();
		ArrayList<HomeworkTemplate> templateList = homeworkTemplateHelper.getList(currentUser);
		model.addAttribute("templates", templateList);
		return "homeworkTemplates";
	}

    @GetMapping("/editHomeworkTemplate/")
    public String editHomeworkTemplate(Model model) {
        User currentUser = userDAO.getUser();
        ArrayList<ProtocolTemplate> pcolList = protocolTemplateHelper.getList(currentUser);
        
        ArrayList<HomeworkQuestion> questions = questionHelper.getHomeworkQuestions(currentUser);
        ArrayList<HomeworkQuestion> detailedQuestions = new ArrayList<>();

        for (HomeworkQuestion question : questions) {
            HomeworkQuestion dQuestion = questionHelper.getQuestion(currentUser, question.getQuestionId());
            detailedQuestions.add(dQuestion);
        }

        model.addAttribute("protocols", pcolList);
        model.addAttribute("questions", detailedQuestions);

        return "editHomeworkTemplate";
    }


	@GetMapping("viewHomeworkTemplate/{tempId}/")
	public String viewHomeworkTemplate(@PathVariable int tempId, Model model) {
		User currentUser = userDAO.getUser();

		HomeworkTemplate template = homeworkTemplateHelper.getTemplate(currentUser, tempId);
		ArrayList<HomeworkQuestion> questionList = questionHelper.getHomeworkQuestions(currentUser);
		model.addAttribute("questionList",questionList);
		model.addAttribute("template", template);
		model.addAttribute("templateId",tempId);
		
		return "viewHomeworkTemplate";
	}


    @PostMapping("/newHomeworkTemplate")
    public String saveHomeworkTemplate(@RequestBody String templateBody, RedirectAttributes redirectAttributes) {
        try {
            // Decode URL-encoded string
            String decodedBody = URLDecoder.decode(templateBody, StandardCharsets.UTF_8.toString());

            // Clean up to ensure the JSON starts correctly
            int jsonStartIndex = decodedBody.indexOf("\"name\"");
            String cleanJson = "{\n" + decodedBody.substring(jsonStartIndex).trim();

            logger.info("Cleaned JSON Data: " + cleanJson);

            User currentUser = userDAO.getUser();
            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("error", "User not found.");
                return "redirect:/login";
            }

            String call = homeworkTemplateHelper.newTemplate(currentUser, cleanJson);
            if ("Success".equals(call)) {
                redirectAttributes.addFlashAttribute("success", "Homework template saved successfully.");
                return "redirect:/homeworkTemplates/";
            } else {
                redirectAttributes.addFlashAttribute("error", "Error processing template: " + call);
                return "redirect:/editHomeworkTemplate/";
            }
        } catch (HttpClientErrorException.Conflict e) {
            String errorResponse = e.getResponseBodyAsString();
            System.err.println("Conflict Error: " + errorResponse);
            redirectAttributes.addFlashAttribute("error", "Homework Template already exists.");
            return "redirect:/editHomeworkTemplate/";
        } catch (Exception e) {
            System.err.println("Error in processing template: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error processing template: " + e.getMessage());
            return "redirect:/newHomeworkTemplate";
        }
    }

	@PatchMapping("/removeHomeworkQuestionFromTemplate/{tempId}/{questionId}")
	public ResponseEntity<String> removeHomeworkQuestionFromTemplate(@PathVariable int tempId,
			@PathVariable int questionID) {
		User currentUser = userDAO.getUser();
		HomeworkTemplate template = homeworkTemplateHelper.getTemplate(currentUser, tempId);
		List<HomeworkQuestionsTemplate> questions = template.getQuestions();
		for (HomeworkQuestionsTemplate question : questions) {
			if (question.getQuestionId() == questionID) {
				questions.remove(question);
			}
		}

		try {
			int apiCall = homeworkTemplateHelper.removeQuestionFromTemplate(currentUser, tempId, questions);
			if (apiCall == 1) {
				return ResponseEntity.ok("Template processed successfully");
			} else {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing template");
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing template");
		}
	}

	@DeleteMapping("/removeStepFromTemplate/{tempId}/{stepId}")
	public ResponseEntity<String> removeStepFromTemplate(@PathVariable int tempId, @PathVariable int stepId) {
		User currentUser = userDAO.getUser();

		try {
			int apiCall = protocolTemplateHelper.deleteProtocolStepTemplate(currentUser, tempId, stepId);
			if (apiCall == 1) {
				return ResponseEntity.ok("Template processed successfully");
			} else {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing template");
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing template");
		}
	}
	
	@GetMapping("/getUser/{id}")
	public ResponseEntity<Map<String, String>> getUser(@PathVariable int id) {
	    User currentUser = userDAO.getUser();
	    User usr = userHelper.getUser(currentUser, id);
	    if (usr != null) {
	        String userName = usr.getFirstName() + " " + usr.getLastName();
	        Map<String, String> response = new HashMap<>();
	        response.put("name", userName);
	        response.put("id", String.valueOf(id));
	        return ResponseEntity.ok(response);
	    } else {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	    }
	}


    @GetMapping("/api/protocols/{protocolId}/steps")
    public ResponseEntity<List<ProtocolStepTemplate>> getStepsForProtocol(@PathVariable int protocolId) {
        User currentUser = userDAO.getUser();
        List<ProtocolStepTemplate> steps = protocolTemplateHelper.getStepList(currentUser, protocolId);
        return ResponseEntity.ok(steps);
    }
	

    @DeleteMapping("/removeHomeworkFromStepTemplate/{stepId}/{homeworkId}")
    public ResponseEntity<Map<String, String>> removeHomeworkFromStepTemplate(@PathVariable int stepId, @PathVariable int homeworkId) {
        Map<String, String> response = new HashMap<>();
        User currentUser = userDAO.getUser();
        logger.info("Calling removeHomeworkFromStepTemplate with stepId: " + stepId + " and homeworkId: " + homeworkId);
        try {
            int apiCall = protocolStepTemplateHelper.deleteHomeworkTemplate(currentUser, stepId, homeworkId);
            if (apiCall == 1) {
                response.put("message", "Template processed successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "Error processing template");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            response.put("message", "Error processing template: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


	@GetMapping("/homeworkList/{id}")
	public String homeworkList(@PathVariable int id, Model model) {
		User currentUser = userDAO.getUser();

		ArrayList<Homework> homeworks = homeworkHelper.getHomeworkByProtocolId(currentUser, id);

		model.addAttribute("homeworks", homeworks);

		return "homeworkList";
	}

	@GetMapping("homeworkDisplay/{homeworkId}")
	public String homeworkDisplay(@PathVariable int homeworkId, Model model) {
		User currentUser = userDAO.getUser();

		Homework homework = homeworkHelper.getHomeworkByHomeworkId(currentUser, homeworkId);
		model.addAttribute("homework", homework);
		return "homeworkDisplay";
	}

	@PostMapping("/assignUserResponseToHomework/{homeworkId}")
	public ResponseEntity<String> assignUserResponseToHomework(@PathVariable int homeworkId,
			@RequestParam("questionIds") Long[] questionIds, // assumes questionIds and userResponses are the same
																// length
			@RequestParam("userResponses") String[] userResponses,
			@RequestParam("files") Optional<List<MultipartFile>> files) {
		User currentUser = userDAO.getUser();

		logger.info("Responses: " + userResponses);

		Iterator<Long> questionIdList = Arrays.asList(questionIds).iterator();
		Iterator<String> userResponseList = Arrays.asList(userResponses).iterator();

		Function<String, Optional<MultipartFile>> retrieveFile = files
				.<Function<String, Optional<MultipartFile>>>map(multipartFiles -> (filename) -> multipartFiles.stream()
						.filter(file -> Objects.equals(file.getOriginalFilename(), filename)).findFirst())
				.orElseGet(() -> (filename) -> Optional.empty());
		if (retrieveFile == null) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("{\"error\": Unable to retrieve file.\"}");
		}
		List<AssignedHomeworkResponse> homeworkResponses = new ArrayList<>();
		try {
			for (int i = 0; i < questionIds.length; i++) {
				AssignedHomeworkResponse assignedHomeworkResponse = new AssignedHomeworkResponse();

				assignedHomeworkResponse.setQuestionId(questionIdList.next());
				assignedHomeworkResponse.setUserResponse(userResponseList.next());

				homeworkResponses.add(assignedHomeworkResponse);
			}

			AssignedHomeworkResponseList assignedHomeworkResponseList = new AssignedHomeworkResponseList();
			assignedHomeworkResponseList.setResponses(homeworkResponses);

			homeworkHelper.submitHomeworkResponses(currentUser, homeworkId, assignedHomeworkResponseList,
					files.orElseGet(List::of));
			return ResponseEntity.ok().body("{\"message\": \"Success!\"}");
		} catch (NumberFormatException e) {
			System.err.println("Invalid question ID: " + e.getMessage());
			return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
		} catch (Exception e) {
			e.printStackTrace(); // This will print stack trace to the console
			System.err.println(
					"Error in processing responses: " + (e.getMessage() != null ? e.getMessage() : "Unknown Error"));
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("{\"error\": \"" + e.getMessage() + "\"}");
		}
	}

	@GetMapping("/uploadFile/{homeworkId}/{questionId}")
	public String uploadFil(@PathVariable int homeworkId, @PathVariable int questionId, Model model) {
		model.addAttribute("questionId", questionId);
		return "uploadFile";

	}

	@GetMapping("extraInfoPopUp/{userId}/{protocolId}")
	public String extraInfoPopUp(@PathVariable int userId, @PathVariable int protocolId, Model model) {
		User currentUser = userDAO.getUser();
		ProtocolTemplate pcol = protocolTemplateHelper.getTemplate(currentUser, protocolId);
		String name = pcol.getName();
		model.addAttribute("name",name);
		model.addAttribute("pcol", pcol);
		model.addAttribute("userId", userId);
		model.addAttribute("protocolId", protocolId);
		return "extraInfoPopUp";

	}

	@PatchMapping("/uploadFileToHomework/")
	public ResponseEntity<String> uploadFileToHomework(@RequestParam("file") MultipartFile file, String userResponse,
			@RequestParam("homeworkId") int homeworkId, @RequestParam("questionId") int questionId) {
		User currentUser = userDAO.getUser();

		logger.info("Calling upoloadFileToHomework");
		try {

			homeworkHelper.assigneFileUpload(currentUser, homeworkId, questionId, "File Upload", file);
			return ResponseEntity.ok().body("{\"message\": \"File successfully uploaded\"}");

		} catch (Exception e) {
			System.err.println("Error uploading file: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("{\"error\": \"Error uploading file\"}");
		}
	}
	
	

	@GetMapping("/allClientProtocols/{clientId}")
	public String allClientProtocols(@PathVariable int clientId, Model model) {
		User currentUser = userDAO.getUser();
		ArrayList<Protocol> listProtocols = protocolHelper.getAssignedProtocols(currentUser, clientId);
		for (Protocol pcol : listProtocols) {
			logger.info("Status: " + pcol.getStatus());
		}
		model.addAttribute("listProtocols", listProtocols);
		return "allClientProtocols";
	}

	@GetMapping("/protocolHistory/{clientId}/{name}")
	public String protocolHistory(@PathVariable int clientId, @PathVariable String name, Model model) {
		User currentUser = userDAO.getUser();
		List<Protocol> allProtocols = protocolHelper.getAssignedProtocols(currentUser, clientId);
		List<Protocol> filteredProtocols = allProtocols.stream().filter(p -> p.getName().equals(name))
				.collect(Collectors.toList());
		model.addAttribute("listProtocols", filteredProtocols);
		return "allClientProtocols";
	}

	@GetMapping("/homeworkReport/{clientId}")
	public String homeworkReport(@PathVariable int clientId, Model model) {
		User currentUser = userDAO.getUser();

		ArrayList<Homework> homeworks = homeworkHelper.getHomeworkByUser(currentUser, clientId);
		ArrayList<Homework> homeworkDetails = new ArrayList<>();
		for (Homework homework : homeworks) {
			Homework detailedHomework = homeworkHelper.getHomeworkByHomeworkId(currentUser, homework.getId());
			homeworkDetails.add(detailedHomework);
			logger.info("Added homework with ID: " + detailedHomework.getId());
		}
		model.addAttribute("homeworks", homeworkDetails);
		model.addAttribute("clientId", clientId);

		return "homeworkReport";
	}

	@PostMapping("/sendEmail/{userId}")
	public ResponseEntity<String> sendEmail(@PathVariable int userId, @RequestParam String subject,
			@RequestParam String message) {
		User currentUser = userDAO.getUser();
		User user = userHelper.getUser(currentUser, userId);

		if (message == null || message.isEmpty()) {
			return ResponseEntity.badRequest().body("Error: Message is empty");
		}

		try {
			SimpleMailMessage email = new SimpleMailMessage();
			email.setTo(user.getEmail());
			email.setSubject(subject);
			email.setText(message);
			mailSender.send(email);
			return ResponseEntity.ok().body("Success: Email Sent!");
		} catch (MailException e) {
			System.err.println("Error sending email: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: Sending Email!");
		}
	}

	@GetMapping("/protocolRecommendations/{id}/{clientId}")
	public String protocolRecomendations(@PathVariable int id, @PathVariable int clientId, Model model) {
		User currentUser = userDAO.getUser();

		Protocol protocol = protocolHelper.getProtocol(currentUser, id);
		model.addAttribute("protocol", protocol);
		model.addAttribute("protocolId", protocol.getId());
		model.addAttribute("clientId", clientId);
		return "protocolRecommendations";
	}

	// public ProtocolReport
	@GetMapping("/completionReport")
	public String completionReport(Model model) {
		User currentUser = userDAO.getUser();
		
		ArrayList<ProtocolTemplate> pcolList= protocolTemplateHelper.getList(currentUser);
		ArrayList<Household> householdList = userHelper.getHouseholdList(currentUser);
		ArrayList<ProtocolStepTemplate> stepTList = protocolTemplateHelper.getAllSteps(currentUser);
		ArrayList<ProtocolReport> userReports = reportHelper.protocolCompletionReportByHousehold(currentUser,householdList);
		ArrayList<ProtocolReport> templateReports = reportHelper.protocolCompletionReportByTemplate(currentUser,pcolList);
		ArrayList<ProtocolReport> stepReports = reportHelper.stepCompletionReportTemplate(currentUser, stepTList,householdList);
		ArrayList<ProtocolReport> clientStepReports = reportHelper.stepCompletionReportByHousehold(currentUser,householdList);

		model.addAttribute("userReports", userReports);
		model.addAttribute("stepReports", stepReports);
		model.addAttribute("reports", templateReports);
		model.addAttribute("userStepReports", clientStepReports);
		return "completionReport";
	}
	
	

	public ArrayList<ProtocolReport> stepCompletionReportByStep(ArrayList<ProtocolStepTemplate> stepList) {
		ArrayList<ProtocolReport> reports = new ArrayList<ProtocolReport>();
		return reports;
	}
	public ArrayList<ProtocolReport> stepCompletionReportByHousehold(ArrayList<Household> households){
		ArrayList<ProtocolReport> reports = new ArrayList<ProtocolReport>();
		return reports;
	}
	

	@PostMapping("/postRecommendations/{id}")
	public ResponseEntity<String> postRecommendation(@PathVariable int id, @RequestBody String recommendation) {
		User currentUser = userDAO.getUser();
		logger.info("Calling postRecommendation");
		if (recommendation.startsWith("\"") && recommendation.endsWith("\"")) {
			recommendation = recommendation.substring(1, recommendation.length() - 1);
		}

		logger.info("Reccomendation: " + recommendation);
		try {
			logger.info("Success!");
			protocolHelper.postProtocolComment(currentUser, id, "RECOMMENDATION", recommendation);
			return ResponseEntity.ok().body("{\"message\": \"Success: Recomendation Posted!\"}");

		} catch (Exception e) {
			logger.info("Error uploading file: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("{\"error\": \"Error Posting Recomendation!\"}");
		}
	}

	@GetMapping("/downloadFile/{guid}")
	public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable String guid) {
		User currentUser = userDAO.getUser();

		try {
			logger.info("Initiating file download...");
			return homeworkHelper.downloadResponseFile(currentUser, guid);
		} catch (IOException | URISyntaxException e) {
			System.err.println("Error Downloading File: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	@GetMapping("deleteTemplatePopUp/{type}/{id}")
	public String deleteTemplatePopUp(@PathVariable String type, @PathVariable int id, Model model) {
		model.addAttribute("id", id);
		model.addAttribute("type", type);
		return "deleteTemplatePopUp";

	}


	@DeleteMapping("deleteTemplate/{type}/{id}")
	public ResponseEntity<String> deleteTemplate(@PathVariable String type, @PathVariable int id) {
	    User currentUser = userDAO.getUser();
	    int call = 0;
	    String errorMessage = "";

	    switch (type) {
	        case "Protocol":
	            call = protocolHelper.deleteProtocol(currentUser, id);
	            errorMessage = "Error deleting Protocol";
	            break;
	        case "ProtocolTemplate":
	            call = protocolTemplateHelper.deleteProtocolTemplate(currentUser, id);
	            errorMessage = "Error deleting Protocol Template";
	            break;
	        case "StepTemplate":
	            call = protocolStepTemplateHelper.deleteStepTemplate(currentUser, id);
	            errorMessage = "Error deleting Step Template";
	            break;
	        case "Homework":
	            call = homeworkHelper.deleteHomework(currentUser, id);
	            errorMessage = "Error deleting Homework";
	            break;
	        case "HomeworkTemplate":
	            call = homeworkTemplateHelper.deleteHomeworkTemplate(currentUser, id);
	            errorMessage = "Error deleting Homework Template";
	            break;
	        case "HomeworkQuestion":
	            call = homeworkHelper.deleteHomeworkQuestion(currentUser, id);
	            errorMessage = "Error deleting Homework Question";
	            break;
	        default:
	            return new ResponseEntity<>("Invalid type", HttpStatus.BAD_REQUEST);
	    }

	    if (call != 1) {
	        logger.info("Failed to delete " + type + " with id " + id);
	        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
	    }

	    logger.info("Successfully deleted " + type + " with id " + id);
	    return new ResponseEntity<>("Deleted successfully", HttpStatus.OK);
	}

	@PatchMapping("/updateHomeworkQuestion/{id}")
	public ResponseEntity<String>updateHomeworkTemplate(@PathVariable int id, @RequestBody HomeworkQuestion question){
		User currentUser = userDAO.getUser();
		logger.info("Trigger Response id" + question.getTriggerProtocolId());
		logger.info("Homework Question: "+ question.getQuestion()+ " Status: " +  question.getStatus());
		try {
			questionHelper.updateHomeworkQuestion(currentUser, id, question);
			return ResponseEntity.ok().body("{\"message\": \"Homework Successfully Changed\"}");
		
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error Updating Question");
		}
	}
	
	@GetMapping("/newHomeworkQuestion")
	public String newHomeworkQuestion(Model model) {
		User currentUser = userDAO.getUser();
		ArrayList<ProtocolTemplate> protocolList = protocolTemplateHelper.getList(currentUser);
		model.addAttribute("protocolList",protocolList);
		return "newHomeworkQuestion";
	}
	
	@GetMapping("/displayHomeworkQustion/{id}")
	public String displayHomeworkQuestion(@PathVariable int id, Model model) {
		User currentUser = userDAO.getUser();

		HomeworkQuestion question = questionHelper.getQuestion(currentUser, id);
		ArrayList<ProtocolTemplate> protocolList = protocolTemplateHelper.getList(currentUser);
		ExpectedHomeworkResponses responses = question.getExpectedHomeworkResponses();
		if (responses.getResponses().isEmpty()) {
			logger.info("No Homework Responses");
		}
		for (HomeworkResponse response: responses.getResponses()) {
			logger.info("Response: "+response.getResponse());
			logger.info("ToolTip: "+response.getTooltip());
		}
		model.addAttribute("questionId",id);
		model.addAttribute("protocolList",protocolList);
		model.addAttribute("question",question);
		return "displayHomeworkQuestion";
	}

	@GetMapping("/homeworkQuestionList")
	public String homeworkQuestionList(Model model) {
		User currentUser = userDAO.getUser();
		ArrayList<HomeworkQuestion> questionList = homeworkTemplateHelper.getHomeworkQuestions(currentUser);
		model.addAttribute("questionList", questionList);

		return "homeworkQuestionList";
	}

    @PostMapping("/saveQuestion")
    public ResponseEntity<String> saveQuestion(@RequestBody HomeworkQuestion question) {
        User currentUser = userDAO.getUser();
        String body = ("QuestionID: "+ question.getQuestionId() + "Question Abbr: "+ question.getQuestionAbbreviation() + "Question Type"+ question.getQuestionType() + "Trigger Response: "+ question.getTriggerProtocolId());
        logger.info(body);

        try {
            questionHelper.newHomeworkQuestion(currentUser, question);
            return ResponseEntity.ok("{\"message\": \"Question successfully saved\"}");
        } catch (Exception e) {
            logger.error("Error saving question", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("{\"message\": \"Error saving question\"}");
        }
    }
    
    
    @PostMapping("/resetPassword")
    public ResponseEntity<String> resetPassword(@RequestBody User request) {
        String username = request.getUsername();
        String email = request.getEmail();
        
        logger.info("Calling resetPassword with: " + username + " email: " + email);
        try {
            logger.info("Try resetUserPasswordEmail");
            String result = userHelper.resetUserPasswordEmail(username, email);

            if (result.startsWith("Success")) {
                return ResponseEntity.ok(result);
            } else {
                logger.error("Error response from userHelper: " + result);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                     .body(result);
            }
        } catch (Exception e) {
            logger.error("Error sending reset password email", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error sending reset password email: " + e.getMessage());
        }
    }


    
    
    @GetMapping("/sendResetEmail/{id}")
    public ResponseEntity<String>sendResetEmail(@PathVariable int id){
    	User currentUser = userDAO.getUser();
    	User client = userHelper.getUser(currentUser, id);
    	String email = client.getEmail();
    	String username = client.getUsername();
    	try {
            userHelper.resetUserPasswordEmail(username,email);
            return ResponseEntity.ok("{\"message\": \"Question successfully saved\"}");
        } catch (Exception e) {
            logger.error("Error saving question", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("{\"message\": \"Error saving question\"}");
        }
    
    }

    
    @PatchMapping("/saveTemplateQuestions/{mode}/{tempId}/{qId}")
    public ResponseEntity<String>saveTemplateQuestions(@PathVariable String mode, @PathVariable int tempId, @PathVariable int qId) {
    	User currentUser = userDAO.getUser();
    	logger.info("Question Id: " + qId);
    	logger.info("Mode: "+mode);
   
    	HomeworkTemplate template = homeworkTemplateHelper.getTemplate(currentUser, tempId);
    	
    	List<HomeworkQuestionsTemplate> questionList = template.getQuestions();
    	ArrayList<Integer> questionIds = new ArrayList<Integer>();
    	String status = template.getStatus();
    	for (HomeworkQuestionsTemplate question: questionList) {
    		int questionId = question.getQuestionId();
    		questionIds.add(questionId);
    	}
        if (mode.equals("Delete")) {
            questionIds.remove(Integer.valueOf(qId));
        }
    	if(mode.equals("Add")) {
    		questionIds.add(qId);
    		logger.info("Question Id: " + qId + " added");
    	}
    	
    	try {
    		int call = homeworkTemplateHelper.updateTemplateQuestions(currentUser, tempId,status ,questionIds);
    		if(call>0) {
    		return new ResponseEntity<>("Question Added successfully", HttpStatus.OK);
    		}
    		else {
    			return new ResponseEntity<>("Error Updating Question: See Logs", HttpStatus.BAD_REQUEST);
    		}
		
		} catch (Exception e) {
			return new ResponseEntity<>("Error Updating Question: See Logs", HttpStatus.BAD_REQUEST);
		}
    	
    }
    
    @PatchMapping("/updateHomeworkTemplate/{id}")
    public ResponseEntity<String>updateHomeworktempalt(@RequestBody HomeworkTemplate template, @PathVariable int id){
    	User currentUser = userDAO.getUser();
    	try {
			int call = homeworkTemplateHelper.updateHomeworkTemplate(currentUser, id, template);
			if (call >0 ) {
			return ResponseEntity.ok().body("{\"message\": \"Homework Successfully Changed\"}");
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error Updating Template : exception"+e);
		}
    	return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error Updating Template");
    }
    
    @PostMapping("/createAndAssignDependent/{id}")
    public ResponseEntity<String> createAndAssignDependent(@RequestBody User dependent, @PathVariable int id) {
        User currentUser = userDAO.getUser();
        logger.info(dependent.getFirstName() + " " + dependent.getLastName() + " " + dependent.getEmail());
        String call = userHelper.createDependent(currentUser, dependent);
        try {
            if (call.contains("success")) {
                // Extract the ID using regular expression
                Pattern pattern = Pattern.compile("id: (\\d+)");
                Matcher matcher = pattern.matcher(call);
                if (matcher.find()) {
                    int dependentId = Integer.parseInt(matcher.group(1));
                    logger.info("DependentID: " + dependentId);
                    
                    User primeUser = userHelper.getUser(currentUser, id);
                    ArrayList<User> dependents = primeUser.getDependents();
                    
                    // Logging existing dependents
                    String dependentAsString = dependents.stream()
                        .map(dep -> dep.getFirstName() + " " + dep.getLastName())
                        .collect(Collectors.joining(", "));
                    logger.info(dependentAsString);
                    
                    // Creating a list of dependent IDs
                    ArrayList<Integer> dependentIdList = dependents.stream()
                        .map(User::getId)
                        .collect(Collectors.toCollection(ArrayList::new));
                    dependentIdList.add(dependentId);
                    
                    String call2 = userHelper.addDependant(currentUser, id, dependentIdList);
                    if (call2.contains("success")) {
                        return ResponseEntity.ok().body("Dependent Successfully Created and Assigned");
                    }
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: Unable to parse dependent ID");
                }
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: Unknown error occurred");
    }

   
    
    
    @PostMapping("/createUserAndDependents/{id}")
    public ResponseEntity<String> createUserAndDependents(@RequestBody User user, @RequestBody ArrayList<User> dependents, @PathVariable int id) {
        User currentUser = userDAO.getUser();
        String call = userHelper.addUser(currentUser, user);
        ArrayList<Integer> dependentIdList = new ArrayList<>();
        
        try {
            if (call.contains("success")) {
                for (User dependent: dependents) {
                    String call2 = userHelper.createDependent(currentUser, dependent);
                    if (call2.contains("success")) {
                        // Assuming call2 returns a response string that includes the ID like "success: ID=123"
                        String[] parts = call2.split("id: ");
                        if (parts.length == 2) {
                            int dependentId = Integer.parseInt(parts[1].trim());
                            dependentIdList.add(dependentId);
                        } else {
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: Failed to parse dependent ID");
                        }
                        String call3 = userHelper.addDependant(currentUser, id, dependentIdList);
                        if (call3.contains("success")) {
                        	return ResponseEntity.ok().body("User and Dependents Successfully Created and Assigned");
                        	
                        }
                    } else {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: Unknown error occurred");
                    }
                }
                // Return success response with list of dependent IDs
                return ResponseEntity.status(HttpStatus.OK).body("Success: Dependent IDs = " + dependentIdList.toString());
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: Unknown error occurred");
    }
    
    
    @PatchMapping("/promoteToPrimaryContact/{householdId}/{clientId}")
    public ResponseEntity<String> promoteToPrimaryContact(@PathVariable int householdId, @PathVariable int clientId) {
        User currentUser = userDAO.getUser();
        try {
            String call = userHelper.promoteToPrimaryContact(currentUser, householdId, clientId);
            if (call.contains("success")) {
                return ResponseEntity.ok().body("Primary Contact Successfully Updated");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error");
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: Unknown error occurred");
    }

    
    @PatchMapping("/changeStatus/{type}/{id}/{status}")
    public ResponseEntity<String>changeStatus(@PathVariable String type, @PathVariable int id, @PathVariable String status ){
    	User currentUser = userDAO.getUser();
    	String newStatus = "";
 
    	if (status.equals("LIVE")){
    		newStatus = "INACTIVE";
    	}
    	else {
    		newStatus = "LIVE";
    	}
    	if (type.equals("Protocol Template"))
	    	try {
	    		int call = protocolTemplateHelper.changeStatus(currentUser, id, newStatus);
	    		if (call > 0) {
	                return ResponseEntity.ok().body("Primary Contact Successfully Updated");
	            }
	    	}catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error");
	        }
    	if(type.equals("Homework Template")) {	
    		try {
	    		int call = homeworkTemplateHelper.changeStatus(currentUser, id, newStatus);
	    		if (call > 0) {
	                return ResponseEntity.ok().body("Primary Contact Successfully Updated");
	            }
	    	}catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error");
	        }
    	}
    	if(type.equals("Step Template")) {
    		try {
	    		int call = protocolStepTemplateHelper.changeStatus(currentUser, id, newStatus);
	    		if (call > 0) {
	                return ResponseEntity.ok().body("Primary Contact Successfully Updated");
	            }
	    	}catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error");
	        }
    	}
    	if(type.equals("Homework Question")) {
    		try {
	    		int call = questionHelper.changeStatus(currentUser, id, newStatus);
	    		if (call > 0) {
	                return ResponseEntity.ok().body("Primary Contact Successfully Updated");
	            }
	    	}catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error");
	        }
    	}
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: Unknown error occurred");
    }
    
    @PostMapping("/uploadFile")
    public ResponseEntity<Integer> submit(@RequestParam("files") MultipartFile file, @RequestParam("pcolId") int pcolId, @RequestParam("stepId") int stepId) {
        User currentUser = userDAO.getUser();
        int call = -1;
        try {
            call = protocolHelper.assigneFileUpload(currentUser, pcolId, stepId, file);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(call);
        }
        return ResponseEntity.ok(call);
    }

    @GetMapping("/getHomeworkList/{id}")
    @ResponseBody
    public ArrayList<Homework> getHomeworkList(@PathVariable int id) {
    	logger.info("calling getHomeworkList");
        User currentUser = userDAO.getUser();
        ArrayList<Homework> homeworks =homeworkHelper.getHomeworkByProtocolId(currentUser, id);
        for (Homework homework : homeworks) {
        	logger.info("Homework: "+ homework.getName());
        }
        return homeworks;
    }
    
    @GetMapping("/testCards/{id}")
    public String testCards(@PathVariable int id,  Model model) {
    	User currentUser = userDAO.getUser();
    	ArrayList<Protocol> assignedProtocols = protocolHelper.getAssignedProtocols(currentUser, id);
		
    	model.addAttribute("pcol",assignedProtocols);
    	return "testCards";
    }
    
    @GetMapping("/getStepListFiltered/{type}")
    public ResponseEntity<List<ProtocolStepTemplate>> getStepListFiltered(@PathVariable int type) {
        User currentUser = userDAO.getUser();
        ArrayList<ProtocolStepTemplate> stepList = protocolTemplateHelper.getAllSteps(currentUser);
        logger.info("Type: "+ type);
        if(stepList.isEmpty()) {
        	logger.info("List is empty");
        }

        if (type > 0) {
        	stepList.removeIf(step->!step.getStatus().equals("LIVE"));
            stepList.removeIf(step -> step.getCategoryId() != type);
        }
        stepList.removeIf(step->!step.getStatus().equals("LIVE"));

        for (ProtocolStepTemplate template: stepList) {
        	logger.info("Step: "+ template.getName() + " Type: "+ template.getCategoryId() + " Type ID: " +template.getCategoryId() + " Status: " + template.getStatus());
        	
        }

        return ResponseEntity.ok(stepList);
    }

}
