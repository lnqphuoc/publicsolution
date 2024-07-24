package com.app.server.data.dto.mission;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfigRequest {
    private String type;
    private String data;
}