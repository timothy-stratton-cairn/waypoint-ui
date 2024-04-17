package com.cairn.ui.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;

public class ProtocolStepNoteDto {

  @JsonSerialize(using = LocalDateTimeSerializer.class)
  private LocalDateTime takenAt;
  private String takenBy;
  private String note;

  public LocalDateTime getTakenAt() {
    return takenAt;
  }

  public void setTakenAt(LocalDateTime takenAt) {
    this.takenAt = takenAt;
  }

  public String getTakenBy() {
    return takenBy;
  }

  public void setTakenBy(String takenBy) {
    this.takenBy = takenBy;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }
}
