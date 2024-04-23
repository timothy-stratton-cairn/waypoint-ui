package com.cairn.ui.dto;



import java.util.List;

public class HomeworkUserListDto {

  private List<Long> userIds;
  private Integer numOfUsers;

  public Integer getNumOfUsers() {
    return userIds.size();
  }

public List<Long> getUserIds() {
	return userIds;
}

public void setUserIds(List<Long> userIds) {
	this.userIds = userIds;
}

public void setNumOfUsers(Integer numOfUsers) {
	this.numOfUsers = numOfUsers;
}
}
