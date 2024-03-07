package com.cairn.ui.model;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;

@Repository
public class UserDAOImpl implements UserDAO {
	@Override
	public User getUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (!(auth instanceof AnonymousAuthenticationToken)) {
			String login = auth.getPrincipal().toString();
			User usr = new User();
			usr.setUsername(login);
			usr.setEmail("testuser@gmail.com");
			return usr;
		}
		return null;
	}	
}
