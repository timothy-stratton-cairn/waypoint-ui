package com.cairn.ui.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProtocolDTO {
  private Long id;
  private String name;
  private String description;
  private String goal;
  private String goalProgress;
  private Date createdAt;
  private String dueBy;
  private Date completedOn;
  private boolean needsAttention;
  private Long protocolTemplateId;
  private String status;
  private Date lastStatusUpdateTimestamp;
  private String completionPercentage;

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

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public String getDueBy() {
    return dueBy;
  }

  public void setDueBy(String dueBy) {
    this.dueBy = dueBy;
  }

  public Date getCompletedOn() {
    return completedOn;
  }

  public void setCompletedOn(Date completedOn) {
    this.completedOn = completedOn;
  }

  public boolean isNeedsAttention() {
    return needsAttention;
  }

  public void setNeedsAttention(boolean needsAttention) {
    this.needsAttention = needsAttention;
  }

  public Long getProtocolTemplateId() {
    return protocolTemplateId;
  }

  public void setProtocolTemplateId(Long protocolTemplateId) {
    this.protocolTemplateId = protocolTemplateId;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Date getLastStatusUpdateTimestamp() {
    return lastStatusUpdateTimestamp;
  }

  public void setLastStatusUpdateTimestamp(Date lastStatusUpdateTimestamp) {
    this.lastStatusUpdateTimestamp = lastStatusUpdateTimestamp;
  }

  public String getCompletionPercentage() {
    return completionPercentage;
  }

  public void setCompletionPercentage(String completionPercentage) {
    this.completionPercentage = completionPercentage;
  }
}
