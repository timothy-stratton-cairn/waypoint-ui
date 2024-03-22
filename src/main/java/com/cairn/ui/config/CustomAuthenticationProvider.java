package com.cairn.ui.config;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.cairn.ui.Constants;
import com.cairn.ui.model.User;
import com.cairn.ui.model.UserDAO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {
	private final RestTemplate restTemplate;

	public CustomAuthenticationProvider(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@Autowired
	UserDAO userDAO;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String username = authentication.getName();
		String password = authentication.getCredentials().toString();

		// Prepare the request body
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/json");
		String requestBody = "{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}";
		HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

		// Make a POST request to your authentication API
		ResponseEntity<String> response = restTemplate.exchange(Constants.auth_server + "/api/oauth/token",
				HttpMethod.POST, requestEntity, String.class);

		// Check if the response is successful (e.g., status code 200)
		if (response.getStatusCode().is2xxSuccessful()) {
			User usr = new User();
			usr.setUsername(username);
			try {
				ObjectMapper objectMapper = new ObjectMapper();

				JsonNode jsonNode = objectMapper.readTree(response.getBody());
				usr.setToken(jsonNode.get("accessToken").asText());
				JsonNode perms = jsonNode.get("permissions");
				// Iterate through the array elements
				if (perms.isArray()) {
					for (JsonNode element : perms) {
						// Access and print array elements
						if (element != null) {
							usr.addPermission(element.toString());
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (response.getBody().contains("homework.create")) {
				Authentication auth = new UsernamePasswordAuthenticationToken(username, password,
						List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
				HashMap<String, Object> info = new HashMap<String, Object>();
			    info.put("currentUser", usr);
			    ((AbstractAuthenticationToken) auth).setDetails(info);
				return auth;
			} else {
				Authentication auth = new UsernamePasswordAuthenticationToken(username, password,
						List.of(new SimpleGrantedAuthority("ROLE_USER")));
				HashMap<String, Object> info = new HashMap<String, Object>();
			    info.put("currentUser", usr);
			    ((AbstractAuthenticationToken) auth).setDetails(info);
				return auth;
			}
		} else {
			throw new BadCredentialsException("Authentication failed");
		}
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}
}