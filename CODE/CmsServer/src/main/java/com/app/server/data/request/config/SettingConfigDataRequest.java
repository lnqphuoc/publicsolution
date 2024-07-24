package com.app.server.data.request.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SettingConfigDataRequest {
    private String type;
    private String value;
}