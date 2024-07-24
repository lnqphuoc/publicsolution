package com.app.server.data.request.agency;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SetStaffManageAgencyRequest {
    private int agency_id;
    private int staff_id;
    @ApiModelProperty("Loại: 1 - Phụ trách, 2 - Hỗ trợ")
    private int type = 1;
    @ApiModelProperty("Gỡ: false/true")
    private boolean remove = false;

    public ClientResponse validate() {
        if (agency_id <= 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.INFO_INVALID);
        }
        if (staff_id <= 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.INFO_INVALID);
        }
        if (type <= 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.INFO_INVALID);
        }
        return ClientResponse.success(null);
    }
}