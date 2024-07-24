package com.app.server.data.request.dept;

import com.app.server.constants.ResponseMessage;
import com.app.server.data.request.promo.PromoApplyObjectRequest;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import com.ygame.framework.utils.DateTimeUtils;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class CreateDeptSettingRequest {
    private DeptSettingApplyObjectRequest dept_apply_object;
    private Long dept_limit;
    private Long ngd_limit;
    private Integer dept_cycle;
    private Long start_date;
    private Long end_date;
    private String note = "";

    public ClientResponse validate() {
        ClientResponse clientResponse = dept_apply_object.validate();
        if (clientResponse.failed()) {
            return clientResponse;
        }

        if (start_date == null || start_date.longValue() == 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.START_DATE_NOT_EMPTY);
        }

        if (dept_limit == null
                && dept_cycle == null
                && ngd_limit == null) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.DEPT_SETTING_NOT_EMPTY);
        }

        return ClientResponse.success(null);
    }
}