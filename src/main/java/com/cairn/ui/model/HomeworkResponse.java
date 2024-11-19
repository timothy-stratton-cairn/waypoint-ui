package com.cairn.ui.model;

public class HomeworkResponse {
    private String response;
    private String tooltip;
    private int questionId;
    private int categoryId;
    private int userId;
   // private int protocolId;

    // Getters and setters
    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public int getCategoryId() {
      return categoryId;
    }

    public void setCategoryId(int categoryId) {
      this.categoryId = categoryId;
    }

    public int getUserId() {return userId;}

    public void setUserId(int userId) {this.userId = userId;}

    public int getQuestionId() {
      return questionId;
    }

    public void setQuestionId(int questionId) {
      this.questionId = questionId;
    }
}