package com.cairn.ui.model;

import java.util.List;

public class HomeworkQuestion {
    private String questionAbbreviation;
    private String question;
    private String questionType;
    private boolean isRequired;
    private ExpectedHomeworkResponses expectedHomeworkResponses;

    // Getters and setters
    public String getQuestionAbbreviation() {
        return questionAbbreviation;
    }

    public void setQuestionAbbreviation(String questionAbbreviation) {
        this.questionAbbreviation = questionAbbreviation;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean required) {
        isRequired = required;
    }

    public ExpectedHomeworkResponses getExpectedHomeworkResponses() {
        return expectedHomeworkResponses;
    }

    public void setExpectedHomeworkResponses(ExpectedHomeworkResponses expectedHomeworkResponses) {
        this.expectedHomeworkResponses = expectedHomeworkResponses;
    }
}


