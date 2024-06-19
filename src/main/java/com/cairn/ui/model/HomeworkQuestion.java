package com.cairn.ui.model;

import org.springframework.web.multipart.MultipartFile;

public class HomeworkQuestion {
	private int questionId;
    private String questionAbbreviation;
    private String question;
    private String questionType;
    private boolean isRequired;
    private boolean isTriggeringReponse;
    private HomeworkResponse triggerResponse;
    private int triggerProtocolId;
    private String status;
    private String userResponse;
    private MultipartFile file;
    private ExpectedHomeworkResponses expectedHomeworkResponses;

    // Getters and setters
    
    public int getQuestionId() {
    	return questionId;
    }
    public void setQuestionId(int questionId ) {
    	this.questionId = questionId;
    }
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

    public String getUserResponse() {
        return userResponse;
    }

    public void setUserResponse(String userResponse) {
        this.userResponse = userResponse;
    }
    public MultipartFile getFile() {
    return file;
    }
    public void setFile(MultipartFile file) {
		this.file = file;
	}

    public ExpectedHomeworkResponses getExpectedHomeworkResponses() {
        return expectedHomeworkResponses;
    }

    public void setExpectedHomeworkResponses(
        ExpectedHomeworkResponses expectedHomeworkResponses) {
        this.expectedHomeworkResponses = expectedHomeworkResponses;
    }
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public boolean getIsTriggeringReponse() {
		return isTriggeringReponse;
	}
	public void setTriggeringReponse(boolean isTriggeringReponse) {
		this.isTriggeringReponse = isTriggeringReponse;
	}
	public HomeworkResponse getTriggerResponse() {
		return triggerResponse;
	}
	public void setTriggerResponse(HomeworkResponse triggerResponse) {
		this.triggerResponse = triggerResponse;
	}
	public int getTriggerProtocolId() {
		return triggerProtocolId;
	}
	public void setTriggerProtocolId(int triggerProtocolId) {
		this.triggerProtocolId = triggerProtocolId;
	}
}


