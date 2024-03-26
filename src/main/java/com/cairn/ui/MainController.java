package com.cairn.ui;

import java.util.ArrayList;
import java.util.List;

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
import org.springframework.web.servlet.ModelAndView;

import com.cairn.ui.helper.DashboardHelper;
import com.cairn.ui.helper.ProtocolHelper;
import com.cairn.ui.helper.ProtocolTemplateHelper;
import com.cairn.ui.helper.UserHelper;
import com.cairn.ui.model.Dashboard;
import com.cairn.ui.model.Protocol;
import com.cairn.ui.model.ProtocolStep;
import com.cairn.ui.model.ProtocolStepTemplate;
import com.cairn.ui.model.ProtocolTemplate;
import com.cairn.ui.model.User;
import com.cairn.ui.model.UserDAO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class MainController {
	@Autowired
	UserDAO userDAO;

	@GetMapping("/")
	public String startPage() {
		return "home";
	}

	@GetMapping("/dashboard")
	public String homePage(@RequestParam(name = "msg", required = false) String msg, Model model) {
		User usr = userDAO.getUser();
		if (usr == null) {
			return "home";
		}
		int id = usr.getId();
		DashboardHelper helper = new DashboardHelper();
		model.addAttribute("msg", msg);
		model.addAttribute("user", usr);
		model.addAttribute("stats", helper.getDashboard(usr));
		return "UserDashboard";
	}

	@GetMapping("/login")
	public String loginPage() {
		return "home";
	}

	/**
	 * Handle a request to view the Protocol details. 
	 * @return
	 */
	@GetMapping("/viewProtocol/{pcolId}")
	public String viewProtocol(Model model, @PathVariable int pcolId) {
    	User currentUser = userDAO.getUser(); 
    	ProtocolHelper helper = new ProtocolHelper();
    	Protocol protocol = helper.getProtocol(currentUser, pcolId);
    	ArrayList<ProtocolStep> steps = helper.getStepList(currentUser, pcolId);
    	for (ProtocolStep step : steps) {
    	    System.out.println("Step Name: " + step.getName() + ", Category: " + step.getCategory());
    	}

    	model.addAttribute("protocol",protocol);
    	model.addAttribute("steps",steps);

	    return "protocolDetail";
	}



	@GetMapping("/protocols")
	public String protocolListPage(HttpSession session, Model model) {
		User usr = (User) userDAO.getUser();
		ProtocolHelper helper = new ProtocolHelper();
		List<Protocol> listProtocols = helper.getList(usr);
		model.addAttribute("listProtocols", listProtocols );
		return "protocolList";
	}
	

	/**
	 * Handle a request to view the Protocol details. 
	 * @return
	 */
	//@RequestMapping(value = "/editProtocol/{id}", method = RequestMethod.GET)
	public ModelAndView editProtocol(HttpServletRequest request, @PathVariable int id) {
		
		ModelAndView model = new ModelAndView("protocolEdit");
		User usr = (User) userDAO.getUser();
		ProtocolTemplateHelper helper = new ProtocolTemplateHelper();
		ProtocolTemplate pcol = helper.getTemplate(usr,id);
		List<ProtocolStepTemplate> listSteps = helper.getStepList(usr,id);
		model.addObject("protocol", pcol );
		model.addObject("steps", listSteps );
		
		return model;
	}
	
	@GetMapping("/editProtocol/{id}") 
	public String editProtocolTemplate(@PathVariable int id, Model model) {
	    User usr = (User) userDAO.getUser();
	    ProtocolTemplateHelper helper = new ProtocolTemplateHelper();
	    ProtocolTemplate pcol = helper.getTemplate(usr, id);
	    ArrayList<ProtocolStepTemplate> allSteps = helper.getAllSteps(usr);
	    List<ProtocolStepTemplate> listSteps = helper.getStepList(usr, id);
	    
	    model.addAttribute("protocolId", id);
	    model.addAttribute("protocol", pcol);
	    model.addAttribute("steps", listSteps);
	    model.addAttribute("allSteps", allSteps);

	    return "displayProtocol";
	}
	//Place holders for creating new and saving changes to steps and protocols 
	@PostMapping("/newStep/")
	public String newStep(@PathVariable int id, Model model) {
		return "edit_step";
	}
	
	@PostMapping("/newProtocol/")
	public String newProtocol(@PathVariable int id, Model model) {
		return "displayProtocol";
	} 
	
	@PatchMapping("saveProtocol/{id}")
	public ResponseEntity<?> saveProtocol(@PathVariable int id){
	    try {
	        User usr = (User) userDAO.getUser();
	        ProtocolTemplateHelper helper = new ProtocolTemplateHelper();
	        ProtocolTemplate pcol = helper.getTemplate(usr, id);

	        if (pcol != null) {
	            helper.saveProtocolTemplate(usr, pcol);
	            return ResponseEntity.ok().body("Protocol with ID " + id + " updated successfully.");
	        }
	        else {
	            return ResponseEntity.notFound().build();
	        }
	    	} 
	    catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while saving the protocol.");
	    }
	}
	
	@PatchMapping("/saveStep/{stepId}")
	public ResponseEntity<?> saveStep(@PathVariable int stepId) {
	    try {
	        User usr = (User) userDAO.getUser();
	        ProtocolTemplateHelper helper = new ProtocolTemplateHelper();
	    
	        ProtocolStepTemplate pstep = helper.getStep(usr, stepId);
	     

	        if (pstep != null) {
	            helper.saveTemplateStep(usr,pstep);
	            return ResponseEntity.ok().body("Step with ID " + stepId + " updated successfully.");
	        } else {
	            return ResponseEntity.notFound().build();
	        }
	    } 
	    catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while saving the step.");
	    }
	}

	
	@GetMapping("/displayProtocol/{id}")
	public String displayProtocolPage(@PathVariable int id, Model model) {
		User usr = (User) userDAO.getUser();
        List<ProtocolStepTemplate> steps = new ArrayList<>();
        List<ProtocolStepTemplate> associatedSteps = new ArrayList<>();
        ProtocolTemplate protocol = new ProtocolTemplate(); 
		ProtocolTemplateHelper helper = new ProtocolTemplateHelper();
	    if (id > 0) {
	        // Fetch the protocol by its ID
	        protocol = helper.getTemplate(usr,id);
	        associatedSteps = helper.getStepList(usr,id);  
	    }
        List<ProtocolStepTemplate> allSteps = helper.getAllSteps(usr); //used for the drop down 

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
		ProtocolTemplateHelper helper = new ProtocolTemplateHelper();
		List<ProtocolTemplate> listProtocols = helper.getList(usr);
		model.addAttribute("listProtocols", listProtocols );
		return "protocolTemplateList";
	}

	

	@PatchMapping("/addStepToProtocol/{protocolId}/{stepId}")
	public ResponseEntity<?> addStepToProtocol(@PathVariable Integer protocolId, @PathVariable Integer stepId) {
	    try {
	        ProtocolTemplateHelper helper = new ProtocolTemplateHelper();
	        User usr = (User) userDAO.getUser();
	        ProtocolTemplate pcol = helper.getTemplate(usr, protocolId);
	        ProtocolStepTemplate step = helper.getStep(usr, stepId);

	        if (pcol == null || step == null) {
	            return ResponseEntity.notFound().build();
	        }

	        int result = helper.addTemplateStep(usr, pcol, step);
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
	public ModelAndView editStep(@PathVariable int stepId) {
	    ModelAndView model = new ModelAndView("edit_Step");
		User usr = (User) userDAO.getUser();
		ProtocolTemplateHelper helper = new ProtocolTemplateHelper();

	    if (stepId == 0) {
	        // Create a new step with default values
	        ProtocolStepTemplate step = new ProtocolStepTemplate();
	        model.addObject("step", step);
	    } else {
	        ProtocolStepTemplate step = helper.getStep(usr, stepId);
	        model.addObject("step", step);
	    }

	    return model;
	}

    @GetMapping("/profile")
    public String userProfile(Model model) {
        User currentUser = userDAO.getUser(); // Fetch the currently logged-in user using UserDAO
        if (currentUser != null) {
            model.addAttribute("user", currentUser);
        } else {
            model.addAttribute("error", "Something's gone wrong please");
        }
        return "userProfile"; 
    }
    
    @GetMapping("/changeUserInfo")
    public String showChangeUserInfoForm(Model model) {
        User userDetails = new User(); // Creates a new UserDetails object to hold form data
        model.addAttribute("userDetails", userDetails); // Adds the object to the model to be accessed by the form
        return "changeUserInfo"; 
    }

    @GetMapping("reports")
    public String reports(Model model) {
    	return "reports";
    }
    
    @GetMapping("clients")
    public String displayClients(Model model) {
    	User currentUser = userDAO.getUser(); 
    	UserHelper usr = new UserHelper();
    	ArrayList<User> userList = usr.getUserList(currentUser);
    	model.addAttribute("UserList",userList);
    	
    	
    	return "displayClients";
    }
    
    @GetMapping("clientProfile/{clientId}")
    public String clientProfile(@PathVariable int clientId , Model model) {
    	User currentUser = userDAO.getUser(); 
    	UserHelper helper = new UserHelper();
    	User client = helper.getUser(currentUser, clientId);
    	model.addAttribute("client",client);
    	return "clientProfile";
    }
    
    
    @GetMapping("clientProtocol/{pcolId}")
    public String clientProtocol(@PathVariable int pcolId, Model model) {
    	User currentUser = userDAO.getUser(); 
    	ProtocolHelper helper = new ProtocolHelper();
    	Protocol protocol = helper.getProtocol(currentUser, pcolId);
    	model.addAttribute("protocol",protocol);
    	return "clientProtocol";
    }
}