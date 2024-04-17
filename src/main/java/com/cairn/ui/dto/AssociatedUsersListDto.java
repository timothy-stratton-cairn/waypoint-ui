package com.cairn.ui.dto;

import java.util.List;

public class AssociatedUsersListDto {

  List<Long> userIds;
  private Integer numOfUsers;

  public Integer getNumOfUsers() {
    return userIds.size();
  }

  public void setNumOfUsers(Integer numOfUsers) {
    this.numOfUsers = numOfUsers;
  }

  public List<Long> getUserIds() {
    return userIds;
  }

  public void setUserIds(List<Long> userIds) {
    this.userIds = userIds;
  }
}
