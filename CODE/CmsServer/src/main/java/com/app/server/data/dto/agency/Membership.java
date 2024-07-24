package com.app.server.data.dto.agency;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Membership {
    private int id;
    @JsonProperty("label")
    private String name;
    private String code;
    private long money_require;
}