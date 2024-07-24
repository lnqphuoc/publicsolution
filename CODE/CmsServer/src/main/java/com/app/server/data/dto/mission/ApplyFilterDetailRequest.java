package com.app.server.data.dto.mission;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplyFilterDetailRequest {
    private String filter_type;
    private String filter_data;
}