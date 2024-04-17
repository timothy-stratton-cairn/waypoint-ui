package com.cairn.ui.dto;

import java.util.List;

public class AssociatedStepsListDto {

  List<ProtocolStepDto> steps;
  private Integer numOfSteps;

  public Integer getNumOfSteps() {
    return steps.size();
  }

  public List<ProtocolStepDto> getSteps() {
    return steps;
  }

  public void setSteps(List<ProtocolStepDto> steps) {
    this.steps = steps;
  }

  public void setNumOfSteps(Integer numOfSteps) {
    this.numOfSteps = numOfSteps;
  }
}
