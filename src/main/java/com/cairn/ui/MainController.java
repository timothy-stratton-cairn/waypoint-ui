package com.cairn.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.cairn.ui.helper.DashboardHelper;
import com.cairn.ui.helper.HomeworkTemplateHelper;
import com.cairn.ui.helper.ProtocolHelper;
import com.cairn.ui.helper.ProtocolStepTemplateHelper;
import com.cairn.ui.helper.ProtocolTemplateHelper;
import com.cairn.ui.helper.UserHelper;
import com.cairn.ui.model.Dashboard;
import com.cairn.ui.model.HomeworkTemplate;
import com.cairn.ui.model.Protocol;
import com.cairn.ui.model.ProtocolStep;
import com.cairn.ui.model.ProtocolStepTemplate;
import com.cairn.ui.model.ProtocolTemplate;
import com.cairn.ui.model.User;
import com.cairn.ui.model.UserDAO;
import com.cairn.ui.model.UserDAOImpl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class MainController {
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

		int sessionUserId = userHelper.getUserId(usr);
		User sessionUser = userHelper.getUser(usr, sessionUserId);
		if (sessionUser.getRoles() != null) {
			String userRoles = String.join(", ", sessionUser.getRoles());
			session.setAttribute("userRoles", userRoles);
		}

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

		model.addAttribute("protocol", protocol);
		model.addAttribute("steps", protocol.getSteps());
		model.addAttribute("protocolId", pcolId);
		model.addAttribute("userId", userId);
		List<ProtocolStep> steps = protocol.getSteps();
		if (steps != null) {
			for (ProtocolStep step : steps) {
				String statusWithUnderscores = step.getStatus().replace(' ', '_');
				System.out.println(statusWithUnderscores);
			}
		}
		return "protocolDetail";
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
			protocolHelper.updateProtocolComment(currentUser, protocolId, comment);
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

		model.addAttribute("protocolId", id);
		model.addAttribute("protocol", pcol);
		model.addAttribute("steps", listSteps);
		model.addAttribute("allSteps", allSteps);

		return "displayProtocol";
	}

	// Place holders for creating new and saving changes to steps and protocols
	@PostMapping("/newStep/")
	public String newStep(@PathVariable int id, Model model) {
		return "edit_step";
	}

	@PostMapping("/newProtocol/")
	public String newProtocol(@PathVariable int id, Model model) {
		return "displayProtocol";
	}

	@PatchMapping("saveProtocol/{id}")
	public ResponseEntity<?> saveProtocol(@PathVariable int id) {
		try {
			User usr = (User) userDAO.getUser();
			ProtocolTemplate pcol = protocolTemplateHelper.getTemplate(usr, id);

			if (pcol != null) {
				protocolTemplateHelper.saveProtocolTemplate(usr, pcol);
				return ResponseEntity.ok().body("Protocol with ID " + id + " updated successfully.");
			} else {
				return ResponseEntity.notFound().build();
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("An error occurred while saving the protocol.");
		}
	}

	@PatchMapping("/saveStep/{stepId}")
	public ResponseEntity<?> saveStep(@PathVariable int stepId) {
		try {
			User usr = (User) userDAO.getUser();

			ProtocolStepTemplate pstep = protocolTemplateHelper.getStep(usr, stepId);

			if (pstep != null) {
				protocolTemplateHelper.saveTemplateStep(usr, pstep);
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

	@GetMapping("/homeworkTemplates/")
	public String homeworkTemplates(Model model) {
		User currentUser = userDAO.getUser();
		ArrayList<HomeworkTemplate> templateList = homeworkTemplateHelper.getList(currentUser);
		model.addAttribute("templates", templateList);
		return "homeworkTemplates";
	}

	@GetMapping("viewHomeworkTemplate/{tempId}/")
	public String editHomeworkTemplate(@PathVariable int tempId, Model model) {
		User currentUser = userDAO.getUser();
		HomeworkTemplateHelper helper = new HomeworkTemplateHelper();
		HomeworkTemplate template = helper.getTemplate(currentUser, tempId);
		model.addAttribute("template", template);
		return "editHomeworkTemplate";
	}

	@GetMapping("/profile")
	public String userProfile(Model model) {
		User usr = userDAO.getUser();
		UserHelper helper = new UserHelper();// Fetch the currently logged-in user using UserDAO
		int id = helper.getUserId(usr);
		User currentUser = helper.getUser(usr, id);
		model.addAttribute("user", currentUser);
		return "userProfile";
	}

	@GetMapping("/changeUserInfo")
	public String showChangeUserInfoForm(Model model) {
		User usr = userDAO.getUser();
		UserHelper helper = new UserHelper();// Fetch the currently logged-in user using UserDAO
		int id = helper.getUserId(usr);
		User currentUser = helper.getUser(usr, id);
		model.addAttribute("user", currentUser);// Adds the object to the model to be accessed by the form
		return "changeUserInfo";
	}

	@GetMapping("reports")
	public String reports(Model model) {
		return "reports";
	}

	@GetMapping("clients")
	public String displayClients(Model model) {
		User currentUser = userDAO.getUser();
		ArrayList<User> userList = userHelper.getUserList(currentUser);
		model.addAttribute("UserList", userList);

		return "displayClients";
	}

	@GetMapping("protocolClients/{protocolId}")
	public String ProtocolClients(@PathVariable int ProtocolId, Model model) {
		User currentUser = userDAO.getUser();
		ProtocolHelper phelper = new ProtocolHelper();
		Protocol protocol = phelper.getProtocol(currentUser, ProtocolId);
		UserHelper uhelper = new UserHelper();
		ArrayList<Integer> userIds = protocol.getUsers();
		List<User> userList = new ArrayList<>();
		for (Integer id : userIds) {
			User user = uhelper.getUser(currentUser, id);
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
																											// needs to
																											// be
																											// written
																											// up on the
																											// helper
																											// side

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

	@PostMapping("addClient/{username}/{firstName}/{lastName}/{role}/{email}/{password}")
	public ResponseEntity<Object> addClient(@PathVariable String username, @PathVariable String firstName,
			@PathVariable String lastName, @PathVariable int role, @PathVariable String email,
			@PathVariable String password, Model model) {
		User currentUser = userDAO.getUser();
		try {
			userHelper.addUser(currentUser, username, firstName, lastName, role, email, password);

		} catch (Exception e) {
			System.out.println("Error in addClient:");
			e.printStackTrace(); // Print the stack trace to the console
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error adding client: " + e.getMessage());
		}
		return ResponseEntity.ok().build();
	}

	@PostMapping("/updateUserPassword/{oldpassword}/{newpassword}/")
	public ResponseEntity<Object> updateUserPassword(@PathVariable String opassword, @PathVariable String npassword,
			Model model) {
		User currentUser = userDAO.getUser();
		UserHelper helper = new UserHelper();
		System.out.println(helper.getUserId(currentUser));
		int id = helper.getUserId(currentUser);
		try {
			helper.changeUserPassword(currentUser, id, opassword, npassword);
		} catch (Exception e) {
			System.out.println("Error in addClient:");
			e.printStackTrace(); // Print the stack trace to the console
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error Changing Password: " + e.getMessage());
		}
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/updateUserDetails/{firstName}/{lastName}/{email}/")
	public ResponseEntity<Object> updateUserDetails(@PathVariable String firstName, @PathVariable String lastName,
			@PathVariable String email, Model model) {
		User currentUser = userDAO.getUser();
		UserHelper helper = new UserHelper();
		System.out.print(helper.getUserId(currentUser));
		int id = helper.getUserId(currentUser);
		try {
			helper.updateUserDetails(currentUser, id, firstName, lastName, email);

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

			UserHelper helper = new UserHelper();
			User assignedUser = helper.getUser(currentUser, id);

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

}