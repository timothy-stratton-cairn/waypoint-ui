package com.cairn.ui;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.cairn.ui.model.Protocol;
import com.cairn.ui.model.User;
import com.cairn.ui.model.UserDAO;

import jakarta.servlet.http.HttpServletRequest;

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
	public String protocolListPage(Model model) {
		List<Protocol> listProtocols = Protocol.getList();
		model.addAttribute("listProtocols", listProtocols );
		return "protocolList";
	}

}