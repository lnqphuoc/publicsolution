package com.app.server.data.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SortByRequest {
    private String type;
    private String key;
}