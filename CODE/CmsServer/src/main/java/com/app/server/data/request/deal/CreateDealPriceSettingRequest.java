package com.app.server.data.request.deal;

import com.app.server.data.dto.agency.AgencyBasicData;
import com.app.server.data.request.ApplyFilterRequest;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CreateDealPriceSettingRequest {
    private List<AgencyBasicData> agency_includes = new ArrayList<>();
    private List<AgencyBasicData> agency_ignores = new ArrayList<>();
    private List<ApplyFilterRequest> filters = new ArrayList<>();
}