package com.app.server.data.dto.location;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Location {
    private int id;
    @JsonProperty("label")
    private String name;
    private int status;
}