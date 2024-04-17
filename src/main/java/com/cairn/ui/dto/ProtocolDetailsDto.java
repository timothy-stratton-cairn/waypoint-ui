package com.cairn.ui.dto;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProtocolDetailsDto {

  private Long id;
  private String name;
  private String description;
  private String goal;
  private String goalProgress;
  private Boolean needsAttention;
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  private LocalDateTime lastStatusUpdateTimestamp;

  private BigDecimal completionPercentage;
  private ProtocolCommentListDto protocolComments;
  private AssociatedUsersListDto associatedUsers;
  private AssociatedStepsListDto associatedSteps;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

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

  public String getGoal() {
    return goal;
  }

  public void setGoal(String goal) {
    this.goal = goal;
  }

  public String getGoalProgress() {
    return goalProgress;
  }

  public void setGoalProgress(String goalProgress) {
    this.goalProgress = goalProgress;
  }

  public Boolean getNeedsAttention() {
    return needsAttention;
  }

  public void setNeedsAttention(Boolean needsAttention) {
    this.needsAttention = needsAttention;
  }

  public LocalDateTime getLastStatusUpdateTimestamp() {
    return lastStatusUpdateTimestamp;
  }

  public void setLastStatusUpdateTimestamp(LocalDateTime lastStatusUpdateTimestamp) {
    this.lastStatusUpdateTimestamp = lastStatusUpdateTimestamp;
  }

  public BigDecimal getCompletionPercentage() {
    return completionPercentage;
  }

  public void setCompletionPercentage(BigDecimal completionPercentage) {
    this.completionPercentage = completionPercentage;
  }

  public ProtocolCommentListDto getProtocolComments() {
    return protocolComments;
  }

  public void setProtocolComments(ProtocolCommentListDto protocolComments) {
    this.protocolComments = protocolComments;
  }

  public AssociatedUsersListDto getAssociatedUsers() {
    return associatedUsers;
  }

  public void setAssociatedUsers(AssociatedUsersListDto associatedUsers) {
    this.associatedUsers = associatedUsers;
  }

  public AssociatedStepsListDto getAssociatedSteps() {
    return associatedSteps;
  }

  public void setAssociatedSteps(AssociatedStepsListDto associatedSteps) {
    this.associatedSteps = associatedSteps;
  }
}
