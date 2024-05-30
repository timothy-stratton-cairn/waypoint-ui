package com.cairn.ui.model;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

public class ProtocolReport{
	private int id;
	private int meanDays;
	private String name;
	private int medDays;
	private int high;
	private int highId;
	private int low;
	private int lowId;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getMeanDays() {
		return meanDays;
	}
	public void setMeanDays(int meanDays) {
		this.meanDays = meanDays;
	}
	public int getMedDays() {
		return medDays;
	}
	public void setMedDays(int medDays) {
		this.medDays = medDays;
	}
	public int getHigh() {
		return high;
	}
	public void setHigh(int high) {
		this.high = high;
	}
	public int getHighId() {
		return highId;
	}
	public void setHighId(int highId) {
		this.highId = highId;
	}
	public int getLow() {
		return low;
	}
	public void setLow(int low) {
		this.low = low;
	}
	public int getLowId() {
		return lowId;
	}
	public void setLowId(int lowId) {
		this.lowId = lowId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}