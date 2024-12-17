package com.cairn.ui.dto;
import com.cairn.ui.model.GoalCategory;
import com.cairn.ui.model.ProtocolTemplate;
import java.util.List;

public class HouseholdGoalDto {
  private Long id;
  private String name;
  private String description;
  private GoalCategory category;
  private List<ProtocolTemplate> protocolTemplates;

}
