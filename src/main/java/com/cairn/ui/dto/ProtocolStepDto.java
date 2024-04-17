package com.cairn.ui.dto;

public class ProtocolStepDto {

  private Long id;
  private String name;
  private String description;
  private String status;
  private String category;
  private ProtocolStepNoteListDto stepNotes;

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

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public ProtocolStepNoteListDto getStepNotes() {
    return stepNotes;
  }

  public void setStepNotes(ProtocolStepNoteListDto stepNotes) {
    this.stepNotes = stepNotes;
  }
}
