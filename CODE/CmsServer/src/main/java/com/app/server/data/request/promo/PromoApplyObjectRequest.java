package com.app.server.data.request.promo;


import com.app.server.data.dto.agency.AgencyBasicData;
import com.app.server.response.ClientResponse;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PromoApplyObjectRequest {
    private List<AgencyBasicData> promo_agency_includes = new ArrayList<>();
    private List<AgencyBasicData> promo_agency_ignores = new ArrayList<>();
    private List<PromoApplyFilterRequest> promo_filters = new ArrayList<>();
    private List<PromoApplyFilterRequest> promo_sufficient_conditions = new ArrayList<>();

    public ClientResponse validate() {
        return ClientResponse.success(null);
    }
}