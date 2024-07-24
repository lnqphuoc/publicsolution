package com.app.server.data.response.location;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocationResponse {
    @JsonProperty("id")
    private int id;
    @JsonProperty("label")
    private String name;
}