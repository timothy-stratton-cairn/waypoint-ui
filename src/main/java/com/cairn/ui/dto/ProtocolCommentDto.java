package com.cairn.ui.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;

public class ProtocolCommentDto {

  @JsonSerialize(using = LocalDateTimeSerializer.class)
  private LocalDateTime takenAt;
  private String takenBy;
  private String comment;

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String getTakenBy() {
    return takenBy;
  }

  public void setTakenBy(String takenBy) {
    this.takenBy = takenBy;
  }

  public LocalDateTime getTakenAt() {
    return takenAt;
  }

  public void setTakenAt(LocalDateTime takenAt) {
    this.takenAt = takenAt;
  }
}
