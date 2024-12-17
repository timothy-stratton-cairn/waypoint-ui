package com.cairn.ui.dto;

import com.cairn.ui.model.GoalCategory;

public class GoalTemplateDto {

  private final Long id;
  private final String name;
  private final String description;
  private final GoalCategoryDto category;

  // All-args constructor
  public GoalTemplateDto(Long id, String name, String description, GoalCategoryDto category) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.category = category;
  }

  // Getters only (no setters to maintain immutability)
  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public GoalCategoryDto getCategory() {
    return category;
  }
}

