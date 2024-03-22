package com.cairn.ui.model;

import java.util.ArrayList;
import java.util.Date;

public class Dashboard {
	private ArrayList<ProtocolStats> protocols = new ArrayList<ProtocolStats>();

	public void addProtolStat(ProtocolStats stats) {
		this.protocols.add(stats);
	}
	
	public ArrayList<ProtocolStats> getProtocolStats() {
		return this.protocols;
	}	
}
