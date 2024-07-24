package com.app.server.data.response;

import com.app.server.data.response.agency.AgencyResponse;
import lombok.Data;

import java.util.List;

@Data
public class FilterAgencyDataInListResponse {
    private List<AgencyResponse> agencies;
}
