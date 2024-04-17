package com.cairn.ui.dto;

import java.util.List;


public class ProtocolCommentListDto {

  List<ProtocolCommentDto> comments;
  private Integer numOfComments;

  public Integer getNumOfComments() {
    return comments.size();
  }

  public List<ProtocolCommentDto> getComments() {
    return comments;
  }

  public void setComments(List<ProtocolCommentDto> comments) {
    this.comments = comments;
  }

  public void setNumOfComments(Integer numOfComments) {
    this.numOfComments = numOfComments;
  }
}
