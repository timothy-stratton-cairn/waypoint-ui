package com.cairn.ui;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
import com.cairn.ui.model.Homework;
import com.cairn.ui.model.HomeworkQuestion;
import com.cairn.ui.model.HomeworkQuestionsTemplate;
import com.cairn.ui.model.HomeworkTemplate;
import com.cairn.ui.model.Household;
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

	// @Autowired
	// HomeworkHelper homeworkHelper;

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
		for (ProtocolStats stat : stats) {
			logger.info("Temp ID: " + stat.getTemplateId() + " Number Of Steps: " + stat.getNumSteps() + " Progress: "
					+ stat.getProgress());
			// logger.info("Temp ID: " + stat.getTemplateId() + " Number Of Steps: "+
			// stat.getNumSteps() + " Progress: " + stat.getProgress());
		}
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

	/**
	 * Handle a request to view the Protocol details.
	 * 
	 * @return
	 */
	@GetMapping("/viewProtocol/{pcolId}/{userId}")
	public String viewProtocol(Model model, @PathVariable int pcolId, @PathVariable int userId) {
		User currentUser = userDAO.getUser();
		Protocol protocol = protocolHelper.getProtocol(currentUser, pcolId);
		HomeworkHelper homeworkHelper = new HomeworkHelper();
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
		HomeworkHelper homeworkHelper = new HomeworkHelper();
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
		HomeworkHelper homeworkHelper = new HomeworkHelper();
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

	@PatchMapping("/updateProtocolCommentsGoalsAndProgress/{protocolId}/{comment}/{goal}/{progress}/{status}/{date}")
	public ResponseEntity<Object> updateProtocolComment(@PathVariable int protocolId, @PathVariable String comment,
			@PathVariable String goal, @PathVariable String progress, @PathVariable String status,
			@PathVariable String date, Model model) {

		User currentUser = userDAO.getUser();
		logger.info("Status: " + status);
		try {
			protocolHelper.postProtocolComment(currentUser, protocolId, "COMMENT", comment);
			protocolHelper.updateProtocolGoal(currentUser, protocolId, goal);
			protocolHelper.updateProtocolProgress(currentUser, protocolId, progress);
			protocolHelper.updateProtocolStatus(currentUser, protocolId, status);
			protocolHelper.updateProtocolDueDate(currentUser, protocolId, date);
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
				userNames.put(protocol.getId(), "User is Empty ");
				userIds.put(protocol.getId(), 0);
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

		List<ProtocolStepTemplate> fullStepList = new ArrayList<ProtocolStepTemplate>(); // there's a method to this
																							// madness. getStepList
																							// doesn't get homeworks
		int dueByDays = pcol.getDueByDay();

		for (ProtocolStepTemplate step : listSteps) {
			int stepId = step.getId();
			ProtocolStepTemplate fullStep = protocolTemplateHelper.getStep(usr, stepId);
			fullStepList.add(fullStep);
		}

		for (ProtocolStepTemplate step : fullStepList) {

			logger.info("Step id " + step.getCategoryId());
		}
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
		model.addAttribute("pcolId", null);
		model.addAttribute("homework", templatelist);
		model.addAttribute("step", step);

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
		model.addAttribute("allSteps", allSteps);
		return "newProtocolTemplate";
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

	@PatchMapping("/updateProtocol/{id}")
	public ResponseEntity<String> updateProtocol(@PathVariable int id, @RequestBody ProtocolTemplate updateRequest) {
	    User usr = (User) userDAO.getUser();
	    if (usr == null) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
	    }
	    logger.info("Status: " + updateRequest.getStatus());
	    String description = updateRequest.getDescription();
	    String name = updateRequest.getName();
	    int years = updateRequest.getDueByYear();
	    int months = updateRequest.getDueByMonth();
	    int days = updateRequest.getDueByDay();
	    int sYears = updateRequest.getYearSchedule();
	    int sMonths = updateRequest.getMonthSchedule();
	    int sDays = updateRequest.getDaySchedule();
	    String status = updateRequest.getStatus();
	    logger.info("years: " + years + " months: " + months + " days: " + days);
	    try {
	        protocolTemplateHelper.updateProtocolTemplateDescription(usr, id, description);
	        protocolTemplateHelper.updateProtocolTemplateName(usr, id, name);
	        protocolTemplateHelper.updateProtocolTemplateDueDate(usr, id, years, months, days);
	        protocolTemplateHelper.updateProtocolTemplateStatus(usr, id, status);
	        protocolTemplateHelper.updateProtocolTemplateScheduleDate(usr, id, sYears, sMonths, sDays);
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
		return "ProtocolStepTemplateList";
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
				return ResponseEntity.badRequest().body("Failed to assign step to template");
			} else {
				// Error
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error in assigning step");
			}
		} catch (Exception e) {

			System.err.println("Error adding step to protocol: " + e.getMessage());
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
		model.addAttribute("user", currentUser);// Adds the object to the model to be accessed by the form
		model.addAttribute("id", id);
		return "changeUserInfo";
	}

	@GetMapping("/changeClientInfo/{id}") // basically the same from above but for clients that aren't the current user,
											// requires a different page.
	public String changeClientInfo(@PathVariable int id, Model model) {
		User usr = userDAO.getUser();
		User client = userHelper.getUser(usr, id);
		int clientId = client.getId();
		String roles = "";
		for (String role : client.getRoles()) {
			roles = roles + role;
		}
		logger.info(roles);
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
		User client = userHelper.getUser(currentUser, clientId);
		String userDependant = userHelper.addUser(currentUser, dependantUser);
		int dependantId = extractIdFromString(userDependant);

		User newDependant = userHelper.getUser(currentUser, dependantId);
		logger.info("Dependant Created: " + newDependant + "Dependant ID " + dependantId);
		ArrayList<User> dependants = client.getDependents();
		dependants.add(newDependant);

		try {
			userHelper.addDependant(currentUser, client, dependants);

		} catch (Exception e) {
			logger.info("Error in addClientToProtocol:");
			e.printStackTrace(); // Print the stack trace to the console
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error adding client to protocol: " + e.getMessage());
		}
		return ResponseEntity.ok().build();
	}

	@PatchMapping("addCoClient/{clientId}/{coClientId}")
	public ResponseEntity<String> addCoClient(@PathVariable int clientId, @PathVariable int coClientId) {
		logger.info("Calling Add CoClient for Household: " + clientId + " and CoClient ID: " + coClientId);
		User currentUser = userDAO.getUser();
		Household household = userHelper.getHouseholdById(currentUser, clientId);
		ArrayList<Integer> householdIds = new ArrayList<Integer>();
		for (User usr : household.getHouseholdAccounts()) {
			householdIds.add(usr.getId());
			householdIds.add(coClientId);

		}
		for (int id : householdIds) {
			logger.info("id: " + id);
		}

		try {
			userHelper.addHouseholdAccount(currentUser, clientId, householdIds);
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
		ArrayList<ProtocolTemplate> pcolList = protocolTemplateHelper.getList(currentUser);
		ArrayList<Protocol> assignedProtocols = protocolHelper.getAssignedProtocols(currentUser, clientId);
		ArrayList<User> userList = userHelper.getUserList(currentUser);
		int userId = userHelper.getUserId(currentUser);
		ArrayList<User> clientList = household.getHouseholdAccounts();
		ArrayList<User> primaryContact = household.getPrimaryContacts();
		User primaryContactUser = !primaryContact.isEmpty() ? primaryContact.get(0) : null;

		for (User client : clientList) {
			logger.info("Client Name: " + client.getFirstName() + client.getLastName());
		}
		model.addAttribute("primaryContact", primaryContactUser);
		model.addAttribute("userList", userList);
		model.addAttribute("coClientList", clientList);
		model.addAttribute("userId", userId);
		model.addAttribute("client", household);
		model.addAttribute("clientId", clientId);
		model.addAttribute("protocolList", pcolList);
		model.addAttribute("assignedProtocols", assignedProtocols);
		for (Protocol pcol : assignedProtocols) {
			logger.info("Status: " + pcol.getStatus());
		}

		return "clientProfile";
	}

	@PostMapping("/addClientToProtocol/{clientId}/{protocolTemplateId}")
	public ResponseEntity<Object> addClientToProtocol(@PathVariable int clientId, @PathVariable int protocolTemplateId,
			@RequestBody Protocol protocolRequest) {
		logger.info("Protocol Name: " + protocolRequest.getName() + " Protocol Due Date: " + protocolRequest.getDueDate());
		try {
			User currentUser = userDAO.getUser();
			protocolHelper.addClient(currentUser, clientId, protocolTemplateId, protocolRequest); // Perform the
																									// operation

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
		return "newClient";
	}

	@GetMapping("/newUser/")
	public String newUser(Model model) {
		return "newUser";
	}

	@PostMapping("/addClient/")
	public ResponseEntity<Object> addClient(@RequestBody User requestBody) {
		User currentUser = userDAO.getUser();
		String call = userHelper.addUser(currentUser, requestBody);

		if (call.startsWith("Success")) {
			logger.info(call);
			return ResponseEntity.ok(Collections.singletonMap("message", "Client added successfully"));
		} else {
			Map<String, String> errorResponse = new HashMap<>();
			errorResponse.put("error", "Error processing the request");

			// Customize this logic to parse the actual error message
			if (call.contains("error")) {
				// Extract the "error" field and remove unnecessary details
				String errorMessage = call.substring(call.indexOf("\"error\":\"") + 8,
						call.indexOf("\",", call.indexOf("\"error\":\"")));
				errorResponse.put("error", errorMessage);
			}
			logger.warn(errorResponse.toString());
			return ResponseEntity.badRequest().body(errorResponse);
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

	@GetMapping("editHomeworkTemplate/")
	public String editHomeworkTemplate(Model model) {
		User currentUser = userDAO.getUser();
		ArrayList<ProtocolTemplate> pcolList = protocolTemplateHelper.getList(currentUser);
		HomeworkQuestionHelper helper = new HomeworkQuestionHelper();
		ArrayList<HomeworkQuestion> questions = helper.getHomeworkQuestions(currentUser);
		ArrayList<HomeworkQuestion> detailedQuestions = new ArrayList<HomeworkQuestion>();
		for (HomeworkQuestion question: questions) {
			//logger.info("Question Id: "+ question.getQuestionId());
			HomeworkQuestion dQuestion = helper.getQuestion(currentUser, question.getQuestionId());
			detailedQuestions.add(dQuestion);
			
		}
		if (detailedQuestions.isEmpty()) {
			//logger.info("No Questions returned");
		}
		else {
			for (HomeworkQuestion dQuestion: detailedQuestions) {
				//logger.info("Question ID : "+ dQuestion.getQuestionId() + " Question: " + dQuestion.getQuestion());
			}
		}
		model.addAttribute("protocols", pcolList);
		model.addAttribute("questions",detailedQuestions);
		for (ProtocolTemplate pcol : pcolList) {
			logger.info("Name: " + pcol.getName());
		}
		return "editHomeworkTemplate";
	}

	@GetMapping("viewHomeworkTemplate/{tempId}/")
	public String viewHomeworkTemplate(@PathVariable int tempId, Model model) {
		User currentUser = userDAO.getUser();

		HomeworkTemplate template = homeworkTemplateHelper.getTemplate(currentUser, tempId);
		model.addAttribute("template", template);
		return "viewHomeworkTemplate";
	}

	@PostMapping("/newHomeworkTemplate")
	public String saveHomeworkTemplate(@RequestBody String templateBody, RedirectAttributes redirectAttributes) {
		try {
			// Decode URL-encoded string
			String decodedBody = URLDecoder.decode(templateBody, StandardCharsets.UTF_8.toString());

			// Log the raw decoded body for debugging

			// Clean up to ensure the JSON starts correctly
			int jsonStartIndex = decodedBody.indexOf("\"name\"");

			// Extract the JSON correctly including the first curly brace and trim any
			// leading/trailing whitespace
			String cleanJson = "{\n" + decodedBody.substring(jsonStartIndex).trim();

			logger.info("Cleaned JSON Data: " + cleanJson);

			User currentUser = userDAO.getUser();

			String call = homeworkTemplateHelper.newTemplate(currentUser, cleanJson);
			if (currentUser == null) {
				redirectAttributes.addFlashAttribute("error", "User not found.");
				return "redirect:/login";
			}
			if (call == "Success") {
				redirectAttributes.addFlashAttribute("success", "Homework template saved successfully.");
				return "redirect:/homeworkTemplates/";
			} else {
				redirectAttributes.addFlashAttribute("error", "Error processing template: " + call);
				return "redirect:/editHomeworkTemplate/";
			}
		} catch (HttpClientErrorException.Conflict e) {
			// Specifically handle the conflict exception
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
		HomeworkHelper helper = new HomeworkHelper(); // this will need to be autowired at some point
		ArrayList<Homework> homeworks = helper.getHomeworkByProtocolId(currentUser, id);

		model.addAttribute("homeworks", homeworks);

		return "homeworkList";
	}

	@GetMapping("homeworkDisplay/{homeworkId}")
	public String homeworkDisplay(@PathVariable int homeworkId, Model model) {
		User currentUser = userDAO.getUser();
		HomeworkHelper hwHelper = new HomeworkHelper();
		Homework homework = hwHelper.getHomeworkByHomeworkId(currentUser, homeworkId);
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
		HomeworkHelper helper = new HomeworkHelper();
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

			helper.submitHomeworkResponses(currentUser, homeworkId, assignedHomeworkResponseList,
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
		model.addAttribute("pcol", pcol);
		model.addAttribute("userId", userId);
		model.addAttribute("protocolId", protocolId);
		return "extraInfoPopUp";

	}

	@PatchMapping("/uploadFileToHomework/")
	public ResponseEntity<String> uploadFileToHomework(@RequestParam("file") MultipartFile file, String userResponse,
			@RequestParam("homeworkId") int homeworkId, @RequestParam("questionId") int questionId) {
		User currentUser = userDAO.getUser();
		HomeworkHelper helper = new HomeworkHelper();
		logger.info("Calling upoloadFileToHomework");
		try {

			helper.assigneFileUpload(currentUser, homeworkId, questionId, "File Upload", file);
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
		HomeworkHelper helper = new HomeworkHelper();
		ArrayList<Homework> homeworks = helper.getHomeworkByUser(currentUser, clientId);
		ArrayList<Homework> homeworkDetails = new ArrayList<>();
		for (Homework homework : homeworks) {
			Homework detailedHomework = helper.getHomeworkByHomeworkId(currentUser, homework.getId());
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
		ArrayList<ProtocolReport> reports = new ArrayList<ProtocolReport>();
		ArrayList<ProtocolTemplate> templates = protocolTemplateHelper.getList(currentUser);
		ArrayList<User> users = userHelper.getUserList(currentUser);
		Map<String, ArrayList<ProtocolStep>> stepMap = new HashMap<>();
		Map<Integer, ArrayList<Protocol>> userMap = new HashMap<>();
		Map<Integer, ArrayList<ProtocolStep>> userStepMap = new HashMap<>();

		ArrayList<ProtocolReport> userReports = new ArrayList<>();
		ArrayList<ProtocolReport> stepReports = new ArrayList<>();
		ArrayList<ProtocolReport> userStepReports = new ArrayList<>();
		for (ProtocolTemplate template : templates) {
			ArrayList<Protocol> tempPcolList = protocolHelper.getListbyTemplateId(currentUser, template.getId());
			for (Protocol pcol : tempPcolList) {
				logger.info("Protocol: " + pcol.getId() + " HouseholdId: " + pcol.getUserId());
			}
			if (tempPcolList.isEmpty())
				continue; // Skip empty lists

			// Filter out protocols with daysToComplete < 0
			List<Protocol> completedProtocols = tempPcolList.stream().filter(p -> p.getDaysToComplete() >= 0)
					.collect(Collectors.toList());
			for (Protocol protocol : completedProtocols) {
				logger.info("Protocol in User List:" + protocol.getName() + "Protocol Users: " + protocol.getUserId());
			}
			for (Protocol protocol : completedProtocols) { // for each protocol

				for (int userId : protocol.getUsers()) {
					logger.info("UserID: " + userId); // for each userid
					if (!userMap.containsKey(userId)) { // set check to see if user is in the UserMap already if it is
														// just add the protocol to the map
						if (users.contains(userId)) { // if it's not verify that the userID is real and not a bit of
														// junk data
							ArrayList<Protocol> tempList = new ArrayList<Protocol>(); // then add a new entry into the
																						// map
							tempList.add(protocol);
							userMap.put(userId, tempList);
						}
					} else {
						userMap.get(userId).add(protocol);
					}
					if (!userStepMap.containsKey(userId)) {
						if (protocol.getSteps() != null) {
							userStepMap.put(userId, protocol.getSteps());
						}
					} else {
						for (ProtocolStep step : protocol.getSteps()) {
							boolean stepExists = false;
							for (ProtocolStep existingStep : userStepMap.get(userId)) {
								if (existingStep.getId() == step.getId()) {
									stepExists = true;
									break;
								}
							}

							if (!stepExists) {
								userStepMap.get(userId).add(step);
							}
						}
					}
				}

				for (ProtocolStep step : protocol.getSteps()) { // this does the same thing but we don't need to bother
																// with verifying
					if (step.getDaysToComplete() >= 0) {
						if (!stepMap.containsKey(step.getName())) { // if the step is real as it's a full step object in
																	// the array not just an id
							ArrayList<ProtocolStep> tempList = new ArrayList<ProtocolStep>(); // using names instead of
																								// ids because we can't
																								// change step names and
																								// I
							tempList.add(step); // don't have a way to get associated stepIds from this call, this is
												// good enough
							stepMap.put(step.getName(), tempList);
						} else {
							stepMap.get(step.getName()).add(step);
						}
					}
				}
			}
			if (completedProtocols.isEmpty())
				continue;

			completedProtocols.sort(Comparator.comparingInt(Protocol::getDaysToComplete));

			int size = completedProtocols.size();
			int meanDays = completedProtocols.stream().mapToInt(Protocol::getDaysToComplete).sum() / size;
			int medianDays = completedProtocols.get(size / 2).getDaysToComplete();
			int high = completedProtocols.get(size - 1).getDaysToComplete();
			int low = completedProtocols.get(0).getDaysToComplete();

			int highId = completedProtocols.get(size - 1).getId();
			int lowId = completedProtocols.get(0).getId();

			ProtocolReport report = new ProtocolReport();
			report.setId(template.getId());
			report.setMeanDays(meanDays);
			report.setMedDays(medianDays);
			report.setHigh(high);
			report.setHighId(highId);
			report.setLow(low);
			report.setLowId(lowId);
			report.setName(template.getName());

			reports.add(report);
		}

		// Process userMap
		for (Map.Entry<Integer, ArrayList<Protocol>> entry : userMap.entrySet()) {
			int userId = entry.getKey();
			ArrayList<Protocol> protocols = entry.getValue();

			int size = protocols.size();
			int meanDays = protocols.stream().mapToInt(Protocol::getDaysToComplete).sum() / size;
			int medianDays = protocols.get(size / 2).getDaysToComplete();
			int high = protocols.get(size - 1).getDaysToComplete();
			int low = protocols.get(0).getDaysToComplete();

			ProtocolReport report = new ProtocolReport();
			for (User usr : users) {
				if (usr.getId() == userId) {
					report.setName(usr.getFirstName() + " " + usr.getLastName());
				}
			}
			report.setId(userId);
			report.setMeanDays(meanDays);
			report.setMedDays(medianDays);
			report.setHigh(high);
			report.setLow(low);

			userReports.add(report);
		}

		// Process stepMap
		for (Map.Entry<String, ArrayList<ProtocolStep>> entry : stepMap.entrySet()) {
			String stepName = entry.getKey();
			ArrayList<ProtocolStep> protocols = entry.getValue();

			int size = protocols.size();
			int meanDays = protocols.stream().mapToInt(ProtocolStep::getDaysToComplete).sum() / size;
			int medianDays = protocols.get(size / 2).getDaysToComplete();
			int high = protocols.get(size - 1).getDaysToComplete();
			int low = protocols.get(0).getDaysToComplete();

			ProtocolReport report = new ProtocolReport();
			report.setId(stepName.hashCode()); // Assuming a unique hash code for the step name
			report.setMeanDays(meanDays);
			report.setMedDays(medianDays);
			report.setHigh(high);
			report.setLow(low);
			report.setName("Step: " + stepName);

			stepReports.add(report);
		}

		for (Map.Entry<Integer, ArrayList<ProtocolStep>> entry : userStepMap.entrySet()) {
			int userId = entry.getKey();
			ArrayList<ProtocolStep> protocols = entry.getValue();

			int size = protocols.size();
			int meanDays = protocols.stream().mapToInt(ProtocolStep::getDaysToComplete).sum() / size;
			int medianDays = protocols.get(size / 2).getDaysToComplete();
			int high = protocols.get(size - 1).getDaysToComplete();
			int low = protocols.get(0).getDaysToComplete();

			ProtocolReport report = new ProtocolReport();
			for (User usr : users) {
				if (usr.getId() == userId) {
					report.setName(usr.getFirstName() + " " + usr.getLastName());
				}
			}
			report.setId(userId);
			report.setMeanDays(meanDays);
			report.setMedDays(medianDays);
			report.setHigh(high);
			report.setLow(low);

			userStepReports.add(report);
		}

		if (!reports.isEmpty()) {
			for (ProtocolReport report : reports) {
				logger.info("Report Id: " + report.getId() + " Report Name: " + report.getName() + " Report AVG: "
						+ report.getMeanDays() + " Report MED: " + report.getMedDays() + " Report Low: "
						+ report.getLow() + " Report High: " + report.getHigh());
			}
		} else {
			logger.info("No Protocols in Template Report");
		}

		if (!userReports.isEmpty()) {
			for (ProtocolReport report : userReports) {

				logger.info("Report Id: " + report.getId() + " Report Name: " + report.getName() + " Report AVG: "
						+ report.getMeanDays() + " Report MED: " + report.getMedDays() + " Report Low: "
						+ report.getLow() + " Report High: " + report.getHigh());
			}
		} else {
			logger.info("No Protocols in User Report");
		}

		if (!stepReports.isEmpty()) {
			for (ProtocolReport report : stepReports) {

				logger.info("Report Id: " + report.getId() + " Report Name: " + report.getName() + " Report AVG: "
						+ report.getMeanDays() + " Report MED: " + report.getMedDays() + " Report Low: "
						+ report.getLow() + " Report High: " + report.getHigh());
			}
		} else {
			logger.info("No Protocols in Step Report");
		}
		if (!userStepReports.isEmpty()) {
			for (ProtocolReport report : userStepReports) {

				logger.info("Report not Empty");
				logger.info("Report Id: " + report.getId() + " Report Name: " + report.getName() + " Report AVG: "
						+ report.getMeanDays() + " Report MED: " + report.getMedDays() + " Report Low: "
						+ report.getLow() + " Report High: " + report.getHigh());
			}
		} else {
			logger.info("No Protocols in User Step Report");
		}

		model.addAttribute("userReports", userReports);
		model.addAttribute("stepReports", stepReports);
		model.addAttribute("reports", reports);
		model.addAttribute("userStepReports", userStepReports);
		return "completionReport";
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
		HomeworkHelper helper = new HomeworkHelper();
		try {
			logger.info("Initiating file download...");
			return helper.downloadResponseFile(currentUser, guid);
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
		HomeworkHelper helper = new HomeworkHelper();
		int call = 0;

		switch (type) {
		case "Protocol":
			call = protocolHelper.deleteProtocol(currentUser, id);

			if (call != 1) {
				logger.info("Failed to delete");
				return new ResponseEntity<>("Error deleting Protocol", HttpStatus.BAD_REQUEST);
			}
			break;

		case "ProtocolTemplate":
			call = protocolTemplateHelper.deleteProtocolTemplate(currentUser, id);

			if (call != 1) {
				return new ResponseEntity<>("Error deleting Protocol Template", HttpStatus.BAD_REQUEST);
			}
			break;

		case "StepTemplate":
			call = protocolStepTemplateHelper.deleteStepTemplate(currentUser, id);
			if (call != 1) {
				return new ResponseEntity<>("Error deleting Step Template", HttpStatus.BAD_REQUEST);
			}
			break;

		case "Homework":
			call = helper.deleteHomework(currentUser, id);
			if (call != 1) {
				return new ResponseEntity<>("Error deleting Homework", HttpStatus.BAD_REQUEST);
			}
			break;

		case "HomeworkTemplate":
			call = homeworkTemplateHelper.deleteHomeworkTemplate(currentUser, id);
			if (call != 1) {
				return new ResponseEntity<>("Error deleting Homework Template", HttpStatus.BAD_REQUEST);
			}
			break;

		case "HomeworkQuestion":
			call = helper.deleteHomeworkQuestion(currentUser, id);
			if (call != 1) {
				return new ResponseEntity<>("Error deleting Homework Question", HttpStatus.BAD_REQUEST);
			}
			break;

		default:
			return new ResponseEntity<>("Invalid type", HttpStatus.BAD_REQUEST);
		}
		logger.info("Success "+ call);
		
		return new ResponseEntity<>("Deleted successfully", HttpStatus.OK);
	}
	
	@PatchMapping("/updateHomeworkQuestion/{id}")
	public ResponseEntity<String>updateHomeworkTemplate(@PathVariable int id, @RequestBody HomeworkQuestion question){
		User currentUser = userDAO.getUser();
		HomeworkQuestionHelper helper = new HomeworkQuestionHelper();
		try {
			helper.updateHomeowrkQuestion(currentUser, id, question);
			return ResponseEntity.ok().body("{\"message\": \"File successfully uploaded\"}");
		
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
	
	@GetMapping("/DisplayHomeworkQustion/{id}")
	public String displayHomeworkQuestion(@PathVariable int id, Model model) {
		User currentUser = userDAO.getUser();
		HomeworkQuestionHelper helper = new HomeworkQuestionHelper();
		HomeworkQuestion question = helper.getQuestion(currentUser, id);
		model.addAttribute("question",question);
		return "displayHomeworkQuestion";
	}

	@GetMapping("/homeworkQuestionList/")
	public String homeworkQuestionList(Model model) {
		User currentUser = userDAO.getUser();
		ArrayList<HomeworkQuestion> questionList = homeworkTemplateHelper.getHomeworkQuestions(currentUser);
		model.addAttribute("questionList", questionList);

		return "homeworkQuestionList";
	}

	@PostMapping("/saveQuestion/{question}")
	public ResponseEntity<String> saveQuestion(@RequestBody HomeworkQuestion question, Model model) {
		User currentUser = userDAO.getUser();
		HomeworkQuestionHelper helper = new HomeworkQuestionHelper();
		try {
			helper.newHomeworkQuestion(currentUser, question);
			return ResponseEntity.ok().body("{\"message\": \"File successfully uploaded\"}");
		
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error Updating Question");
		}
	}
}
