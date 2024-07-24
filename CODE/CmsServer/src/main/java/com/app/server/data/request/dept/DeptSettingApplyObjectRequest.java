package com.app.server.data.request.dept;

import com.app.server.constants.ResponseMessage;
import com.app.server.data.dto.agency.AgencyBasicData;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DeptSettingApplyObjectRequest {
    private List<AgencyBasicData> dept_agency_includes = new ArrayList<>();
    private List<AgencyBasicData> dept_agency_ignores = new ArrayList<>();

    public ClientResponse validate() {
        if (dept_agency_includes == null || dept_agency_includes.isEmpty()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_EMPTY);
        }

        return ClientResponse.success(null);
    }
}