package com.cairn.ui;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
	@RequestMapping(value = "/editProtocol/{id}", method = RequestMethod.GET)
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
	
	@GetMapping("/displayProtocol/{id}")
	public String displayProtocolPage(@PathVariable int id, Model model) {
	    // Fetch the protocol by its ID
	    Protocol_admin protocol = Protocol_admin.findById(id);

	    // Load all steps
	    List<Protocol_step_admin> allSteps = Protocol_step_admin.loadStepsFromJson();

	    // Filter steps to only include those associated with the current protocol
	    List<Protocol_step_admin> associatedSteps = protocol.getSteps().stream()
	        .map(stepId -> Protocol_step_admin.findById(stepId, allSteps))
	        .collect(Collectors.toList());

	    // Add attributes to the model
	    model.addAttribute("protocol", protocol);
	    model.addAttribute("steps", associatedSteps); 

	    return "displayProtocol";
	}


	
	
	@GetMapping("/editStep_admin/{stepId}")
	public ModelAndView editStep_admin(@PathVariable int stepId) {
	    ModelAndView model = new ModelAndView("edit_Step");

	    List<Protocol_step_admin> steps = Protocol_step_admin.loadStepsFromJson();
	    Protocol_step_admin step = Protocol_step_admin.findById(stepId, steps);

	    model.addObject("step", step);
	    return model;
	}

}