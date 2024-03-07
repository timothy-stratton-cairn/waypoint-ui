package com.cairn.ui.model;

import java.util.Map;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;

@Repository
public class UserDAOImpl implements UserDAO {
	@Override
	public User getUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		Map<String, Object> info = (Map<String, Object>)auth.getDetails();  
		User usr = (User) info.get("currentUser");  
		if (!(auth instanceof AnonymousAuthenticationToken)) {
			String login = auth.getPrincipal().toString();
			if (usr == null) {
				usr = new User();
				usr.setUsername(login);
				usr.setEmail("testuser@gmail.com");
			}
			return usr;
		}
		return null;
	}	
}
