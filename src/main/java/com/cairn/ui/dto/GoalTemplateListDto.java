package com.cairn.ui.dto;

import java.util.List;

public class GoalTemplateListDto {

  private List<GoalTemplateDto> templates;
  private int totalTemplate;

  public List<GoalTemplateDto> getTemplates() {
    return templates;
  }

  public void setTemplates(List<GoalTemplateDto> templates) {
    this.templates = templates;
  }

  public int getTotalTemplate() {
    return totalTemplate;
  }

  public void setTotalTemplate(int totalTemplate) {
    this.totalTemplate = totalTemplate;
  }
}
