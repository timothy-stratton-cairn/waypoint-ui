package com.cairn.ui.model;

import java.util.ArrayList;

public class HouseholdGoalTemplateProtocols {
  private Long id;
  private String name;
  private String description;
  private GoalCategory category;
  private ArrayList<Protocol> protocols;
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

  public GoalCategory getCategory() {
    return category;
  }

  public void setCategory(GoalCategory category) {
    this.category = category;
  }

  public ArrayList<Protocol> getProtocols() {
    return protocols;
  }

  public void setProtocols(ArrayList<Protocol> protocols) {
    this.protocols = protocols;
  }
}