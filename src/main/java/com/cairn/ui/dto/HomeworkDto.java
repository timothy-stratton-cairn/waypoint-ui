package com.cairn.ui.dto;

import com.cairn.waypoint.dashboard.endpoints.homework.dto.HomeworkQuestionListDto;
import com.cairn.waypoint.dashboard.endpoints.homework.dto.HomeworkUserListDto;

public class HomeworkDto {

  private String name;
  private String description;
  private HomeworkQuestionListDto homeworkQuestions;
  private Long protocolStepId;
  private HomeworkUserListDto assignedUsers;
  private Long parentProtocolId;
  private Long parentProtocolStepId;
  
  
public String getName() {
	return name;
}
public void setName(String name) {
	this.name = name;
}
public String getDescription() {
	return description;
}
public void setDescription(String description) {
	this.description = description;
}
public HomeworkQuestionListDto getHomeworkQuestions() {
	return homeworkQuestions;
}
public void setHomeworkQuestions(HomeworkQuestionListDto homeworkQuestions) {
	this.homeworkQuestions = homeworkQuestions;
}
public Long getProtocolStepId() {
	return protocolStepId;
}
public void setProtocolStepId(Long protocolStepId) {
	this.protocolStepId = protocolStepId;
}
public HomeworkUserListDto getAssignedUsers() {
	return assignedUsers;
}
public void setAssignedUsers(HomeworkUserListDto assignedUsers) {
	this.assignedUsers = assignedUsers;
}
public Long getParentProtocolId() {
	return parentProtocolId;
}
public void setParentProtocolId(Long parentProtocolId) {
	this.parentProtocolId = parentProtocolId;
}
public Long getParentProtocolStepId() {
	return parentProtocolStepId;
}
public void setParentProtocolStepId(Long parentProtocolStepId) {
	this.parentProtocolStepId = parentProtocolStepId;
}


}
