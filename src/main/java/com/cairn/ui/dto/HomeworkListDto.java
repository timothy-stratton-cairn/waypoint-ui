package com.cairn.ui.dto;


import java.util.List;

public class HomeworkListDto {

  private List<HomeworkDto> homeworks;
  private Integer numOfHomeworks;

  public Integer getNumOfHomeworks() {
    return homeworks.size();
  }

public List<HomeworkDto> getHomeworks() {
	return homeworks;
}

public void setHomeworks(List<HomeworkDto> homeworks) {
	this.homeworks = homeworks;
}

public void setNumOfHomeworks(Integer numOfHomeworks) {
	this.numOfHomeworks = numOfHomeworks;
}

}
