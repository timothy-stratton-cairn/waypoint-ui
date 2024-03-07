package com.cairn.ui.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.cairn.ui.model.User;
import com.cairn.ui.model.UserDAO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpSession;

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
		ResponseEntity<String> response = restTemplate.exchange("http://96.61.158.12:8082/api/oauth/token",
				HttpMethod.POST, requestEntity, String.class);

		// Check if the response is successful (e.g., status code 200)
		if (response.getStatusCode().is2xxSuccessful()) {
			User usr = new User();
			usr.setUsername(username);
			try {
				ObjectMapper objectMapper = new ObjectMapper();

				JsonNode jsonNode = objectMapper.readTree(response.getBody());
				usr.setToken(jsonNode.get("accessToken").toString());
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
				ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder
						.currentRequestAttributes();
				HttpSession session = attr.getRequest().getSession(true);
				session.setAttribute("CurUser", usr);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (response.getBody().contains("homework.create")) {
				return new UsernamePasswordAuthenticationToken(username, password,
						List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
			} else {
				return new UsernamePasswordAuthenticationToken(username, password,
						List.of(new SimpleGrantedAuthority("ROLE_USER")));
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