package com.app.server.data.response.agency;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgencyStatusResponse {

    @JsonProperty("id")
    private int value;
    @JsonProperty("label")
    private String name;

    public AgencyStatusResponse(int value, String name) {
        this.value = value;
        this.name = name;
    }
}