package com.cairn.ui.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GoalTemplateDto {

  private final Long id;
  private final String name;
  private final String description;
  private final GoalCategoryDto category;

  @JsonCreator
  public GoalTemplateDto(
      @JsonProperty("id") Long id,
      @JsonProperty("name") String name,
      @JsonProperty("description") String description,
      @JsonProperty("category") GoalCategoryDto category) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.category = category;
  }

  // Getters
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
