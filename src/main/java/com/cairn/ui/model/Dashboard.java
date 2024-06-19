package com.cairn.ui.model;

import java.util.ArrayList;

public class Dashboard {
	private ArrayList<ProtocolStats> protocols = new ArrayList<ProtocolStats>();

	public void addProtolStat(ProtocolStats stats) {
		this.protocols.add(stats);
	}
	
	public ArrayList<ProtocolStats> getProtocolStats() {
		return this.protocols;
	}	
}
