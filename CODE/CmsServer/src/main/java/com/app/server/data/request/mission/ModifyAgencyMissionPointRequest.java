package com.app.server.data.request.mission;

import com.app.server.constants.ResponseConstants;
import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModifyAgencyMissionPointRequest {
    @ApiModelProperty("Id đại lý")
    private int agency_id;
    @ApiModelProperty("Giá trị")
    private int point;
    @ApiModelProperty("Loại: Tăng - INCREASE / Giảm - DECREASE")
    private String type;
    @ApiModelProperty("Ghi chú")
    private String note = "";

    public ClientResponse validate() {
        if (agency_id <= 0) {
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "Id đại lý");
        }

        if (type == null || type.isEmpty()) {
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "Loại tăng giảm");
        }

        if (point <= 0) {
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "Giá trị");
        }

        if (note.isEmpty()) {
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "Ghi chú");
        }

        return ResponseConstants.success;
    }
}