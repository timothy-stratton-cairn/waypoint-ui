package com.cairn.ui.model;

import java.util.ArrayList;

public class ProtocolTemplate {

	private int id;
	private String name;
	private String description;
	private int dueByDay;
	private int dueByMonth;
	private int dueByYear;
	private String daySchedule;
	private String monthSchedule;
	private String yearSchedule;
	private String schedule;
	private String type;
	private String status ;
	private ArrayList<ProtocolStepTemplate> steps = new ArrayList<ProtocolStepTemplate>();

	public int getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public void setId(int id) {
		this.id = id;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		/* 2 types of protocols *
		  0: Undefined
		  1: Life Events
		  2: Life Cycle */
		return type;
	}
	public ArrayList<ProtocolStepTemplate> getSteps() {
		return steps;
	}
	public void setSteps(ArrayList<ProtocolStepTemplate> steps) {
		this.steps = steps;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getSchedule() {
		return schedule;
	}
	public void setSchedule(String schedule) {
		this.schedule = schedule;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public int getDueByDay() {
		return dueByDay;
	}
	public void setDueByDay(int duebyDay) {
		this.dueByDay = duebyDay;
	}
	public int getDueByMonth() {
		return dueByMonth;
	}
	public void setDueByMonth(int duebyMonth) {
		this.dueByMonth = duebyMonth;
	}
	public int getDueByYear() {
		return dueByYear;
	}
	public void setDueByYear(int dueByYear) {
		this.dueByYear = dueByYear;
	}
	public String getDaySchedule() {
		return daySchedule;
	}
	public void setDaySchedule(String daySchedule) {
		this.daySchedule = daySchedule;
	}
	public String getMonthSchedule() {
		return monthSchedule;
	}
	public void setMonthSchedule(String monthSchedule) {
		this.monthSchedule = monthSchedule;
	}
	public String getYearSchedule() {
		return yearSchedule;
	}
	public void setYearSchedule(String yearSchedule) {
		this.yearSchedule = yearSchedule;
	}
}
