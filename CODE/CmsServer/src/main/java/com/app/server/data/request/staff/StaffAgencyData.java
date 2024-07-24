package com.app.server.data.request.staff;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.AgencyStatus;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import com.ygame.framework.utils.ConvertUtils;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class StaffAgencyData {
    private String type = "ALL";
    private List<String> agency_ids = new ArrayList<>();
    private List<String> city_ids = new ArrayList<>();
    private List<String> region_ids = new ArrayList<>();
    private List<String> membership_ids = new ArrayList<>();
    private List<String> business_department_ids = new ArrayList<>();
    private List<String> agency_ignore_ids = new ArrayList<>();
    private StaffStatusData status;
    private int can_action_agency_lock = 1;

    public ClientResponse validate() {
        ClientResponse crValidateStatus = validateStatus();
        if (crValidateStatus.failed()) {
            return crValidateStatus;
        }
        return crValidateStatus;
    }

    private ClientResponse validateStatus() {
        if (status == null) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
        }

        if (status.getType().equals("ALL")) {
            if (!(status.getStatus() == null || status.getStatus().isEmpty())) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }
        } else if (status.getType().equals("LIST")) {

        } else {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
        }

        return ClientResponse.success(null);
    }
}