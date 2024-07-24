package com.app.server.data.dto.notify;

import com.app.server.config.ConfigInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotifyPopupData {
    private String key;
    private String firebase_token_data;
    private String type;
    private String title;
    private String body;
    private String data;
    private String image;
    private String env;
    private long id;
}