package com.cairn.ui.helper;

import com.cairn.ui.Constants;
import com.cairn.ui.dto.AddGoalTemplateDto;
import com.cairn.ui.dto.GoalTemplateDto;
import com.cairn.ui.dto.GoalTemplateListDto;
import com.cairn.ui.model.Entity;
import com.cairn.ui.model.GoalTemplate;
import com.cairn.ui.model.GoalCategory;
import com.cairn.ui.model.ProtocolTemplate;
import com.cairn.ui.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GoalTemplateHelper {

  Logger logger = LoggerFactory.getLogger(GoalTemplateHelper.class);
  private APIHelper apiHelper = new APIHelper();

  @Value("${waypoint.dashboard-api.base-url}")
  private String dashboardApiBaseUrl;

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


  public ArrayList<GoalTemplate> getGoalTemplates(User user) {
    ArrayList<GoalTemplate> goalTemplates = new ArrayList<>();
    String apiUrl = dashboardApiBaseUrl + Constants.api_goal_template_get;
    HttpEntity<String> entity = Entity.getEntity(user, apiUrl);

    String jsonResponse = apiHelper.callAPI(apiUrl, user);

    if (!jsonResponse.isEmpty()) {
      ObjectMapper objectMapper = new ObjectMapper();
      try {

        GoalTemplateListDto goalTemplateList =
            objectMapper.readValue(jsonResponse, GoalTemplateListDto.class);

        for (GoalTemplateDto dto : goalTemplateList.getTemplates()) {
          GoalCategory category = null;

          if (dto.getCategory() != null) {
            category = new GoalCategory();
            category.setCategoryId(dto.getCategory().getId());
            category.setCategoryName(dto.getCategory().getName());
            category.setDescription(dto.getCategory().getDescription());
          }

          GoalTemplate goalTemplate = new GoalTemplate();
          goalTemplate.setId(dto.getId());
          goalTemplate.setName(dto.getName());
          goalTemplate.setDescription(dto.getDescription());
          goalTemplate.setCategory(category);

          goalTemplates.add(goalTemplate);
        }

      } catch (Exception e) {
        logger.error("Error processing JSON response for Goal Templates", e);
      }
    }

    return goalTemplates;
  }

  public int createGoalTemplate(User user, AddGoalTemplateDto dto) {
    String apiUrl = dashboardApiBaseUrl + "/api/goal-template"; // Endpoint path
    ObjectMapper objectMapper = new ObjectMapper();
    try {

      String requestBody = objectMapper.writeValueAsString(dto);

      int resultId = apiHelper.postAPI(apiUrl, requestBody, user);

      if (resultId > 0) {
        logger.info("Successfully created Goal Template with ID: " + resultId);
      } else {
        logger.error("Failed to create Goal Template. API returned: " + resultId);
      }

      return resultId;
    } catch (Exception e) {
      logger.error("Error creating Goal Template", e);
      return -1;
    }
  }


  public ArrayList<ProtocolTemplate> getProtocolListByGoalTemplate(User usr, int templateId) {
    ArrayList<ProtocolTemplate> results = new ArrayList<>();

    if (usr == null) {
      logger.warn("User is null. Returning empty results.");
      return results;
    }

    String apiUrl = this.dashboardApiBaseUrl + "/api/goal-templates/linked-protocols/" + templateId;
    logger.info("Fetching protocol templates from API URL: {}", apiUrl);

    String jsonResponse = apiHelper.callAPI(apiUrl, usr);

    if (jsonResponse == null || jsonResponse.isEmpty()) {
      logger.error("API response is empty for templateId: {}", templateId);
      return results;
    }

    logger.info("Raw JSON Response: {}", jsonResponse);

    ObjectMapper objectMapper = new ObjectMapper();

    try {
      JsonNode jsonNode = objectMapper.readTree(jsonResponse);

      if (jsonNode.isArray()) { // Since the response is an array
        for (JsonNode element : jsonNode) {
          if (element != null) {
            ProtocolTemplate entry = new ProtocolTemplate();
            entry.setId(element.path("id").asInt());
            entry.setName(element.path("name").asText(null));
            entry.setDescription(element.path("description").asText(null));
            entry.setStatus(element.path("status").asText(null));
            results.add(entry);
          }
        }
      } else {
        logger.warn("Expected an array but received non-array JSON for templateId: {}", templateId);
      }
    } catch (JsonMappingException e) {
      logger.error("Error mapping JSON response for templateId: {}", templateId, e);
    } catch (JsonProcessingException e) {
      logger.error("Error processing JSON response for templateId: {}", templateId, e);
    }

    logger.info("Fetched {} protocol templates for templateId: {}", results.size(), templateId);
    return results;
  }


  public GoalTemplate getGoalTemplateById(User usr, Long templateId) {
    GoalTemplate goalTemplate = null;

    String apiUrl = this.dashboardApiBaseUrl + Constants.api_goal_template_get +"/" + templateId;

    logger.info("Constructed API URL: {}", apiUrl);

    String jsonResponse = apiHelper.callAPI(apiUrl, usr);

    if (jsonResponse == null || jsonResponse.isEmpty()) {
      logger.error("Empty response from API for templateId {}", templateId);
      return goalTemplate;
    }

    logger.info("Raw JSON Response: {}", jsonResponse);

    ObjectMapper objectMapper = new ObjectMapper();
    try {
      JsonNode rootNode = objectMapper.readTree(jsonResponse);

      goalTemplate = new GoalTemplate();
      goalTemplate.setId(rootNode.path("id").asLong());
      goalTemplate.setName(rootNode.path("name").asText("Unnamed Goal"));
      goalTemplate.setDescription(rootNode.path("description").asText("No Description"));
      goalTemplate.setCategory(null); // Assuming category is not available in the response

    } catch (JsonProcessingException e) {
      logger.error("Error parsing JSON response for templateId {}", templateId, e);
    }

    logger.info("Successfully parsed GoalTemplate: {}", goalTemplate);
    return goalTemplate;
  }

}
