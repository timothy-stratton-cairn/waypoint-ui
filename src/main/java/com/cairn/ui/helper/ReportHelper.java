package com.cairn.ui.helper;

import java.util.ArrayList;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.cairn.ui.model.Protocol;
import com.cairn.ui.model.ReportStat;
import com.cairn.ui.model.User;

@Service
public class ReportHelper {
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
}