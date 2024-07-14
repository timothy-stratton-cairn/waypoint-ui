package com.cairn.ui.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cairn.ui.model.Household;
import com.cairn.ui.model.Protocol;
import com.cairn.ui.model.ProtocolReport;
import com.cairn.ui.model.ProtocolStep;
import com.cairn.ui.model.ProtocolStepTemplate;
import com.cairn.ui.model.ProtocolTemplate;
import com.cairn.ui.model.ReportStat;
import com.cairn.ui.model.User;

@Service
public class ReportHelper {
    Logger logger = LoggerFactory.getLogger(ProtocolHelper.class);
	@Autowired
	ProtocolHelper protocolHelper;
	
	@Autowired
	UserHelper userHelper;

	/**
	 * Get the report data for general statistics.
	 * 
	 * @return
	 */
	public ArrayList<ReportStat> getStatsReport(User usr) {
		ArrayList<ReportStat> results = new ArrayList<ReportStat>();

		// Get protocol numbers assigned to clients.
		ArrayList<User> users = userHelper.getUserList(usr);
		ReportStat theStat = new ReportStat();
		int idx = 0;
		int numbers[] = new int[users.size()];
		for (User client : users) {
			ArrayList<Protocol> prots = protocolHelper.getAssignedProtocols(usr, client.getId());
			numbers[idx++] = prots.size();
		}
		theStat.setName("Client Protocols");
		double temp = 0.0;
		for (int val : numbers) {
			temp += val;
		}
		Arrays.sort(numbers);
		theStat.setMin((double) numbers[0]);
		theStat.setMax((double) numbers[idx - 1]);
		theStat.setMedian((double) numbers[idx / 2]);
		theStat.setAverage(temp / (double) idx);
		results.add(theStat);

		return results;
	}
	
	public ArrayList<ProtocolReport> protocolCompletionReportByTemplate(User currentUser, ArrayList<ProtocolTemplate> pcolList) {
	    ArrayList<ProtocolReport> reports = new ArrayList<>();


	    // Map to hold ProtocolTemplate ID and corresponding list of Protocols
	    Map<Integer, ArrayList<Protocol>> protocolMap = new HashMap<>();

	    // Populate the map with Protocols grouped by ProtocolTemplate ID
	    for (ProtocolTemplate template : pcolList) {
	        ArrayList<Protocol> protocols = protocolHelper.getListbyTemplateId(currentUser, template.getId());
	        protocolMap.put(template.getId(), protocols);
	    }

	    // Process each entry in the map to create ProtocolReports
	    for (Map.Entry<Integer, ArrayList<Protocol>> entry : protocolMap.entrySet()) {
	        Integer templateId = entry.getKey();
	        ArrayList<Protocol> protocols = entry.getValue();

	        // Filter out protocols with daysToComplete less than zero
	        List<Protocol> validProtocols = protocols.stream()
	        										 .filter(protocol -> protocol.getDaysToComplete() >= 0)
	                                                 .collect(Collectors.toList());

	        if (validProtocols.isEmpty()) {
	            continue;
	        }

	        ProtocolReport report = new ProtocolReport();
	        report.setId(templateId);
	        report.setName(pcolList.stream()
	                               .filter(template -> template.getId() == templateId)
	                               .findFirst()
	                               .map(ProtocolTemplate::getName)
	                               .orElse("Unknown"));

	        // Calculate mean and median daysToComplete
	        List<Integer> daysToCompleteList = validProtocols.stream()
	                                                         .map(Protocol::getDaysToComplete)
	                                                         .sorted()
	                                                         .collect(Collectors.toList());

	        double meanDaysToComplete = daysToCompleteList.stream().mapToInt(Integer::intValue).average().orElse(0);
	        int medianDaysToComplete = daysToCompleteList.size() % 2 == 0
	                ? (daysToCompleteList.get(daysToCompleteList.size() / 2 - 1) + daysToCompleteList.get(daysToCompleteList.size() / 2)) / 2
	                : daysToCompleteList.get(daysToCompleteList.size() / 2);

	        report.setMeanDays((int) meanDaysToComplete);
	        report.setMedDays(medianDaysToComplete);

	        // Get the Protocol with the lowest and highest daysToComplete
	        Protocol minProtocol = Collections.min(validProtocols, Comparator.comparingInt(Protocol::getDaysToComplete));
	        Protocol maxProtocol = Collections.max(validProtocols, Comparator.comparingInt(Protocol::getDaysToComplete));

	        report.setLow(minProtocol.getDaysToComplete());
	        report.setHigh(maxProtocol.getDaysToComplete());
	        report.setLowId(minProtocol.getId());
	        report.setHighId(maxProtocol.getId());


	        reports.add(report);
	    }

	    return reports;
	}
	
	public ArrayList<ProtocolReport> protocolCompletionReportByHousehold(User currentUser, ArrayList<Household> households) {
	    ArrayList<ProtocolReport> reports = new ArrayList<>();
	    logger.info("Calling ProtocolCompletionReportByHousehold");

	    // Map to hold Household ID and corresponding list of Protocols
	    Map<Integer, ArrayList<Protocol>> protocolMap = new HashMap<>();

	    // Populate the map with Protocols grouped by Household ID
	    for (Household household : households) {
	       // logger.info("Processing Household ID: " + household.getId() + " Household Name: " + household.getName());
	        ArrayList<Protocol> protocols = protocolHelper.getAssignedProtocols(currentUser, household.getId());
	       // logger.info("Retrieved " + protocols.size() + " protocols for Household ID: " + household.getId());
	        protocolMap.put(household.getId(), protocols);
	    }

	    // Process each entry in the map to create ProtocolReports
	    for (Map.Entry<Integer, ArrayList<Protocol>> entry : protocolMap.entrySet()) {
	        Integer householdId = entry.getKey();
	        ArrayList<Protocol> protocols = entry.getValue();

	        //logger.info("Processing protocols for Household ID: " + householdId);

	        // Filter out protocols with daysToComplete less than zero
	        List<Protocol> validProtocols = protocols.stream()
	                                                 .filter(protocol -> protocol.getDaysToComplete() >= 0)
	                                                 .collect(Collectors.toList());

	       // logger.info("Filtered valid protocols count: " + validProtocols.size() + " for Household ID: " + householdId);

	        if (validProtocols.isEmpty()) {
	            //logger.info("No valid protocols found for Household ID: " + householdId);
	            continue;
	        }

	        ProtocolReport report = new ProtocolReport();
	        report.setId(householdId);
	        households.stream()
	                  .filter(household -> household.getId() == householdId)
	                  .findFirst()
	                  .ifPresent(household -> {
	                      //logger.info("Setting report name for Household ID: " + householdId);
	                      report.setName(household.getName());
	                  });

	        // Calculate mean and median daysToComplete
	        List<Integer> daysToCompleteList = validProtocols.stream()
	                                                         .map(Protocol::getDaysToComplete)
	                                                         .sorted()
	                                                         .collect(Collectors.toList());

	        double meanDaysToComplete = daysToCompleteList.stream()
	                                                      .mapToInt(Integer::intValue)
	                                                      .average()
	                                                      .orElse(0);
	        int medianDaysToComplete = daysToCompleteList.size() % 2 == 0
	                ? (daysToCompleteList.get(daysToCompleteList.size() / 2 - 1) + daysToCompleteList.get(daysToCompleteList.size() / 2)) / 2
	                : daysToCompleteList.get(daysToCompleteList.size() / 2);

	       // logger.info("Mean days to complete for Household ID: " + householdId + " is " + meanDaysToComplete);
	       // logger.info("Median days to complete for Household ID: " + householdId + " is " + medianDaysToComplete);

	        report.setMeanDays((int) meanDaysToComplete);
	        report.setMedDays(medianDaysToComplete);

	        // Get the Protocol with the lowest and highest daysToComplete
	        Protocol minProtocol = Collections.min(validProtocols, Comparator.comparingInt(Protocol::getDaysToComplete));
	        Protocol maxProtocol = Collections.max(validProtocols, Comparator.comparingInt(Protocol::getDaysToComplete));

	        //logger.info("Lowest days to complete for Household ID: " + householdId + " is " + minProtocol.getDaysToComplete() + " with Protocol ID: " + minProtocol.getId());
	       // logger.info("Highest days to complete for Household ID: " + householdId + " is " + maxProtocol.getDaysToComplete() + " with Protocol ID: " + maxProtocol.getId());

	        report.setLow(minProtocol.getDaysToComplete());
	        report.setHigh(maxProtocol.getDaysToComplete());
	        report.setLowId(minProtocol.getId());
	        report.setHighId(maxProtocol.getId());

	        reports.add(report);
	    }

	    //logger.info("Total reports generated: " + reports.size());
	    return reports;
	}


	public ArrayList<ProtocolReport> stepCompletionReportByHousehold(User currentUser, ArrayList<Household> households) {
	    ArrayList<ProtocolReport> reports = new ArrayList<>();
	    logger.info("Calling stepCompletionReportByHousehold");

	    // Map to hold Household ID and corresponding list of ProtocolSteps
	    Map<Integer, ArrayList<ProtocolStep>> stepMap = new HashMap<>();
	
	    // Populate the map with ProtocolSteps grouped by Household ID
	    for (Household household : households) {
	        logger.info("Processing Household ID: " + household.getId() + " Household Name: " + household.getName());
	        ArrayList<Protocol> protocols = protocolHelper.getAssignedProtocols(currentUser, household.getId());
	        ArrayList<ProtocolStep> allSteps = new ArrayList<>();
	        
	        for (Protocol protocol : protocols) {
	            allSteps.addAll(protocol.getSteps());
	        }

	        logger.info("Retrieved " + allSteps.size() + " steps for Household ID: " + household.getId());
	        stepMap.put(household.getId(), allSteps);
	    }

	    // Process each entry in the map to create ProtocolReports
	    for (Map.Entry<Integer, ArrayList<ProtocolStep>> entry : stepMap.entrySet()) {
	        Integer householdId = entry.getKey();
	        ArrayList<ProtocolStep> steps = entry.getValue();

	        //logger.info("Processing steps for Household ID: " + householdId);

	        // Filter out steps with daysToComplete less than zero
	        List<ProtocolStep> validSteps = steps.stream()
	                                             .filter(step -> step.getDaysToComplete() >= 0)
	                                             .collect(Collectors.toList());

	        logger.info("Filtered valid steps count: " + validSteps.size() + " for Household ID: " + householdId);

	        if (validSteps.isEmpty()) {
	            //logger.info("No valid steps found for Household ID: " + householdId);
	            continue;
	        }

	        ProtocolReport report = new ProtocolReport();
	        report.setId(householdId);
	        households.stream()
	                  .filter(household -> household.getId() == householdId)
	                  .findFirst()
	                  .ifPresent(household -> {
	                      logger.info("Setting report name for Household ID: " + householdId);
	                      report.setName(household.getName());
	                  });

	        // Calculate mean and median daysToComplete
	        List<Integer> daysToCompleteList = validSteps.stream()
	                                                     .map(ProtocolStep::getDaysToComplete)
	                                                     .sorted()
	                                                     .collect(Collectors.toList());

	        double meanDaysToComplete = daysToCompleteList.stream()
	                                                      .mapToInt(Integer::intValue)
	                                                      .average()
	                                                      .orElse(0);
	        int medianDaysToComplete = daysToCompleteList.size() % 2 == 0
	                ? (daysToCompleteList.get(daysToCompleteList.size() / 2 - 1) + daysToCompleteList.get(daysToCompleteList.size() / 2)) / 2
	                : daysToCompleteList.get(daysToCompleteList.size() / 2);

	        //logger.info("Mean days to complete for Household ID: " + householdId + " is " + meanDaysToComplete);
	        //logger.info("Median days to complete for Household ID: " + householdId + " is " + medianDaysToComplete);

	        report.setMeanDays((int) meanDaysToComplete);
	        report.setMedDays(medianDaysToComplete);

	        // Get the ProtocolStep with the lowest and highest daysToComplete
	        ProtocolStep minStep = Collections.min(validSteps, Comparator.comparingInt(ProtocolStep::getDaysToComplete));
	        ProtocolStep maxStep = Collections.max(validSteps, Comparator.comparingInt(ProtocolStep::getDaysToComplete));

	        //logger.info("Lowest days to complete for Household ID: " + householdId + " is " + minStep.getDaysToComplete() + " with ProtocolStep ID: " + minStep.getId());
	        //logger.info("Highest days to complete for Household ID: " + householdId + " is " + maxStep.getDaysToComplete() + " with ProtocolStep ID: " + maxStep.getId());

	        report.setLow(minStep.getDaysToComplete());
	        report.setHigh(maxStep.getDaysToComplete());
	        report.setLowId(minStep.getId());
	        report.setHighId(maxStep.getId());

	        reports.add(report);
	    }

	    logger.info("Total reports generated: " + reports.size());
	    return reports;
	}
	
	public ArrayList<ProtocolReport> stepCompletionReportTemplate(User currentUser, ArrayList<ProtocolStepTemplate> stepTemplates, ArrayList<Household> households) {
	    ArrayList<ProtocolReport> reports = new ArrayList<>();
	    logger.info("Calling stepCompletionReportTemplate");

	    // Map to hold ProtocolStepTemplate ID and corresponding list of ProtocolSteps
	    Map<Integer, ArrayList<ProtocolStep>> stepMap = new HashMap<>();

	    // Populate the map with ProtocolSteps grouped by ProtocolStepTemplate ID
	    for (Household household : households) {
	        logger.info("Processing Household ID: " + household.getId() + " Household Name: " + household.getName());
	        ArrayList<Protocol> protocols = protocolHelper.getAssignedProtocols(currentUser, household.getId());
	        ArrayList<ProtocolStep> allSteps = new ArrayList<>();

	        for (Protocol protocol : protocols) {
	            allSteps.addAll(protocol.getSteps());
	        }

	        logger.info("Retrieved " + allSteps.size() + " steps for Household ID: " + household.getId());
	        for (ProtocolStep step : allSteps) {
	            int templateId = step.getTemplateId();
	            stepMap.computeIfAbsent(templateId, k -> new ArrayList<>()).add(step);
	        }
	    }

	    // Process each entry in the map to create ProtocolReports
	    for (Map.Entry<Integer, ArrayList<ProtocolStep>> entry : stepMap.entrySet()) {
	        Integer templateId = entry.getKey();
	        ArrayList<ProtocolStep> steps = entry.getValue();

	        //logger.info("Processing steps for Template ID: " + templateId);

	        // Filter out steps with daysToComplete less than zero
	        List<ProtocolStep> validSteps = steps.stream()
	                                             .filter(step -> step.getDaysToComplete() >= 0)
	                                             .collect(Collectors.toList());

	        logger.info("Filtered valid steps count: " + validSteps.size() + " for Template ID: " + templateId);

	        if (validSteps.isEmpty()) {
	            //logger.info("No valid steps found for Template ID: " + templateId);
	            continue;
	        }

	        ProtocolReport report = new ProtocolReport();
	        report.setId(templateId);
	        stepTemplates.stream()
	                     .filter(template -> template.getId() == templateId)
	                     .findFirst()
	                     .ifPresent(template -> {
	                         logger.info("Setting report name for Template ID: " + templateId);
	                         report.setName(template.getName());
	                     });

	        // Calculate mean and median daysToComplete
	        List<Integer> daysToCompleteList = validSteps.stream()
	                                                     .map(ProtocolStep::getDaysToComplete)
	                                                     .sorted()
	                                                     .collect(Collectors.toList());

	        double meanDaysToComplete = daysToCompleteList.stream()
	                                                      .mapToInt(Integer::intValue)
	                                                      .average()
	                                                      .orElse(0);
	        int medianDaysToComplete = daysToCompleteList.size() % 2 == 0
	                ? (daysToCompleteList.get(daysToCompleteList.size() / 2 - 1) + daysToCompleteList.get(daysToCompleteList.size() / 2)) / 2
	                : daysToCompleteList.get(daysToCompleteList.size() / 2);

	        //logger.info("Mean days to complete for Template ID: " + templateId + " is " + meanDaysToComplete);
	        //logger.info("Median days to complete for Template ID: " + templateId + " is " + medianDaysToComplete);

	        report.setMeanDays((int) meanDaysToComplete);
	        report.setMedDays(medianDaysToComplete);

	        // Get the ProtocolStep with the lowest and highest daysToComplete
	        ProtocolStep minStep = Collections.min(validSteps, Comparator.comparingInt(ProtocolStep::getDaysToComplete));
	        ProtocolStep maxStep = Collections.max(validSteps, Comparator.comparingInt(ProtocolStep::getDaysToComplete));

	        //logger.info("Lowest days to complete for Template ID: " + templateId + " is " + minStep.getDaysToComplete() + " with ProtocolStep ID: " + minStep.getId());
	        //logger.info("Highest days to complete for Template ID: " + templateId + " is " + maxStep.getDaysToComplete() + " with ProtocolStep ID: " + maxStep.getId());

	        report.setLow(minStep.getDaysToComplete());
	        report.setHigh(maxStep.getDaysToComplete());
	        report.setLowId(minStep.getId());
	        report.setHighId(maxStep.getId());

	        reports.add(report);
	    }

	    logger.info("Total reports generated: " + reports.size());
	    return reports;
	}


	
}