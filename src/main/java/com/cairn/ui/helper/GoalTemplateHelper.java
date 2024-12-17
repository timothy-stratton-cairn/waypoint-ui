package com.cairn.ui.helper;

import com.cairn.ui.Constants;
import com.cairn.ui.dto.GoalCategoryDto;
import com.cairn.ui.dto.GoalTemplateDto;
import com.cairn.ui.dto.GoalTemplateListDto;
import com.cairn.ui.model.Entity;
import com.cairn.ui.model.GoalTemplate;
import com.cairn.ui.model.GoalCategory;
import com.cairn.ui.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
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

        for (GoalTemplateDto dto : goalTemplateList.getGoalTemplates()) {
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
}
