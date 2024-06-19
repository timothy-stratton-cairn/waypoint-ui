package com.cairn.ui.dto;

import java.util.List;

public class HomeworkQuestionListDto {

	private List<HomeworkQuestionDto> questions;
	private Integer numOfQuestions;

	public Integer getNumOfQuestions() {
		return questions.size();
	}

	public List<HomeworkQuestionDto> getQuestions() {
		return questions;
	}

	public void setQuestions(List<HomeworkQuestionDto> questions) {
		this.questions = questions;
	}

	public void setNumOfQuestions(Integer numOfQuestions) {
		this.numOfQuestions = numOfQuestions;
	}
}
