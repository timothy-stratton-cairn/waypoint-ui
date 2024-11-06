package com.cairn.ui.model;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


public class SimplifiedHomeworkResponseDto {
    private Long id;
    private String updated;
    private String response;

    @JsonCreator
    public SimplifiedHomeworkResponseDto(
            @JsonProperty("id") Long id,
            @JsonProperty("updated") String updated,
            @JsonProperty("response") String response) {
        this.id = id;
        this.updated = updated;
        this.response = response;
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

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}