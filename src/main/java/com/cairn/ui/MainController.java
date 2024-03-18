package com.cairn.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.cairn.ui.helper.ProtocolHelper;
import com.cairn.ui.helper.ProtocolTemplateHelper;
import com.cairn.ui.model.Protocol;
import com.cairn.ui.model.ProtocolStepTemplate;
import com.cairn.ui.model.ProtocolTemplate;
import com.cairn.ui.model.Protocol_admin;
import com.cairn.ui.model.Protocol_step_admin;
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

		model.addAttribute("msg", msg);
		model.addAttribute("user", usr);
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
	@RequestMapping(value = "/viewProtocol/{id}", method = RequestMethod.GET)
	public ModelAndView viewProtocol(HttpServletRequest request, @PathVariable int id) {
		ModelAndView model = new ModelAndView("protocolDetail");
		return model;
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
	
	@GetMapping("/editProtocol/{id}") // sorta works but don't have a all steps for the drop down need to figure that out
	public String editProtocolTemplate(@PathVariable int id, Model model) {
	    User usr = (User) userDAO.getUser();
	    ProtocolTemplateHelper helper = new ProtocolTemplateHelper();
	    ProtocolTemplate pcol = helper.getTemplate(usr, id);
	    ArrayList<ProtocolStepTemplate> allSteps = helper.availableSteps(usr, pcol, 0); //need an option for all step types 
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
	
	@PatchMapping("/saveStep/{id}")
	public ResponseEntity<?> saveStep(@PathVariable int id) {
	    try {
	        User usr = (User) userDAO.getUser();
	        ProtocolTemplateHelper helper = new ProtocolTemplateHelper();
	        ProtocolStepTemplate pstep = helper.getStep(usr, id);

	        if (pstep != null) {
	            helper.saveTemplateStep(usr, pstep);
	            return ResponseEntity.ok().body("Step with ID " + id + " updated successfully.");
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
	    if (id == 0) {
	        // Create a new, empty Protocol_admin object and an empty list of steps
	        Protocol_admin protocol = new Protocol_admin(); 
	        List<Protocol_step_admin> steps = new ArrayList<>();
	        // Add attributes to the model
	        model.addAttribute("protocol", protocol);
	        model.addAttribute("steps", steps);
	    } else {
	        // Fetch the protocol by its ID
	        Protocol_admin protocol = Protocol_admin.findById(id);
	        List<Protocol_step_admin> allSteps = Protocol_step_admin.loadStepsFromJson(); //used for the drop down 
	        List<Protocol_step_admin> associatedSteps = protocol.getSteps().stream()// actual display data 
	        	    .map(stepId -> Protocol_step_admin.findById(stepId, allSteps))
	        	    .collect(Collectors.toList());

	        // Add attributes to the model
	        model.addAttribute("protocolId", id);
	        model.addAttribute("protocol", protocol);
	        model.addAttribute("steps", associatedSteps);
	        model.addAttribute("allSteps", allSteps);
	    }

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
	
	@GetMapping("/protocols_admin")
	public String protocolListPage(Model model) {
		List<Protocol_admin> listProtocols = Protocol_admin.getList_admin();
		model.addAttribute("listProtocols", listProtocols );
		return "protocolList_admin";
	}
	
	public ModelAndView viewProtocol_admin(@PathVariable int id) {
	    ModelAndView model = new ModelAndView("protocolDetail_admin");
	    Protocol_admin protocol = Protocol_admin.findById(id);
	    model.addObject("protocol_admin", protocol); // Adding the protocol object to the model
	    return model;
	}
	

	@PostMapping("/addStepToProtocol/{protocolId}")
	public ResponseEntity<?> addStepToProtocol(@PathVariable Integer protocolId, @RequestBody Integer stepId) {
	    Protocol_admin protocol = Protocol_admin.findById(protocolId);
	    if (protocol != null) {
	        protocol.addStep(stepId);
	        return ResponseEntity.ok().build();
	    }
	    return ResponseEntity.notFound().build();
	}


	
	
	@GetMapping("/editStep_admin/{stepId}")
	public ModelAndView editStep_admin(@PathVariable int stepId) {
	    ModelAndView model = new ModelAndView("edit_Step");

	    if (stepId == 0) {
	        // Create a new step with default values
	        Protocol_step_admin step = new Protocol_step_admin();
	        model.addObject("step", step);
	    } else {
	        List<Protocol_step_admin> steps = Protocol_step_admin.loadStepsFromJson();
	        Protocol_step_admin step = Protocol_step_admin.findById(stepId, steps);
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

}