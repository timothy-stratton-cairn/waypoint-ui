package com.cairn.ui.model;

import java.util.List;

import com.nimbusds.oauth2.sdk.Response;

public class ExpectedHomeworkResponses {
    private List<HomeworkResponse> responses;
    private int numOfResponses;

    // Getters and setters
    public List<HomeworkResponse> getResponses() {
        return responses;
    }

    public void setResponses(List<HomeworkResponse> responses) {
        this.responses = responses;
    }

    public int getNumOfResponses() {
        return numOfResponses;
    }

    public void setNumOfResponses(int numOfResponses) {
        this.numOfResponses = numOfResponses;
    }
}