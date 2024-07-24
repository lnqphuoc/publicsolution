package com.app.server.data.request;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ApplyFilterRequest {
    private int data_index;
    private List<ApplyFilterTypeRequest> filter_types = new ArrayList<>();
}