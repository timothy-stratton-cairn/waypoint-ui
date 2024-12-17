package com.cairn.ui.helper;

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


}
