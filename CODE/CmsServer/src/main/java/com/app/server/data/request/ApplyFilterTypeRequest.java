package com.app.server.data.request;

import lombok.Data;

@Data
public class ApplyFilterTypeRequest {
    private String filter_type;
    private String filter_data;
}