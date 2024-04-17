package com.cairn.ui.dto;

import java.util.List;

public class ProtocolStepNoteListDto {

  List<ProtocolStepNoteDto> notes;
  private Integer numOfNotes;

  public Integer getNumOfNotes() {
    return notes.size();
  }

  public List<ProtocolStepNoteDto> getNotes() {
    return notes;
  }

  public void setNotes(List<ProtocolStepNoteDto> notes) {
    this.notes = notes;
  }

  public void setNumOfNotes(Integer numOfNotes) {
    this.numOfNotes = numOfNotes;
  }
}
