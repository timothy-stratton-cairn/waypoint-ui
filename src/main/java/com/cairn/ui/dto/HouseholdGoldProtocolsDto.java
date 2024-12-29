package com.cairn.ui.dto;

import com.cairn.ui.model.Protocol;
import java.util.ArrayList;

public class HouseholdGoldProtocolsDto {

  private Long goalId;
  private String goalName;
  private String goalDescription;
  private String goalCategory;
  private ArrayList<ProtocolDTO> protocols = new ArrayList<>();


  public Long getGoalId() {
    return goalId;
  }

  public void setGoalId(Long goalId) {
    this.goalId = goalId;
  }

  public String getGoalName() {
    return goalName;
  }

  public void setGoalName(String goalName) {
    this.goalName = goalName;
  }

  public String getGoalDescription() {
    return goalDescription;
  }

  public void setGoalDescription(String goalDescription) {
    this.goalDescription = goalDescription;
  }

  public String getGoalCategory() {
    return goalCategory;
  }

  public void setGoalCategory(String goalCategory) {
    this.goalCategory = goalCategory;
  }

  public ArrayList<ProtocolDTO> getProtocols() {
    return protocols;
  }

  public void setProtocols(ArrayList<ProtocolDTO> protocols) {
    this.protocols = protocols;
  }
}
