package com.cairn.ui.dto;

import java.util.List;

public class HomeworkListDto {

	private List<HomeworkDto> homeworks;

	public Integer getNumOfHomeworks() {
		return homeworks.size();
	}

	public List<HomeworkDto> getHomeworks() {
		return homeworks;
	}

	public void setHomeworks(List<HomeworkDto> homeworks) {
		this.homeworks = homeworks;
	}
}
