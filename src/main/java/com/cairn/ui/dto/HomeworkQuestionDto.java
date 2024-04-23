package com.cairn.ui.dto;


public class HomeworkQuestionDto {

  private String questionAbbr;
  private String question;
  private String userResponse;
  private Boolean isRequired;
  private String questionType;
  private Boolean triggersProtocolCreation;
  private ProtocolTemplateDto triggeredProtocol;
public String getQuestionAbbr() {
	return questionAbbr;
}
public void setQuestionAbbr(String questionAbbr) {
	this.questionAbbr = questionAbbr;
}
public String getQuestion() {
	return question;
}
public void setQuestion(String question) {
	this.question = question;
}
public String getUserResponse() {
	return userResponse;
}
public void setUserResponse(String userResponse) {
	this.userResponse = userResponse;
}
public Boolean getIsRequired() {
	return isRequired;
}
public void setIsRequired(Boolean isRequired) {
	this.isRequired = isRequired;
}
public String getQuestionType() {
	return questionType;
}
public void setQuestionType(String questionType) {
	this.questionType = questionType;
}
public Boolean getTriggersProtocolCreation() {
	return triggersProtocolCreation;
}
public void setTriggersProtocolCreation(Boolean triggersProtocolCreation) {
	this.triggersProtocolCreation = triggersProtocolCreation;
}
public ProtocolTemplateDto getTriggeredProtocol() {
	return triggeredProtocol;
}
public void setTriggeredProtocol(ProtocolTemplateDto triggeredProtocol) {
	this.triggeredProtocol = triggeredProtocol;
}
}
