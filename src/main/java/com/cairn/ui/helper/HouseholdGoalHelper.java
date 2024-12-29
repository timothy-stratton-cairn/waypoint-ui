package com.cairn.ui.helper;

import com.cairn.ui.dto.HouseholdGoldProtocolsDto;
import com.cairn.ui.model.ProtocolTemplate;
import com.cairn.ui.model.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class HouseholdGoalHelper {

  Logger logger = LoggerFactory.getLogger(HouseholdGoalHelper.class);
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


  public ArrayList<HouseholdGoldProtocolsDto> getHouseholdGoalProtocolsByHouseholdID(User usr, int householdId) {
    ArrayList<HouseholdGoldProtocolsDto> results = new ArrayList<>();

    if (usr == null) {
      logger.warn("User is null. Returning empty results.");
      return results;
    }

    String apiUrl = this.dashboardApiBaseUrl + "/api/household/goals-and-protocols/" + householdId + "/";
    logger.info("Fetching protocol templates from API URL: {}", apiUrl);

    String jsonResponse = apiHelper.callAPI(apiUrl, usr);

    if (jsonResponse == null || jsonResponse.isEmpty()) {
      logger.error("API response is empty for Household ID: {}", householdId);
      return results;
    }

    logger.info("Raw JSON Response: {}", jsonResponse);

    try {
      ObjectMapper objectMapper = new ObjectMapper();
      List<HouseholdGoldProtocolsDto> householdGoalProtocols = objectMapper.readValue(
          jsonResponse, new TypeReference<List<HouseholdGoldProtocolsDto>>() {});

      results.addAll(householdGoalProtocols);

    } catch (Exception e) {
      logger.error("Failed to parse JSON response for Household ID [{}]. Error: {}", householdId, e.getMessage());
    }

    return results;
  }

}
