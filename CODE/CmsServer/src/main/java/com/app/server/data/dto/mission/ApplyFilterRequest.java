package com.app.server.data.dto.mission;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ApplyFilterRequest {
    private int data_index;
    private List<ApplyFilterDetailRequest> filter_types = new ArrayList<>();
}