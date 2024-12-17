package com.cairn.ui.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.io.OutputStream;
import com.cairn.ui.model.Entity;
import com.cairn.ui.model.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class APIHelper {
    Logger logger = LoggerFactory.getLogger(APIHelper.class); 

	private RestTemplate restTemplate;

	private RestTemplate getRestTemplate() {
		if (this.restTemplate == null) {
			// Using HttpComponentsClientHttpRequestFactory to support PATCH
			HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
			requestFactory.setConnectTimeout(5000); // Optional: set connection timeout
			this.restTemplate = new RestTemplate(requestFactory);
		}
		return this.restTemplate;
	}

	public String callAPI(String apiUrl, User usr) {
		String jsonResponse = "";
		HttpEntity<String> entity = Entity.getEntity(usr, apiUrl);
		// Make the GET request and retrieve the response
		try {
			ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.GET, entity, String.class);
			jsonResponse = response.getBody();

			// Process the response
			if (response.getStatusCode().is2xxSuccessful()) {
				
			} else {
				logger.info("Failed to fetch data " + apiUrl + ". Status code: " + response.getStatusCode());
			}
		} catch (Exception e) {

			logger.info("No records returned for " + apiUrl + e);
		}
		return jsonResponse;
	}
  public String postWithoutBodyAPI(String apiUrl, User usr) {
    String jsonResponse = "";
    HttpEntity<String> entity = Entity.getEntity(usr, apiUrl);
    // Make the GET request and retrieve the response
    try {
      ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.POST, entity, String.class);
      jsonResponse = response.getBody();

      // Process the response
      if (response.getStatusCode().is2xxSuccessful()) {

      } else {
        logger.info("Failed to fetch data " + apiUrl + ". Status code: " + response.getStatusCode());
      }
    } catch (Exception e) {

      logger.info("No records returned for " + apiUrl + e);
    }
    return jsonResponse;
  }
	
    public String callAPINoAuth(String apiUrl) {
        String jsonResponse = "";
        try {
            ResponseEntity<String> response = getRestTemplate().getForEntity(apiUrl, String.class);
            jsonResponse = response.getBody();
            if (response.getStatusCode().is2xxSuccessful()) {
            	return jsonResponse;
            } else {
                logger.info("Failed to fetch data from " + apiUrl + ". Status code: " + response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("Error calling API: " + e.getMessage(), e);
        }
        return jsonResponse;
    }
    
	public String postAPINoAuth(String apiUrl, String requestBody) {
		String result = "";
		HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("accept", "application/json");
		HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
		logger.info("Headers: " + headers);
		try {
			ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.POST, entity, String.class);
			if (response.getStatusCode().is2xxSuccessful()) {
				result = response.getBody();
			} else {
				result = "Error";
				logger.warn(apiUrl + "==>Failed to fetch data. Status code: " + response.getStatusCode());
			}
	
		} catch (Exception e) {
			logger.info("Error in updating note");
			e.printStackTrace();
		}
		return result;
	}

    public static String postAPICurl(String apiUrl, String requestBody) {
        String result = "";
        try {
            // Construct the cURL command
            String[] cmd = {
                "curl",
                "-X", "POST",
                apiUrl,
                "-H", "Content-Type: application/json",
                "-H", "accept: application/json",
                "-d", requestBody,
                "-w", "%{http_code}"  // Append the HTTP status code to the output
            };

            // Execute the cURL command
            ProcessBuilder processBuilder = new ProcessBuilder(cmd);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Read the response
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Wait for the process to complete
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                String responseString = response.toString();
                // Extract the HTTP status code
                String httpStatusCode = responseString.substring(responseString.length() - 3);
                // Extract the response body
                String responseBody = responseString.substring(0, responseString.length() - 3);

                if (httpStatusCode.startsWith("2")) { // Check if status code is 2xx
                    result = "Success";
                } else {
                    result = "Error: HTTP status code " + httpStatusCode + ". Response: " + responseBody;
                }
            } else {
                result = "Error: cURL command failed with exit code " + exitCode;
            }

        } catch (Exception e) {
            e.printStackTrace();
            result = "Error: " + e.getMessage();
        }
        return result;
    }

	/*public int postAPI(String apiUrl, String requestBody, User usr) {
		int result = 0;
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + usr.getToken());
		headers.add("Content-Type", "application/json");
		HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
		try {
			ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.POST, entity, String.class);
			if (response.getStatusCode().is2xxSuccessful()) {
				String rspd = "Server REsponse"+ response;
				logger.info(rspd);
				result = 1;
			} else {
				result = -1;
				logger.warn(apiUrl + "==>Failed to fetch data. Status code: " + response.getStatusCode());
			}

		} catch (Exception e) {
			logger.info("Error in updating note");
			e.printStackTrace();
		}
		return result;
	}*/
    public int postAPI(String apiUrl, String requestBody, User usr) {
        int result = 0;
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + usr.getToken());
        headers.add("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = null;
        try {
            response = getRestTemplate().exchange(apiUrl, HttpMethod.POST, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                logger.info("Server Response: " + responseBody);

                if (responseBody != null) {
                    MediaType contentType = response.getHeaders().getContentType();

                    if (contentType != null && contentType.includes(MediaType.APPLICATION_JSON)) {
                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode jsonResponse = objectMapper.readTree(responseBody);
                        if (jsonResponse.has("id")) {
                            result = jsonResponse.get("id").asInt();
                        } else {
                            result = 1; // Success but no ID in the response
                        }
                    } else {
                        // Handle as plain text
                        logger.info("Plain text response received");
                        result = 1; // Treat as success without ID
                    }
                }
            } else {
                result = -1;
                logger.info(apiUrl + "==>Failed to fetch data. Status code: " + response.getStatusCode());
            }

        } catch (Exception e) {
            result = -1;
            System.out.println(response);
        }
        String resultMsg = "Result Id: " + result;
        logger.info(resultMsg);
        return result;
    }
    public int postFileAPI(String apiUrl, MultipartFile file, User usr) {
        int result = 0;
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + usr.getToken());
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        File binaryFile = new File("src/main/resources/targetFile.tmp");

        try (OutputStream os = new FileOutputStream(binaryFile)) {
            os.write(file.getBytes());
        } catch (IOException e) {
            logger.error("Error in creating temporary file", e);
            return -1;
        }

        body.add("file", new FileSystemResource(binaryFile));

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.POST, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                MediaType contentType = response.getHeaders().getContentType();

                if (contentType != null && contentType.includes(MediaType.APPLICATION_JSON)) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonResponse = objectMapper.readTree(responseBody);
                    if (jsonResponse.has("id")) {
                        result = jsonResponse.get("id").asInt();
                    } else {
                        result = 1; // Success but no ID in the response
                    }
                } else {
                    // Handle plain text response
                    logger.info("Server Response: " + responseBody);
                    result = 1; // Assuming success based on the response content
                }
            } else {
                result = -1;
                logger.error("Failed to fetch data. Status code: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            logger.error("HttpClientErrorException: " + e.getStatusCode() + " : " + e.getResponseBodyAsString());
            result = -1;
        } catch (Exception e) {
            logger.error("Exception in postFileAPI", e);
            result = -1;
        }

        logger.info("Result Id: " + result);
        return result;
    }




	public int deleteAPI(String apiUrl, User usr) {
		int result = 0;
        HttpEntity<String> entity = Entity.getEntity(usr, apiUrl);
        try {
			ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.DELETE, entity, String.class);
			if (response.getStatusCode().is2xxSuccessful()) {
			result = 1;
			}
        }
	    catch (Exception e) {
	        logger.error("Error in deleting " + apiUrl);
	        e.printStackTrace();
	    }
        
		return result;
	}

	public int patchAPI(String apiUrl, String requestBody, User usr) {
		int result = 0;
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + usr.getToken());
        headers.add("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
		try {
			ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.PATCH, entity,
					String.class);
			System.out.print(response);
			if (response.getStatusCode().is2xxSuccessful()) {
				result = 1;
			} else {
				result = -1;
				logger.info(apiUrl + "==>Failed to fetch data. Status code: " + response.getStatusCode());
				// Update result to indicate a specific type of failure
			}

		} catch (Exception e) {
			logger.info("Error in Patch PAI Call: " + apiUrl + e);
			e.printStackTrace();
		}
		return result;
	}

	public String postAPIResponse(String apiUrl, String requestBody, User usr) {
		String result = "";
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + usr.getToken());
		headers.add("Content-Type", "application/json");
		HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
		try {
			ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.POST, entity, String.class);
			if (response.getStatusCode().is2xxSuccessful()) {
				result = response.getBody();
			} else {
				result = "Error";
				logger.warn(apiUrl + "==>Failed to fetch data. Status code: " + response.getStatusCode());
			}
	
		} catch (Exception e) {
			logger.info("Error in updating note");
			e.printStackTrace();
		}
		return result;
	}

  public String callPatchAPI(String apiUrl, User usr) {
    String jsonResponse = "";
    HttpEntity<String> entity = Entity.getEntity(usr, apiUrl);
    // Make the GET request and retrieve the response
    try {
      ResponseEntity<String> response = getRestTemplate().exchange(apiUrl, HttpMethod.PATCH, entity, String.class);
      jsonResponse = response.getBody();

      // Process the response
      if (response.getStatusCode().is2xxSuccessful()) {

      } else {
        logger.info("Failed to fetch data " + apiUrl + ". Status code: " + response.getStatusCode());
      }
    } catch (Exception e) {

      logger.info("No records returned for " + apiUrl + ":Error==>" + e);
    }
    return jsonResponse;
  }
}
