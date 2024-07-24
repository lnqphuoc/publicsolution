package com.app.server.data.dto.mission;


import com.app.server.data.dto.agency.AgencyBasicData;
import com.app.server.response.ClientResponse;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ApplyObjectRequest {
    @ApiModelProperty("Chỉ định đại lý")
    private List<AgencyBasicData> agency_includes = new ArrayList<>();
    @ApiModelProperty("Loại trừ đại lý")
    private List<AgencyBasicData> agency_ignores = new ArrayList<>();
    @ApiModelProperty("Bộ lọc")
    private List<ApplyFilterRequest> filters = new ArrayList<>();

    public ClientResponse validate() {
        return ClientResponse.success(null);
    }
}