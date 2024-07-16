package com.cairn.ui.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.cairn.ui.Constants;
import com.cairn.ui.model.Household;
import com.cairn.ui.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class UserHelper {
    Logger logger = LoggerFactory.getLogger(UserHelper.class); 

	@Value("${waypoint.authorization-api.base-url}")
	private String authorizationApiBaseUrl;

	private APIHelper apiHelper = new APIHelper();

	/**
	 * Get the dashboard stats
	 * 
	 * @return
	 */
	
	
	public ArrayList<User> getUserList(User usr) {
	    ArrayList<User> results = new ArrayList<User>();

	    String apiUrl = this.authorizationApiBaseUrl + Constants.api_userlist_get;
	    String jsonResponse = apiHelper.callAPI(apiUrl, usr);
	    if (!jsonResponse.isEmpty()) {
	        ObjectMapper objectMapper = new ObjectMapper();

	        try {
	            JsonNode jsonNode = objectMapper.readTree(jsonResponse);
	            JsonNode accountsNode = jsonNode.get("accounts");

	            if (accountsNode.isArray()) {
	                for (JsonNode element : accountsNode) {
	                    if (element != null) {
	                        User entry = new User();
	                        entry.setId(element.get("id").asInt());
	                        entry.setFirstName(element.get("firstName").asText());
	                        entry.setLastName(element.get("lastName").asText());
	                        entry.setEmail(element.get("email").asText());

	                        // Set the role
	                        JsonNode accountRolesNode = element.get("accountRoles").get("roles");
	                        ArrayList<String> userRoles = new ArrayList<>();
	                        if (accountRolesNode != null && accountRolesNode.isArray()) {
	                            for (JsonNode roleNode : accountRolesNode) {
	                                userRoles.add(roleNode.asText());
	                            }
	                        }
	                        entry.setRoles(userRoles);

	                        // Parse and set dependents
	                        JsonNode dependentsNode = element.get("dependents");
	                        if (dependentsNode != null && dependentsNode.isArray()) {
	                            ArrayList<User> dependents = new ArrayList<>();
	                            for (JsonNode dep : dependentsNode) {
	                                User dependent = new User();
	                                dependent.setId(dep.get("id").asInt());
	                                dependent.setFirstName(dep.get("firstName").asText());
	                                dependent.setLastName(dep.get("lastName").asText());
	                                dependent.setUsername(dep.get("username").asText());
	                                dependents.add(dependent);
	                            }
	                            entry.setDependents(dependents);
	                        }

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
	        logger.info("Failed to fetch getUserList data.");
	    }

	    return results;
	}




	public Household getHouseholdById(User usr, int id) {
	    String apiUrl = this.authorizationApiBaseUrl + Constants.api_household_get + id;
		String jsonResponse = apiHelper.callAPI(apiUrl, usr);
		if (!jsonResponse.isEmpty()) {
			ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode;
            try {
                jsonNode = objectMapper.readTree(jsonResponse);

                // Access relevant nodes directly
                JsonNode primaryContacts = jsonNode.get("primaryContacts");
                JsonNode householdAccounts = jsonNode.get("householdAccounts");

                // Construct Household object
                Household household = new Household();
                household.setId(jsonNode.get("id").asInt());
                household.setName(jsonNode.get("name").asText());

                // Populate primary contact details
                if (primaryContacts != null && primaryContacts.has("accounts")) {
                    JsonNode primaryAccounts = primaryContacts.get("accounts");
                    if (primaryAccounts.isArray() && primaryAccounts.size() > 0) {
                        ArrayList<User> primaryContactsList = new ArrayList<>();
                        for (JsonNode account : primaryAccounts) {
                            User user = new User();
                            user.setId(account.get("accountId").asInt()); // Assuming "accountId" is the correct field name
                            user.setFirstName(account.get("firstName").asText());
                            user.setLastName(account.get("lastName").asText());
                            user.setEmail(account.get("email").asText());
                            user.setRole("PRIMARY_CONTACT");
                            user.setPhoneNumber(account.get("phoneNumber").asText());
                            // Add more properties as needed
                            primaryContactsList.add(user);
                        }
                        household.setPrimaryContacts(primaryContactsList);
                    }
                }

                // Populate household accounts
                if (householdAccounts != null && householdAccounts.has("accounts")) {
                    JsonNode accounts = householdAccounts.get("accounts");
                    if (accounts.isArray()) {
                        ArrayList<User> householdAccountsList = new ArrayList<>();
                        for (JsonNode account : accounts) {
                            User user = new User();
                            user.setId(account.get("clientAccountId").asInt()); // Assuming "clientAccountId" is the correct field name
                            user.setFirstName(account.get("firstName").asText());
                            user.setLastName(account.get("lastName").asText());
                            // Add more properties as needed
                            householdAccountsList.add(user);
                        }
                        household.setHouseholdAccounts(householdAccountsList);
                    }
                }

                return household; // Return the constructed household
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            logger.info("Failed to fetch household data.");
        }
	    return null; // Return null if the household with the given ID is not found
	}



    public ArrayList<Household> getHouseholdList(User usr) {
        ArrayList<Household> results = new ArrayList<Household>();
        String apiUrl = this.authorizationApiBaseUrl + Constants.api_household;
		String jsonResponse = apiHelper.callAPI(apiUrl, usr);
		if (!jsonResponse.isEmpty()) {
			ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode;
            try {
                jsonNode = objectMapper.readTree(jsonResponse);
                JsonNode households = jsonNode.get("households");
                if (households.isArray()) {
                    for (JsonNode element : households) {
                        Household household = new Household();
                        household.setId(Integer.valueOf(element.get("id").asText()));
                        household.setName(element.get("name").asText());

                        // Parse household accounts
                        JsonNode householdAccounts = element.get("householdAccounts");
                        if (householdAccounts != null) {
                            JsonNode accounts = householdAccounts.get("accounts");
                            if (accounts.isArray()) {
                                ArrayList<User> householdUsers = new ArrayList<>();
                                for (JsonNode account : accounts) {
                                    User user = new User();
                                    user.setId(Integer.valueOf(account.get("id").asText()));
                                    user.setFirstName(account.get("firstName").asText());
                                    user.setLastName(account.get("lastName").asText());

                                    user.setRole(account.get("role").asText());
  
                                        householdUsers.add(user);
                                    }
                                    household.setHouseholdAccounts(householdUsers);
                                }
                            }

                            // Add household to results list
                        results.add(household);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            logger.info("Failed to fetch getHouseholdList data.");
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

        String apiUrl = this.authorizationApiBaseUrl + Constants.api_userlist_get + "/" + uid;
        String jsonResponse = apiHelper.callAPI(apiUrl, usr);
        if (!jsonResponse.isEmpty()) {
            ObjectMapper objectMapper = new ObjectMapper();

            try {
                JsonNode jsonNode = objectMapper.readTree(jsonResponse);
                result.setId(uid);
                result.setFirstName(jsonNode.path("firstName").asText(null));
                result.setLastName(jsonNode.path("lastName").asText(null));
                result.setUsername(jsonNode.path("username").asText(null));
                result.setEmail(jsonNode.path("email").asText(null));

                JsonNode accountRoles = jsonNode.path("accountRoles").path("roles");
                JsonNode deps = jsonNode.path("dependents");
                JsonNode coclient = jsonNode.path("coClient");

                // Handle coClient
                if (coclient != null && !coclient.isNull()) {
                    User temp = new User();
                    temp.setId(coclient.path("id").asInt());
                    temp.setFirstName(coclient.path("firstName").asText(null));
                    temp.setLastName(coclient.path("lastName").asText(null));
                    temp.setUsername(coclient.path("username").asText(null));
                    result.setCoclient(temp);
                }

                // Handle roles
                ArrayList<String> userRoles = new ArrayList<>();
                if (accountRoles.isArray()) {
                    for (JsonNode element : accountRoles) {
                        if (element != null && !element.isNull()) {
                            userRoles.add(element.asText());
                        }
                    }
                }
                result.setRoles(userRoles);

                // Handle dependents
                ArrayList<User> userDeps = new ArrayList<>();
                if (deps.isArray()) {
                    for (JsonNode element : deps) {
                        if (element != null && !element.isNull()) {
                            User temp = new User();
                            temp.setId(element.path("id").asInt());
                            temp.setFirstName(element.path("firstName").asText(null));
                            temp.setLastName(element.path("lastName").asText(null));
                            temp.setUsername(element.path("username").asText(null));
                            userDeps.add(temp);
                        }
                    }
                }
                result.setDependents(userDeps);

            } catch (JsonMappingException e) {
                logger.error("JsonMappingException while parsing user data: ", e);
            } catch (JsonProcessingException e) {
                logger.error("JsonProcessingException while parsing user data: ", e);
            }
        } else {
            logger.info("Failed to fetch getUser data.");
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
		int result = apiHelper.postAPI(apiUrl, requestBody, loggedInUser);
		logger.info(apiUrl);
		if (result < 0) {
			return "error ";
		}
		return "success " + "id: "+ result;
	}

	
	
	public int updateUserDetails(User usr, int id ,String firstName, String lastName, String email, int role) {
		int result = 0;
		logger.info("Calling userhelper updateUserDetails. FirstName: "+ firstName + " LastName: "+lastName+" Email: "+email );
		String requestBody = "{\"firstName\":\"" + firstName + "\", \"lastName\":\"" + lastName + "\", \"email\":\"" + email + "\", \"roleIds\":[" + role + "]}";	
	    String apiUrl = this.authorizationApiBaseUrl + Constants.api_userlist_get +"/"+ id ;
	    logger.info(apiUrl);
		result = apiHelper.patchAPI(apiUrl, requestBody, usr);
		return result;
	}
	
	
	
	public String changeUserPassword(User usr,int id, String oldPassword, String newPassword) {
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Authorization", "Bearer " + usr.getToken());
	    headers.add("Content-Type", "application/json");
	    String requestBody = "{\"oldPassword\":\"" + oldPassword + "\", \"newPassword\":\"" + newPassword + "\"}";
	    String apiUrl = this.authorizationApiBaseUrl + Constants.api_userlist_get + "/" + id + "/reset-password";
		int result = apiHelper.postAPI(apiUrl, requestBody, usr);
		if (result < 0) {
			return "error ";
		}
		return "success";
	}

	public int getUserId(User usr) {
		int result = 0;
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Authorization", "Bearer " + usr.getToken());
	    headers.add("Content-Type", "application/json");
	    String apiUrl = this.authorizationApiBaseUrl + Constants.api_me;
		String jsonResponse = apiHelper.callAPI(apiUrl, usr);
		int accountId = 0;
		if (!jsonResponse.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root;
			try {
				root = mapper.readTree(jsonResponse);
	            accountId = root.path("accountId").asInt(); // Extract accountId
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            result = accountId;
        } else {
            logger.info("Failed to fetch userId data.");
        }
        return result; 
    }
	
	public int getHouseholdId(User usr) {
		int result = 0;
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Authorization", "Bearer " + usr.getToken());
	    headers.add("Content-Type", "application/json");
	    String apiUrl = this.authorizationApiBaseUrl+ Constants.api_me;
		String jsonResponse = apiHelper.callAPI(apiUrl, usr);
        int accountId = 0;
		if (!jsonResponse.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root;
			try {
				root = mapper.readTree(jsonResponse);
	            accountId = root.path("householdId").asInt(); // Extract accountId
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            result = accountId;
        } else {
            logger.info("Failed to fetch householdId data.");
        }
        return result; 
    }

	
	/*public int addDependant(User usr, User client, ArrayList<User> users) {
	    int result = 0;
	    logger.info("Calling addDepenedant from Helper");
	    HttpHeaders headers = new HttpHeaders();
	    headers.add("Authorization", "Bearer " + usr.getToken());
	    headers.add("Content-Type", "application/json");

	    String apiUrl = this.authorizationApiBaseUrl + Constants.api_userlist_get + "/"+ client.getId();
	    
	    StringBuilder tempBody = new StringBuilder("\"dependants\":[");
	    for (int i = 0; i < users.size(); i++) {
	        User user = users.get(i);
	        tempBody.append(String.format("{\"id\":%d,\"firstName\":\"%s\",\"lastName\":\"%s\",\"userName\":\"%s\"}",
	            user.getId(), user.getFirstName(), user.getLastName(), user.getUsername()));
	        if (i < users.size() - 1) {
	            tempBody.append(",");
	        }
	    }
	    tempBody.append("]}");
	    String requestBody = String.format("{\"firstName\":\"%s\",\"lastName\":\"%s\",\"email\":\"%s\",",client.getFirstName(),client.getLastName(),client.getEmail());
	    requestBody = requestBody+tempBody.toString();
	    result = apiHelper.patchAPI(apiUrl,requestBody,usr);
		return result;
	}*/
	
	public String addDependant(User usr, int clientId, ArrayList<Integer> dependentList) {
	    String result = "error";
	    String apiUrl = this.authorizationApiBaseUrl + Constants.api_userlist_get + "/" + clientId;
	    
	    StringBuilder requestBody = new StringBuilder("{ \"dependentIds\": [");
	    
	    for (int i = 0; i < dependentList.size(); i++) {
	        requestBody.append(dependentList.get(i));
	        if (i < dependentList.size() - 1) {
	            requestBody.append(", ");
	        }
	    }
	    
	    requestBody.append("] }");
	    
	    logger.info(apiUrl);
	    logger.info(requestBody.toString());
	    
	    int call = apiHelper.patchAPI(apiUrl, requestBody.toString(), usr);  // Assuming requestBody is the second parameter
	    if (call > 0) {
	        result = "success: Id: " + call;
	    }

	    return result;
	}



	public int addHouseholdAccount(User usr, Household household, ArrayList<Integer> householdIds) {
	    int result = -1;

	    // Convert primary contacts to a list of their IDs
	    ArrayList<Integer> primaryContactIds = household.getPrimaryContacts().stream()
	            .map(User::getId)
	            .collect(Collectors.toCollection(ArrayList::new));

	    // Create a map to hold the request body data
	    Map<String, Object> requestBodyMap = new HashMap<>();
	    requestBodyMap.put("householdAccountIds", householdIds);
	    requestBodyMap.put("name", household.getName());
	    requestBodyMap.put("description", household.getDescription());
	    requestBodyMap.put("primaryContactAccountIds", primaryContactIds);

	    // Convert the map to a JSON string
	    ObjectMapper objectMapper = new ObjectMapper();
	    String requestBody = "";
	    try {
	        requestBody = objectMapper.writeValueAsString(requestBodyMap);
	    } catch (Exception e) {
	        e.printStackTrace();
	        // Handle the exception if necessary
	    }

	    String apiUrl = this.authorizationApiBaseUrl + Constants.api_household_get + household.getId();

	    logger.info("URL: " + apiUrl);
	    logger.info("REQUEST BODY: " + requestBody);

	    result = apiHelper.patchAPI(apiUrl, requestBody, usr);
	    return result;
	}
	
	
	public int addCoClient(User usr, User client, User coClient) {
		logger.info("Calling addCoClient from User Helper with ClientId: "+ client.getId() + " and CoClientId: " +coClient.getId());
	    int result = -1;
	    HttpHeaders headers = new HttpHeaders();
	    headers.add("Authorization", "Bearer " + usr.getToken());
	    headers.setContentType(MediaType.APPLICATION_JSON);

	    // Create the request body JSON structure using a Map
	    Map<String, Object> requestBodyMap = new HashMap<>();
	    requestBodyMap.put("id", client.getId());
	    requestBodyMap.put("username", client.getUsername());
	    requestBodyMap.put("firstName", client.getFirstName());
	    requestBodyMap.put("lastName", client.getLastName());
	    requestBodyMap.put("email", client.getEmail());
	    requestBodyMap.put("roles", client.getRoles());

	    Map<String, Object> coClientMap = new HashMap<>();
	    coClientMap.put("id", coClient.getId());
	    coClientMap.put("firstName", coClient.getFirstName());
	    coClientMap.put("lastName", coClient.getLastName());
	    coClientMap.put("username", coClient.getUsername());

	    requestBodyMap.put("coClient", coClientMap);

	    // Convert the Map to JSON
	    ObjectMapper mapper = new ObjectMapper();
	    String requestBody;
	    try {
	        requestBody = mapper.writeValueAsString(requestBodyMap);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return result;
	    }

	    String apiUrl = this.authorizationApiBaseUrl + Constants.api_userlist_get + "/" + client.getId();
	    result = apiHelper.patchAPI(apiUrl,requestBody,usr);
		return result;
	}
	
	
	public String newHousehold( User usr, Household household) {
		String result = "Error: Household not created";
		String apiUrl = this.authorizationApiBaseUrl + Constants.api_household;
		String requestBody = "{\"name\":\"" + household.getName() + "\", " +
                "\"description\":\"" + household.getDescription() + "\", " +
                "\"householdAccountIds\":[";

		boolean first = true;
		for (int userId : household.getHouseholdAccountsIds()) {
			if (!first) {
			   requestBody += ",";
				}
			requestBody += userId;
			first = false;
			}
	
		requestBody += "], \"primaryContactAccountIds\":[";
		
		first = true;
		for (int pUserId : household.getPrimaryContactsIds()) {
			if (!first) {
			   requestBody += ",";
			}
		requestBody += pUserId;
			first = false;
			}
		
		requestBody += "]}";
		
		int call = apiHelper.postAPI(apiUrl, requestBody, usr);
		if (call>0) {
			result = "Success";
		}
		
				
		return result;
				
	}
		
    public String sendResetUserPasswordEmail(User usr, User client) {
        if (usr == null || client == null) {
            logger.error("User or client is null");
            return "Error: User or client is null";
        }

        if (client.getUsername() == null || client.getEmail() == null) {
            logger.error("Client username or email is null");
            return "Error: Client username or email is null";
        }

        String apiUrl = this.authorizationApiBaseUrl + Constants.api_userlist_get + "/password/reset?username=" + client.getUsername() + "&email=" + client.getEmail();
        logger.info("API URL: " + apiUrl);

        try {
            String call = apiHelper.callAPI(apiUrl, usr);
            logger.info("API Response: " + call);
            return call;
        } catch (Exception e) {
            logger.error("Error calling API: " + e.getMessage(), e);
            return "Error calling API: " + e.getMessage();
        }
    }

    public String resetUserPasswordEmail(User usr, String username, String email) {
        if (usr == null) {
            logger.error("User is null");
            return "Error: User is null";
        }

        if (username == null || email == null) {
            logger.error("Username or email is null");
            return "Error: Username or email is null";
        }

        String apiUrl = this.authorizationApiBaseUrl + Constants.api_userlist_get + "/password/reset?username=" + username + "&email=" + email;
        logger.info("API URL: " + apiUrl);

        try {
            String call = apiHelper.callAPI(apiUrl, usr);
            logger.info("API Response: " + call);
            return call;
        } catch (Exception e) {
            logger.error("Error calling API: " + e.getMessage(), e);
            return "Error calling API: " + e.getMessage();
        }
    }
	public String createDependent(User loggedInUser, User newUser) {
		ArrayList<String> roles = newUser.getRoles();
		int role = Integer.parseInt(roles.get(0)); // Assumption: roles are integers
	    String requestBody = String.format(
	            "{\"firstName\": \"%s\", \"lastName\": \"%s\", \"roleIds\": [%d], \"email\": \"%s\"}",
	            newUser.getFirstName(), newUser.getLastName(),role,newUser.getEmail()
	        );
	    String apiUrl = this.authorizationApiBaseUrl + Constants.api_userlist_get;
		int result = apiHelper.postAPI(apiUrl, requestBody, loggedInUser);
		logger.info(apiUrl);
		if (result < 0) {
			return "error ";
		}
		return "success " + "id: "+ result;
	}
	
	
}
