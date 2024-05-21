package com.cairn.ui;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.cairn.ui.helper.HomeworkTemplateHelper;
import com.cairn.ui.helper.ProtocolHelper;
import com.cairn.ui.helper.ProtocolStepTemplateHelper;
import com.cairn.ui.helper.ProtocolTemplateHelper;
import com.cairn.ui.helper.ReportHelper;
import com.cairn.ui.helper.UserHelper;
import com.cairn.ui.model.AssignedHomeworkResponse;
import com.cairn.ui.model.Homework;
import com.cairn.ui.model.HomeworkFile;
import com.cairn.ui.model.HomeworkQuestion;
import com.cairn.ui.model.HomeworkQuestionsTemplate;
import com.cairn.ui.model.HomeworkTemplate;
import com.cairn.ui.model.Protocol;
import com.cairn.ui.model.ProtocolComments;
import com.cairn.ui.model.ProtocolStep;
import com.cairn.ui.model.ProtocolStepTemplate;
import com.cairn.ui.model.ProtocolTemplate;
import com.cairn.ui.model.ReportStat;
import com.cairn.ui.model.User;
import com.cairn.ui.model.UserDAO;

import jakarta.servlet.http.HttpSession;

@Controller
public class MainController {
	@Autowired
	UserDAO userDAO;

	@Autowired
	DashboardHelper helper;

	@Autowired
	HomeworkTemplateHelper homeworkTemplateHelper;
	
	//@Autowired
	//HomeworkHelper homeworkHelper;

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
	
    //@Autowired
    //private JavaMailSender mailSender;

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
		session.setAttribute("me", usr);
		model.addAttribute("msg", msg);
		model.addAttribute("user", usr);
		model.addAttribute("stats", helper.getDashboard(usr));
		// User currentUser = userDAO.getUser();
		// UserHelper helper = new UserHelper();
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
		System.out.println("Calling getHomeworkByProtocol");
		ArrayList<Homework> allHomeworks = homeworkHelper.getHomeworkByProtocolId(currentUser, pcolId);
		if (allHomeworks != null && !allHomeworks.isEmpty()) {
		    for (Homework homework : allHomeworks) {
		        System.out.println("Homework Name: " + homework.getName() + " Homework ID: " + homework.getId() + ", Description: " + homework.getDescription());
		    }
		} else {
		    System.out.println("No homeworks found or list is empty");
		}
		ArrayList<ProtocolStep> steps = protocolHelper.getStepList(currentUser,pcolId);
		ProtocolComments mostRecentComment = protocol.getComments().stream()
		        .filter(comment -> "COMMENT".equals(comment.getCommentType()))
		        .max(Comparator.comparing(ProtocolComments::getTakenAt))
		        .orElse(null);
		model.addAttribute("mostRecentComment",mostRecentComment);
		model.addAttribute("protocol", protocol);
		model.addAttribute("steps", steps);
		model.addAttribute("protocolId", pcolId);
		model.addAttribute("userId", userId);
		model.addAttribute("homeworks",allHomeworks);

		return "protocolDetail";
	}
	@GetMapping("/analysis/{id}")
	public String analysis(@PathVariable int id, Model model) {
	    User currentUser = userDAO.getUser();
	    ArrayList<ProtocolStep> stepList = protocolHelper.getStepList(currentUser, id);
	    ArrayList<Integer>stepIds = new ArrayList<Integer>();
	    HomeworkHelper homeworkHelper = new HomeworkHelper();
	    ArrayList<Homework> allHomeworks = homeworkHelper.getHomeworkByProtocolId(currentUser, id);
	    
	    for (ProtocolStep step : stepList) {
	        System.out.println("Step " + step.getName() + " Category: " + step.getCategoryId());
	        if (step.getCategoryId() == 2) {
	            stepIds.add(step.getId());
	        }
	    }
	    // Using removeIf to directly filter elements
	    stepList.removeIf(step -> step.getCategoryId() != 2);
	    
	    
	    for (ProtocolStep step: stepList) {
	    	System.out.println("Step "+ step.getName() + " Catagory: "+ step.getCategoryId());
	    }

	    if (allHomeworks != null && !allHomeworks.isEmpty()) {
	        for (Homework homework : allHomeworks) {
	            System.out.println("Homework Name: " + homework.getName() + " Homework ID: " + homework.getId() + ", Description: " + homework.getDescription() + " Parent Step Id: "+ homework.getParentStepId());
	        }
	    } else {
	        System.out.println("No homeworks found or list is empty");
	    }
	    if (allHomeworks != null && !allHomeworks.isEmpty()) {
	        allHomeworks.removeIf(homework -> !stepIds.contains(homework.getParentStepId())); // Remove homeworks whose ParentStepId is not in stepIds
	    }
	    
	    
		model.addAttribute("homeworks",allHomeworks);
	    model.addAttribute("steps", stepList);
	    
	    return "analysis";
	}
    
    @GetMapping("/recommendations/{id}")
    public String recomendations(@PathVariable int id, Model model) {
    	User currentUser = userDAO.getUser();
	    ArrayList<ProtocolStep> stepList = protocolHelper.getStepList(currentUser, id);
	    ArrayList<Integer>stepIds = new ArrayList<Integer>();
	    HomeworkHelper homeworkHelper = new HomeworkHelper(); 
	    ArrayList<Homework> allHomeworks = homeworkHelper.getHomeworkByProtocolId(currentUser, id);
	    
	    for (ProtocolStep step : stepList) {
	        System.out.println("Step " + step.getName() + " Category: " + step.getCategoryId());
	        if (step.getCategoryId() == 2) {
	            stepIds.add(step.getId());
	        }
	    }
	    // Using removeIf to directly filter elements
	    stepList.removeIf(step -> step.getCategoryId() != 3);
	    for (ProtocolStep step: stepList) {
	    	System.out.println("Step "+ step.getName() + " Catagory: "+ step.getCategoryId());
	    }
	    if (allHomeworks != null && !allHomeworks.isEmpty()) {
	        for (Homework homework : allHomeworks) {
	            System.out.println("Homework Name: " + homework.getName() + " Homework ID: " + homework.getId() + ", Description: " + homework.getDescription() + " Parent Step Id: "+ homework.getParentStepId());
	        }
	    } else {
	        System.out.println("No homeworks found or list is empty");
	    }
	    if (allHomeworks != null && !allHomeworks.isEmpty()) {
	        allHomeworks.removeIf(homework -> !stepIds.contains(homework.getParentStepId())); // Remove homeworks whose ParentStepId is not in stepIds
	    } System.out.println("No homeworks found or list is empty");
	    
		model.addAttribute("homeworks",allHomeworks);
    	model.addAttribute("steps",stepList);
    	return"recommendations";
    }

	@PatchMapping("/updateProtocolComment/{protocolId}/{comment}")
	public ResponseEntity<Object> updateProtocolComment(@PathVariable int protocolId, @PathVariable String comment,
			Model model) {
		User currentUser = userDAO.getUser();

		try {
			protocolHelper.updateProtocolComment(currentUser, protocolId, comment);
		} catch (Exception e) {
			System.out.println("Error in addClientToProtocol:");
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
			System.out.println("Error in addClientToProtocol:");
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error updating Protocol Comment: " + e.getMessage());

		}
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/updateProtocolCommentsGoalsAndProgress/{protocolId}/{comment}/{goal}/{progress}")
	public ResponseEntity<Object> updateProtocolComment(@PathVariable int protocolId, @PathVariable String comment,
			@PathVariable String goal, @PathVariable String progress, Model model) {
		User currentUser = userDAO.getUser();

		try {
			protocolHelper.postProtocolComment(currentUser, protocolId,"COMMENT", comment);
			protocolHelper.updateProtocolGoal(currentUser, protocolId, goal);
			protocolHelper.updateProtocolProgress(currentUser, protocolId, progress);
		} catch (Exception e) {
			System.out.println("Error in addClientToProtocol:");
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

		System.out.println(status);
		try {
			protocolHelper.updateStepStatus(currentUser, protocolId, stepId, status);
		} catch (Exception e) {
			System.out.println("Error in addClientToProtocol:");
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

		System.out.println(note);
		try {
			protocolHelper.updateStepNote(currentUser, protocolId, stepId, note);
		} catch (Exception e) {
			System.out.println("Error in addClientToProtocol:");
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


		ArrayList<ProtocolStepTemplate> allSteps = protocolTemplateHelper.getAllSteps(usr);
		List<ProtocolStepTemplate> listSteps = protocolTemplateHelper.getStepList(usr, id);

		List<ProtocolStepTemplate> fullStepList= new ArrayList<ProtocolStepTemplate>(); //there's a method to this madness. getStepList doesn't get homeworks
		
		for (ProtocolStepTemplate step: listSteps) {
			int stepId = step.getId();
			ProtocolStepTemplate fullStep = protocolTemplateHelper.getStep(usr, stepId);
			fullStepList.add(fullStep);
		}

		for (ProtocolStepTemplate step :fullStepList) {
			
			System.out.println("Step id "+ step.getCategoryId());
		}
		
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
		for (HomeworkTemplate hw: templatelist) {
			System.out.println("Homework ID: " + hw.getId() + " Homework Name: "+ hw.getName());
		}
		
		model.addAttribute("homework", templatelist);
		model.addAttribute("step", step);

		return "newStep";
	}
	
	@PostMapping("/saveStep/")
	public ResponseEntity<?> saveStep(@RequestBody ProtocolStepTemplate newStep) {
		User usr = (User) userDAO.getUser();	  
		
	    try {
			protocolStepTemplateHelper.addStepTemplate(usr, newStep);
	        return ResponseEntity.ok("Template processed successfully");
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("An error occurred while saving the Step Template: " + e.getMessage());
	    }
	}



	@GetMapping("/newProtocol/")
	public String newProtocol( Model model) {
		User usr = (User) userDAO.getUser();
		ArrayList<ProtocolStepTemplate> allSteps = protocolTemplateHelper.getAllSteps(usr);
		model.addAttribute("allSteps", allSteps);
		return "newProtocolTemplate";
	}
	
    @PostMapping("/createNewProtocolTemplate/")
    public ResponseEntity<?> createNewProtocolTemplate(@RequestBody ProtocolTemplate requestBody){
        System.out.println("Calling createNewProtocolTemplate");

        try {
            User usr = (User) userDAO.getUser();
            int call = protocolTemplateHelper.newProtocolTemplate(usr, requestBody);
            if (call == 1) {
                System.out.println("Success!");
                Map<String, String> success = new HashMap<>();
                success.put("message", "Template processed successfully");
                return ResponseEntity.ok(success);
            } else {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "An error occurred while creating the protocol.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An error occurred while creating the protocol: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
	
	
	@PatchMapping("/saveProtocol/{id}")
	public ResponseEntity<?> saveProtocol(@PathVariable int id, @RequestBody String requestBody) {
		System.out.println("Save Protocol called");
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
    public ResponseEntity<?> updateProtocol(@PathVariable int id, @RequestBody ProtocolTemplate updateRequest) {
        User usr = (User) userDAO.getUser();
        if (usr == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        String description = updateRequest.getDescription();

        String name = updateRequest.getName();

        try {
            protocolTemplateHelper.updateProtocolTemplateDescription(usr, id, description);
            protocolTemplateHelper.updateProtocolTemplateName(usr, id, name);
            return ResponseEntity.ok(Collections.singletonMap("message", "Protocol updated successfully"));
        } catch (Exception e) {
            System.out.println("Error in updateProtocol:");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Error updating protocol: " + e.getMessage()));
        }
    }
	
	
	
	@PatchMapping("/saveStep/{stepId}")
	public ResponseEntity<?> saveStep(@PathVariable int stepId, @PathVariable String requestBody ) {
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
		List<ProtocolStepTemplate> steps = new ArrayList<>();
		List<ProtocolStepTemplate> associatedSteps = new ArrayList<>();
		ProtocolTemplate protocol = new ProtocolTemplate();
		if (id > 0) {
			// Fetch the protocol by its ID
			protocol = protocolTemplateHelper.getTemplate(usr, id);
			associatedSteps = protocolTemplateHelper.getStepList(usr, id);
		}
		List<ProtocolStepTemplate> allSteps = protocolTemplateHelper.getAllSteps(usr); // used for the drop down
		for (ProtocolStepTemplate step : associatedSteps ) {
			
		}
		// Add attributes to the model
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

	@PatchMapping("/addStepToProtocol/{protocolId}/{stepId}")
	public ResponseEntity<?> addStepToProtocol(@PathVariable Integer protocolId, @PathVariable Integer stepId) {
		try {
			User usr = (User) userDAO.getUser();
			ProtocolTemplate pcol = protocolTemplateHelper.getTemplate(usr, protocolId);
			ProtocolStepTemplate step = protocolTemplateHelper.getStep(usr, stepId);

			if (pcol == null || step == null) {
				return ResponseEntity.notFound().build();
			}

			int result = protocolTemplateHelper.addTemplateStep(usr, pcol, step);
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
		HomeworkHelper hwHelper = new HomeworkHelper();
		//ArrayList<Homework> assignedHomework = hwHelper.getHomeworkByProtocolId(usr, stepId);
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
		model.addAttribute("id",id);
		return "changeUserInfo";
	}
	
	@GetMapping("/changeClientInfo/{id}") //basically the same from above but for clients that aren't the current user, requires a different page. 
	public String changeClientInfo(@PathVariable int id, Model model) {
		User usr = userDAO.getUser();
		User client = userHelper.getUser(usr, id);
		int clientId = client.getId();
		model.addAttribute("user", client);
		model.addAttribute("id",clientId);
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
		ArrayList<User> userList = userHelper.getUserList(currentUser);
		ArrayList<User> detailedUserList = new ArrayList<User>();
		ArrayList<Integer> dependents = new ArrayList<Integer>();
		for (User user: userList) {
			detailedUserList.add(userHelper.getUser(currentUser, user.getId()));
		}
		for(User user: detailedUserList) {
			for(User dependent: user.getDependents()) {
				dependents.add(dependent.getId());
			}
			
		}
	    List<User> filteredUserList = userList.stream().filter(user -> !dependents.contains(user.getId())).collect(Collectors.toList());

		model.addAttribute("UserList", filteredUserList);

		return "displayClients";
	}
	@GetMapping("createDependant/{clientId}")
	public String createDependant(@PathVariable int clientId, Model model) {
		model.addAttribute("clientId",clientId);
;		return "addDependant";
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
	
    @RequestMapping(value = "addDependant/{clientId}", method = {RequestMethod.POST, RequestMethod.PATCH})
	public ResponseEntity<Object> addDependant(@PathVariable int clientId,@RequestBody User dependantUser ){
    	System.out.println("Calling addDependent from controller");
		User currentUser = userDAO.getUser();
		User client = userHelper.getUser(currentUser, clientId);
		String userDependant = userHelper.addUser(currentUser, dependantUser);
		int dependantId = extractIdFromString(userDependant);
	
		User newDependant = userHelper.getUser(currentUser, dependantId);
		System.out.println("Dependant Created: "+ newDependant+ "Dependant ID "+dependantId);
		ArrayList<User> dependants = client.getDependents();
		dependants.add(newDependant);
		
		try {
			userHelper.addDependant(currentUser, client, dependants);
			
		} catch (Exception e) {
			System.out.println("Error in addClientToProtocol:");
			e.printStackTrace(); // Print the stack trace to the console
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error adding client to protocol: " + e.getMessage());
		}
		return ResponseEntity.ok().build();
	}
    
    @PatchMapping("addCoClient/{clientId}/{coClientId}")
    public ResponseEntity<?>addCoClient(@PathVariable int clientId, @PathVariable int coClientId){
    	System.out.println("Calling Add CoClient for ClientID: " + clientId + " and CoClient ID: "+ coClientId);
    	User currentUser = userDAO.getUser();
    	
    	User client = userHelper.getUser(currentUser, clientId);
		User coClient = userHelper.getUser(currentUser, coClientId);
		
		try {
			userHelper.addCoClient(currentUser, client, coClient);
			return ResponseEntity.ok().body("{\"message\": \"CoClient Successfully Added!\"}");

	           
        }catch (Exception e) {
                System.err.println("Error uploading file: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"Error adding CoClient\"}");
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
		User client = userHelper.getUser(currentUser, clientId);
		ArrayList<ProtocolTemplate> pcolList = protocolTemplateHelper.getList(currentUser);
		ArrayList<Protocol> assignedProtocols = protocolHelper.getAssignedProtocols(currentUser, clientId); // this
		int userId = userHelper.getUserId(currentUser);
		ArrayList<User> coClientList = userHelper.getUserList(currentUser);
		model.addAttribute("coclientList",coClientList);
		model.addAttribute("userId",userId);
		model.addAttribute("client", client);
		model.addAttribute("clientId", clientId);
		model.addAttribute("protocolList", pcolList);
		model.addAttribute("assignedProtocols", assignedProtocols);

		if (client.getCoclient() != null) {
			model.addAttribute("coclient", client.getCoclient());
		} else {
			model.addAttribute("coclient", new User());
		}
		return "clientProfile";
	}

	@PostMapping("/addClientToProtocol/{clientId}/{protocolTemplateId}")
	public ResponseEntity<Object> addClientToProtocol(@PathVariable int clientId, @PathVariable int protocolTemplateId,
			Model model) {
		try {

			User currentUser = userDAO.getUser();

			protocolHelper.addClient(currentUser, clientId, protocolTemplateId); // Perform the operation

		} catch (Exception e) {
			System.out.println("Error in addClientToProtocol:");
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
			System.out.println("Error in addHomeworkTemplateToStep:");
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
	
	@PostMapping("/addClient/")
	public ResponseEntity<Object> addClient(@RequestBody User requestBody) {
	    User currentUser = userDAO.getUser();
	    String call = userHelper.addUser(currentUser, requestBody);

	    if (call.startsWith("Success")) {
	    	System.out.println(call);
	        return ResponseEntity.ok(Collections.singletonMap("message", "Client added successfully"));
	    } else {
	        Map<String, String> errorResponse = new HashMap<>();
	        errorResponse.put("error", "Error processing the request");

	        // Customize this logic to parse the actual error message
	        if (call.contains("error")) {
	            // Extract the "error" field and remove unnecessary details
	            String errorMessage = call.substring(call.indexOf("\"error\":\"") + 8, call.indexOf("\",", call.indexOf("\"error\":\"")));
	            errorResponse.put("error", errorMessage);
	        }
	        System.out.println(errorResponse);
	        return ResponseEntity.badRequest().body(errorResponse);
	    }
	}



	@PostMapping("/updateUserPassword/{id}")
	public ResponseEntity<Object> updateUserPassword(@PathVariable int id, @RequestBody User requestBody) {
		User currentUser = userDAO.getUser();

		System.out.println(userHelper.getUserId(currentUser));
		
		
		String call = userHelper.changeUserPassword(currentUser, id, requestBody.getPassword(), requestBody.getVerifypassword());
		if (call.startsWith("Success")) {
	        return ResponseEntity.ok(Collections.singletonMap("message", "Client added successfully"));
	    } else {
	        Map<String, String> errorResponse = new HashMap<>();
	        errorResponse.put("error", "Error processing the request");


	        if (call.contains("error")) {

	            String errorMessage = call.substring(call.indexOf("\"error\":\"") + 8, call.indexOf("\",", call.indexOf("\"error\":\"")));
	            errorResponse.put("error", errorMessage);
	        }
	        System.out.println(errorResponse);
	        return ResponseEntity.badRequest().body(errorResponse);
	    }
	}

	@PatchMapping("/updateUserDetails/{id}/{firstName}/{lastName}/{email}/")
	public ResponseEntity<Object> updateUserDetails(@PathVariable int id, @PathVariable String firstName, @PathVariable String lastName,
			@PathVariable String email, Model model) {
		User currentUser = userDAO.getUser();

		System.out.println("Calling updateUserDetails");

		try {
			userHelper.updateUserDetails(currentUser, id, firstName, lastName, email);

		} catch (Exception e) {
			System.out.println("Error in updateUserDetails:");
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
			System.out.println(result);
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
	public String editHomeworkTemplate( Model model) {
		User currentUser = userDAO.getUser();
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
    public String saveHomeworkTemplate(@RequestBody String templateBody,RedirectAttributes redirectAttributes) {
        try {
            // Decode URL-encoded string
            String decodedBody = URLDecoder.decode(templateBody, StandardCharsets.UTF_8.toString());

            // Log the raw decoded body for debugging

            // Clean up to ensure the JSON starts correctly
            int jsonStartIndex = decodedBody.indexOf("\"name\"");
            //if (jsonStartIndex == -1) {
           //     return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request body: JSON starting with '{\"name\"' not found.");
           // }
            
            // Extract the JSON correctly including the first curly brace and trim any leading/trailing whitespace
            String cleanJson = "{\n" + decodedBody.substring(jsonStartIndex).trim();

            // Optionally, log the cleaned JSON for debugging
            System.out.println("Cleaned JSON Data: " + cleanJson);

            // Further JSON processing here...
            User currentUser = userDAO.getUser();

            String call = homeworkTemplateHelper.newTemplate(currentUser, cleanJson);
            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("error", "User not found.");
                return "redirect:/login";
            }
            if (call == "Success") {
            redirectAttributes.addFlashAttribute("success", "Homework template saved successfully.");
            return "redirect:/homeworkTemplates/";
            }
            else {
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
    public ResponseEntity<?>removeHomeworkQuestionFromTemplate(@PathVariable int tempId, @PathVariable int questionID){
    	User currentUser = userDAO.getUser();
    	HomeworkTemplate template = homeworkTemplateHelper.getTemplate(currentUser,tempId);
    	List<HomeworkQuestionsTemplate> questions = template.getQuestions();
    	for(HomeworkQuestionsTemplate question : questions ) {
    		if(question.getQuestionId()==questionID) {
    			questions.remove(question);
    		}
    	}
    	
    	try{
    		int apiCall = homeworkTemplateHelper.removeQuestionFromTemplate(currentUser, tempId, questions);
    		if (apiCall == 1) {
    			return ResponseEntity.ok("Template processed successfully");
    		}else {
    			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing template");
    		}
    	}catch(Exception e) {
    		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing template");
    	}
    }
    
    @DeleteMapping("/removeStepFromTemplate/{tempId}/{stepId}")
    public ResponseEntity<?>removeStepFromTemplate(@PathVariable int tempId, @PathVariable int stepId){
    	User currentUser = userDAO.getUser();

    	try{
    		int apiCall = protocolTemplateHelper.deleteProtocolStepTemplate(currentUser, tempId, stepId);
    		if (apiCall == 1) {
    			return ResponseEntity.ok("Template processed successfully");
    		}else {
    			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing template");
    		}
    	}catch(Exception e) {
    		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing template");
    	}
    }
    
    @DeleteMapping("/removeHomeworkFromStepTemplate/{stepId}/{homeworkId}")
    public ResponseEntity<?>removeHomeworkFromStepTemplate(@PathVariable int stepId, @PathVariable int homeworkId){
    	
    	User currentUser = userDAO.getUser();

    	try{
    		int apiCall = protocolStepTemplateHelper.deleteHomeworkTemplate(currentUser, stepId, homeworkId);
    		if (apiCall == 1) {
    			return ResponseEntity.ok("Template processed successfully");
    		}else {
    			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing template");
    		}
    	}catch(Exception e) {
    		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing template");
    	}
    	
    }
    
    @GetMapping("/homeworkList/{id}")
    public String homeworkList(@PathVariable int id, Model model) {
    	User currentUser = userDAO.getUser();
    	HomeworkHelper helper = new HomeworkHelper();  // this will need to be autowired at some point 
    	ArrayList<Homework> homeworks = helper.getHomeworkByProtocolId(currentUser, id);
    	
    	
    	model.addAttribute("homeworks",homeworks);
    	
    	return "homeworkList";
    }
    
    @GetMapping("homeworkDisplay/{homeworkId}")
    public String homeworkDisplay(@PathVariable int homeworkId,Model model) {
    	User currentUser = userDAO.getUser();
    	HomeworkHelper hwHelper = new HomeworkHelper();
    	Homework homework = hwHelper.getHomeworkByHomeworkId(currentUser, homeworkId);
    	for (HomeworkQuestion question : homework.getQuestions()) {
    	}
    	model.addAttribute("homework",homework);
    	return "homeworkDisplay";
    }
    

    @PatchMapping("/assignUserResponseToHomework/{homeworkId}")
    public ResponseEntity<?> assignUserResponseToHomework(@PathVariable int homeworkId, @RequestBody List<AssignedHomeworkResponse> responses) {
        User currentUser = userDAO.getUser();
        HomeworkHelper helper= new HomeworkHelper();
        System.out.println("Responses: " + responses);
        try {
            for (AssignedHomeworkResponse response : responses) {
                int questionId = response.getId();
                String userResponse = response.getResponse();
                String path = response.getFilePath();  // Assuming you have a getter for filePath
                System.out.println("Question ID: " + questionId + " Response: " + userResponse + " FilePath: " + path);
                helper.assignAnswerToHomework(currentUser, homeworkId, questionId, userResponse, path);
            }
            return ResponseEntity.ok().body("{\"message\": \"Success!\"}");
        } catch (NumberFormatException e) {
            System.err.println("Invalid question ID: " + e.getMessage());
            return ResponseEntity.badRequest().body("{\"error\": \""+e.getMessage()+"\"}");
        } catch (Exception e) {
            e.printStackTrace(); // This will print stack trace to the console
            System.err.println("Error in processing responses: " + (e.getMessage() != null ? e.getMessage() : "Unknown Error"));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \""+e.getMessage()+"\"}");
        }

    }
    @GetMapping("/uploadFile/{homeworkId}/{questionId}")
    public String uploadFil(@PathVariable int homeworkId, @PathVariable int questionId, Model model){
    	model.addAttribute("questionId",questionId);
    	return"uploadFile";
    	
    }
    @PatchMapping("/uploadFileToHomework/")
    public ResponseEntity<?> uploadFileToHomework(@RequestParam("file") MultipartFile file, String userResponse, @RequestParam("homeworkId") int homeworkId, @RequestParam("questionId") int questionId) {
    	 User currentUser = userDAO.getUser();
         HomeworkHelper helper= new HomeworkHelper();
         System.out.println("Calling upoloadFileToHomework");
        try {
            
            helper.assigneFileUpload(currentUser, homeworkId, questionId, "File Upload", file);
            return ResponseEntity.ok().body("{\"message\": \"File successfully uploaded\"}");

           
        }catch (Exception e) {
                System.err.println("Error uploading file: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"Error uploading file\"}");
            }
    }
    
    @GetMapping("/allClientPrtotocols/{clientId}")
    public String allClientProtocols(@PathVariable int clientId, Model model) {
    	User currentUser = userDAO.getUser();
    	ArrayList<Protocol>listProtocols = protocolHelper.getAssignedProtocols(currentUser, clientId);
    	model.addAttribute("listProtocols", listProtocols);
    	return"allClientProtocols";
    }
    
    @GetMapping("/protocolHistory/{clientId}/{name}")
    public String protocolHistory(@PathVariable int clientId, @PathVariable String name, Model model) {
        User currentUser = userDAO.getUser();
        List<Protocol> allProtocols = protocolHelper.getAssignedProtocols(currentUser, clientId);
        List<Protocol> filteredProtocols = allProtocols.stream().filter(p -> p.getName().equals(name)).collect(Collectors.toList());
        model.addAttribute("listProtocols", filteredProtocols);
        return "allClientProtocols";
    }

    @GetMapping("/homeworkReport/{clientId}")
    public String homeworkReport(@PathVariable int clientId, Model model) {
    	User currentUser = userDAO.getUser();
    	HomeworkHelper helper= new HomeworkHelper();
    	ArrayList<Homework> homeworks = helper.getHomeworkByUser(currentUser, clientId);
    	ArrayList<Homework> homeworkDetails = new ArrayList<Homework>();
    	for(Homework homework: homeworks) {
    		Homework detailedHomework = helper.getHomeworkByHomeworkId(currentUser, homework.getId());

    		homeworkDetails.add(detailedHomework);
    	}
    	model.addAttribute("homeworks", homeworkDetails);
    	
    	return "homeworkReport";
    }
    
    @PostMapping("/sendEmail")
    @ResponseBody
    public String sendEmail(@RequestParam int userId, @RequestParam String message) {
        User user = userHelper.getUser(null, userId);

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(user.getEmail());
        email.setSubject("Attention Required");
        email.setText("Our Records indicate that we are missing some information\n Please complete the following questiosn\n "+message);

        mailSender.send(email);
        return "Email sent successfully";
    }
    
    @GetMapping("/protocolRecommendations/{id}")
    public String protocolRecomendations(@PathVariable int id, Model model) {
    	User currentUser = userDAO.getUser();
    	Protocol protocol = protocolHelper.getProtocol(currentUser, id);
    	model.addAttribute("protocol",protocol);
    	model.addAttribute("protocolId", protocol.getId());
    	return "protocolRecommendations";
    }
    
    @PostMapping("/postRecommendations/{id}")
    public ResponseEntity<?> postRecommendation(@PathVariable int id,@RequestBody String recomendation){
    	User currentUser = userDAO.getUser();
    	try {
    		protocolHelper.postProtocolComment(currentUser, id,"RECOMMENDATION" ,recomendation);
            return ResponseEntity.ok().body("{\"message\": \"Success: Recomendation Posted!\"}");

        }catch (Exception e) {
                System.err.println("Error uploading file: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"Error Posting Recomendation!\"}");
            }
    }
    
    
    

}