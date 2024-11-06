package com.cairn.ui.model;

import java.util.List;

public class QuestionResponsePairListDto {
    private List<QuestionResponsePairDto> questions;
    private int numberOfPairs;

    // Getters and setters
    public List<QuestionResponsePairDto> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionResponsePairDto> questions) {
        this.questions = questions;
    }

    public int getNumberOfPairs() {
        return numberOfPairs;
    }

    public void setNumberOfPairs(int numberOfPairs) {
        this.numberOfPairs = numberOfPairs;
    }
}