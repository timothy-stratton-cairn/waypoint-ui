package com.cairn.ui.helper;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.cairn.ui.Constants;
import com.cairn.ui.model.Entity;
import com.cairn.ui.model.ProtocolStats;
import com.cairn.ui.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class UserHelper {

	@Value("${waypoint.authorization-api.base-url}")
	private String authorizationApiBaseUrl;
    private RestTemplate restTemplate;


    private RestTemplate getRestTemplate() {
        if (this.restTemplate == null) {
            // Using HttpComponentsClientHttpRequestFactory to support PATCH
            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setConnectTimeout(5000); 
            this.restTemplate = new RestTemplate(requestFactory);
        }
        return this.restTemplate;
    }

	/**
	 * Get the dashboard stats
	 * 
	 * @return
	 */
	public ArrayList<User> getUserList(User usr) {
		ArrayList<User> results = new ArrayList<User>();

		String apiUrl = this.authorizationApiBaseUrl + Constants.api_userlist_get;
		HttpEntity<String> entity = Entity.getEntity(usr, apiUrl);
		// Make the GET request and retrieve the response
		try {
			ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.GET, entity, String.class);
			// Process the response
			if (response.getStatusCode().is2xxSuccessful()) {
				String jsonResponse = response.getBody();
				ObjectMapper objectMapper = new ObjectMapper();

				JsonNode jsonNode;
				try {
					jsonNode = objectMapper.readTree(jsonResponse);
					JsonNode prots = jsonNode.get("accounts");
					// Iterate through the array elements
					User entry = null;
					if (prots.isArray()) {
						for (JsonNode element : prots) {
							// Access and print array elements
							if (element != null) {
								entry = new User();
								entry.setId(Integer.valueOf(element.get("id").toString()));
								entry.setFirstName(element.get("firstName").asText());
								entry.setLastName(element.get("lastName").asText());
								results.add(entry);
							}
						}
					}
				} catch (JsonMappingException e) {
					e.printStackTrace();
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("Failed to fetch data. Status code: " + response.getStatusCode());
			}
		} catch (Exception e) {
			System.out.println("No user data returned");
			e.printStackTrace();
		}

		return results;

	}
	
	/**
	 * Get the user details
	 * 
	 * @return
	 */
	public User getUser(User usr, int uid) {
		User result = new User();

		// Prepare the request body

		// Create a HttpEntity with the headers
		
		
		String apiUrl = Constants.auth_server + Constants.api_userlist_get + "/" + uid;
		HttpEntity<String> entity = Entity.getEntity(usr, apiUrl);
		//String apiUrl = this.authorizationApiBaseUrl + Constants.api_userlist_get + "/" + uid;

		// Make the GET request and retrieve the response
		try {
			ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.GET, entity, String.class);
			// Process the response
			if (response.getStatusCode().is2xxSuccessful()) {
				String jsonResponse = response.getBody();
				ObjectMapper objectMapper = new ObjectMapper();

				JsonNode jsonNode;
				try {
					jsonNode = objectMapper.readTree(jsonResponse);
					result = new User();
					result.setId(uid);
					result.setFirstName(jsonNode.get("firstName").asText());
					result.setLastName(jsonNode.get("lastName").asText());
					result.setUsername(jsonNode.get("username").asText());
					result.setEmail(jsonNode.get("email").asText());
					JsonNode roles = jsonNode.get("roles");
					JsonNode deps = jsonNode.get("dependents");
					JsonNode coclient = jsonNode.get("coClient");
					User temp = new User();
					if (!coclient.toString().equals("null")) {
						temp = new User();
						temp.setId(Integer.valueOf(coclient.get("id").toString()));
						temp.setFirstName(coclient.get("firstName").asText());
						temp.setLastName(coclient.get("lastName").asText());
						temp.setUsername(coclient.get("username").asText());
						result.setCoclient(temp);
					}
					// Iterate through the array elements
					ArrayList<String> userRoles = new ArrayList<String>(); 
					if (roles.isArray()) {
						for (JsonNode element : roles) {
							// Access and print array elements
							if (element != null) {
								userRoles.add(element.asText());
							}
						}
					}
					result.setRoles(userRoles);
					ArrayList<User> userDeps = new ArrayList<User>(); 
					if (deps.isArray()) {
						for (JsonNode element : deps) {
							// Access and print array elements
							if (element != null) {
								temp = new User();
								temp.setId(Integer.valueOf(element.get("id").toString()));
								temp.setFirstName(element.get("firstName").asText());
								temp.setLastName(element.get("lastName").asText());
								temp.setUsername(element.get("username").asText());
								userDeps.add(temp);
							}
						}
					}
					result.setDependents(userDeps);
				} catch (JsonMappingException e) {
					e.printStackTrace();
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("Failed to fetch data. Status code: " + response.getStatusCode());
			}
		} catch (Exception e) {
			System.out.println("No user data returned");
			e.printStackTrace();
		}

		return result;

	}
	
	
	public String addUser(User loggedInUser, User newUser) {

		ArrayList<String> roles = newUser.getRoles();
		int role = Integer.parseInt(roles.get(0)); // Assumption: roles are integers
	    String requestBody = String.format(
	            "{\"username\": \"%s\", \"firstName\": \"%s\", \"lastName\": \"%s\", \"roleIds\": [%d], \"email\": \"%s\", \"password\": \"%s\"}",
	            newUser.getUsername(), newUser.getFirstName(), newUser.getLastName(),role,newUser.getEmail() , newUser.getPassword()
	        );
	    String apiUrl = this.authorizationApiBaseUrl + Constants.api_userlist_get;
	    HttpEntity<String> entity = Entity.getEntityWithBody(loggedInUser, apiUrl,requestBody);
		System.out.println(apiUrl);
		System.out.println(entity);
	    try {
	        ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.POST, entity, String.class);
	        if (response.getStatusCode().is2xxSuccessful()) {
	            return "Success";
	        } else {
	            return "Error: " + response.getStatusCode() + " - " + response.getBody();
	        }
	    } catch (HttpClientErrorException e) {
	        return "Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString();
	    } catch (Exception e) {
	        e.printStackTrace();
	        return "Error: An internal error occurred";
	    }
	

	}

	
	
	public int updateUserDetails(User usr, int id ,String firstName, String lastName, String email) {
		int result = 0;
		System.out.println("Calling userhelper updateUserDetails. FirstName: "+ firstName + " LastName: "+lastName+" Email: "+email );
		
	    String requestBody = "{\"firstName\":\"" + firstName + "\", \"lastName\":\"" + lastName + "\", \"email\":\"" + email + "\"}";

	    String apiUrl = Constants.auth_server + Constants.api_userlist_get +"/"+ id ;
	    System.out.println(apiUrl);
	    HttpEntity<String> entity = Entity.getEntityWithBody(usr, apiUrl,requestBody);

	    try {
	        ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.PATCH, entity, String.class);
	        if (response.getStatusCode().is2xxSuccessful()) {
	        	System.out.println("Success!");
	            result = 1;
	        } else {
	        	
	            System.out.println("Failed to fetch data. Status code: " + response.getStatusCode());
	            // Update result to indicate a specific type of failure
	        }  
			
		}
		catch(Exception e) {
			System.out.println("Error in updating User Details");
	        e.printStackTrace();
		}
		return result;
	}
	
	
	public String changeUserPassword(User usr,int id, String oldPassword, String newPassword) {
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Authorization", "Bearer " + usr.getToken());
	    headers.add("Content-Type", "application/json");
	    String requestBody = "{\"oldPassword\":\"" + oldPassword + "\", \"newPassword\":\"" + newPassword + "\"}";
	    String apiUrl = Constants.auth_server + Constants.api_userlist_get + "/" + id + "/reset-password";
	    HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
	    try {
	        ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.POST, entity, String.class);
	        if (response.getStatusCode().is2xxSuccessful()) {
	            return "Success";
	        } else {
	            return "Error: " + response.getStatusCode() + " - " + response.getBody();
	        }
		    } catch (HttpClientErrorException e) {
		        return "Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString();
		    } catch (Exception e) {
		        e.printStackTrace();
		        return "Error: An internal error occurred";
		    }
	    }

	public int getUserId(User usr) {
		int result = 0;
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Authorization", "Bearer " + usr.getToken());
	    headers.add("Content-Type", "application/json");
	    String apiUrl = Constants.auth_server + Constants.api_me;
	    HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper mapper = new ObjectMapper();
                String json = response.getBody();
                JsonNode root = mapper.readTree(json);
                int accountId = root.path("accountId").asInt(); // Extract accountId
                result = accountId;
            } else {
                System.out.println("Failed to fetch data. Status code: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.out.println("Error in getting User ID");
            e.printStackTrace();
            result = -1;
        }
        return result; 
    }

	
	public int addDependant(User usr, int clientid, ArrayList<User> users) {
	    int result = 0;

	    HttpHeaders headers = new HttpHeaders();
	    headers.add("Authorization", "Bearer " + usr.getToken());
	    headers.add("Content-Type", "application/json");

	    String apiUrl = "http://96.61.158.12:8082" + Constants.api_me;

	    StringBuilder tempBody = new StringBuilder("{ \"dependants\":[");
	    for (int i = 0; i < users.size(); i++) {
	        User user = users.get(i);
	        tempBody.append(String.format("{\"id\":%d,\"firstName\":\"%s\",\"lastName\":\"%s\",\"userName\":\"%s\"}",
	            user.getId(), user.getFirstName(), user.getLastName(), user.getUsername()));
	        if (i < users.size() - 1) {
	            tempBody.append(",");
	        }
	    }
	    tempBody.append("]}");
	    String requestBody = tempBody.toString();
	    
	    HttpEntity<String> entity = Entity.getEntityWithBody(usr, apiUrl,requestBody);

	    try {
	        ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.PATCH, entity, String.class);
	        if (response.getStatusCode().is2xxSuccessful()) {

	            result = 1;
	        } else {
	        	result = -1;
	            System.out.println("Failed to fetch data. Status code: " + response.getStatusCode());
	            // Update result to indicate a specific type of failure
	        }  
			
		}
		catch(Exception e) {
			System.out.println("Error in updating User Details");
	        e.printStackTrace();
		}
		return result;
	}

	
}
