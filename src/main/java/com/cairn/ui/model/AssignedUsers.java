package com.cairn.ui.model;


import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignedUsers {
  private List<AssignedUser> accounts;

  public Integer getNumOfAccounts() {
    return accounts.size();
  }
}
