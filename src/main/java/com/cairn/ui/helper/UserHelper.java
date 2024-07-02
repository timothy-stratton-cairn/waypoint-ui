package com.cairn.ui.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
							entry.setEmail(element.get("email").asText());
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

		// Prepare the request body

		// Create a HttpEntity with the headers
		
		
		String apiUrl = Constants.auth_server + Constants.api_userlist_get + "/" + uid;
		String jsonResponse = apiHelper.callAPI(apiUrl, usr);
		if (!jsonResponse.isEmpty()) {
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
		if (result != 0) {
			return "error ";
		}
		return "success";
	}

	
	
	public int updateUserDetails(User usr, int id ,String firstName, String lastName, String email, int role) {
		int result = 0;
		logger.info("Calling userhelper updateUserDetails. FirstName: "+ firstName + " LastName: "+lastName+" Email: "+email );
		String requestBody = "{\"firstName\":\"" + firstName + "\", \"lastName\":\"" + lastName + "\", \"email\":\"" + email + "\", \"roleIds\":[" + role + "]}";	
	    String apiUrl = Constants.auth_server + Constants.api_userlist_get +"/"+ id ;
	    logger.info(apiUrl);
		result = apiHelper.patchAPI(apiUrl, requestBody, usr);
		return result;
	}
	
	
	
	public String changeUserPassword(User usr,int id, String oldPassword, String newPassword) {
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Authorization", "Bearer " + usr.getToken());
	    headers.add("Content-Type", "application/json");
	    String requestBody = "{\"oldPassword\":\"" + oldPassword + "\", \"newPassword\":\"" + newPassword + "\"}";
	    String apiUrl = Constants.auth_server + Constants.api_userlist_get + "/" + id + "/reset-password";
		int result = apiHelper.postAPI(apiUrl, requestBody, usr);
		if (result != 0) {
			return "error ";
		}
		return "success";
	}

	public int getUserId(User usr) {
		int result = 0;
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Authorization", "Bearer " + usr.getToken());
	    headers.add("Content-Type", "application/json");
	    String apiUrl = Constants.auth_server + Constants.api_me;
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
	    String apiUrl = Constants.auth_server + Constants.api_me;
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

	
	public int addDependant(User usr, User client, ArrayList<User> users) {
	    int result = 0;
	    logger.info("Calling addDepenedant from Helper");
	    HttpHeaders headers = new HttpHeaders();
	    headers.add("Authorization", "Bearer " + usr.getToken());
	    headers.add("Content-Type", "application/json");

	    String apiUrl = Constants.auth_server + Constants.api_userlist_get + "/"+ client.getId();
	    
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
	}

	public int addHouseholdAccount(User usr, int primaryContactId, ArrayList<Integer> householdIds) {
		int result = -1;

		String requestBody = "{\"householdAccountIds\":" + householdIds.toString() + "}";
		
	    String apiUrl = Constants.auth_server + Constants.api_household_get +primaryContactId;
	    
	    logger.info("URL: "+apiUrl);
	    logger.info("REQUEST BODY: "+requestBody);
	    result = apiHelper.patchAPI(apiUrl,requestBody,usr);
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

	    String apiUrl = Constants.auth_server + Constants.api_userlist_get + "/" + client.getId();
	    result = apiHelper.patchAPI(apiUrl,requestBody,usr);
		return result;
	}
	
	
	
	
}
