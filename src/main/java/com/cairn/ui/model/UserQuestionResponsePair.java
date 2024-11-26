package com.cairn.ui.model;

public class UserQuestionResponsePair {

  User user;
  QuestionResponsePairListDto responses;



  public QuestionResponsePairListDto getResponses() {
    return responses;
  }

  public void setResponses(QuestionResponsePairListDto responses) {
    this.responses = responses;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }
}
