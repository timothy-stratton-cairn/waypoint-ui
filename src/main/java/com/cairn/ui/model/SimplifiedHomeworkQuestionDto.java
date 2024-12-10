package com.cairn.ui.model;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


public class SimplifiedHomeworkQuestionDto {
    private Long id;
    private String updated;
    private String question;
    private String questionType;
    private String label;
    private Long categoryId;

    @JsonCreator
    public SimplifiedHomeworkQuestionDto(
            @JsonProperty("id") Long id,
            @JsonProperty("updated") String updated,
            @JsonProperty("question") String question,
            @JsonProperty("questionType") String questionType,
            @JsonProperty("label") String label,
            @JsonProperty("categoryId") Long categoryId) {
        this.id = id;
        this.updated = updated;
        this.question = question;
        this.questionType = questionType;
        this.label = label;
        this.categoryId = categoryId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

}

