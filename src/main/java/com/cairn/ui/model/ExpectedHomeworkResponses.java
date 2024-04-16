package com.cairn.ui.model;

import java.util.List;

import com.nimbusds.oauth2.sdk.Response;

public class ExpectedHomeworkResponses {
    private List<Response> responses;
    private int numOfResponses;

    // Getters and setters
    public List<Response> getResponses() {
        return responses;
    }

    public void setResponses(List<Response> responses) {
        this.responses = responses;
    }

    public int getNumOfResponses() {
        return numOfResponses;
    }

    public void setNumOfResponses(int numOfResponses) {
        this.numOfResponses = numOfResponses;
    }
}