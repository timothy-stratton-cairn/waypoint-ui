package com.cairn.ui.model;

public class ReportStat {
	private Double min;
	private Double max;
	private Double median;
	private Double average;
	private String name;
	
	public Double getMin() {
		return min;
	}
	public Double getMax() {
		return max;
	}
	public Double getMedian() {
		return median;
	}
	public Double getAverage() {
		return average;
	}
	public String getName() {
		return name;
	}
	public void setMin(Double min) {
		this.min = min;
	}
	public void setMax(Double max) {
		this.max = max;
	}
	public void setMedian(Double median) {
		this.median = median;
	}
	public void setAverage(Double average) {
		this.average = average;
	}
	public void setName(String name) {
		this.name = name;
	}
}
