package com.cairn.ui.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class QuestionResponsePairDto {
    private SimplifiedProtocolDto protocol;
    private SimplifiedHomeworkQuestionDto question;
    private SimplifiedHomeworkResponseDto response;

    @JsonCreator
    public QuestionResponsePairDto(
            @JsonProperty("protocol") SimplifiedProtocolDto protocol,
            @JsonProperty("question") SimplifiedHomeworkQuestionDto question,
            @JsonProperty("response") SimplifiedHomeworkResponseDto response) {
        this.protocol = protocol;
        this.question = question;
        this.response = response;
    }
    public SimplifiedProtocolDto getProtocol() {
        return protocol;
    }

    public void setProtocol(SimplifiedProtocolDto protocol) {
        this.protocol = protocol;
    }

    public SimplifiedHomeworkQuestionDto getQuestion() {
        return question;
    }

    public void setQuestion(SimplifiedHomeworkQuestionDto question) {
        this.question = question;
    }

    public SimplifiedHomeworkResponseDto getResponse() {
        return response;
    }

    public void setResponse(SimplifiedHomeworkResponseDto response) {
        this.response = response;
    }
}
