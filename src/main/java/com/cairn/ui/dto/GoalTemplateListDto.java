package com.cairn.ui.dto;

import java.util.List;

public class GoalTemplateListDto {

  private List<GoalTemplateDto> goalTemplates;
  private int goalTemplatesCount;


  public List<GoalTemplateDto> getGoalTemplates() {
    return goalTemplates;
  }
  public void setGoalTemplates(List<GoalTemplateDto> goalTemplates) {
    this.goalTemplates = goalTemplates;
  }
  public void setGoalTemplatesCount(int goalTemplatesCount) {
    this.goalTemplatesCount = goalTemplates.size();
  }
  public int getGoalTemplatesCount() {
    return goalTemplatesCount;
  }
}
