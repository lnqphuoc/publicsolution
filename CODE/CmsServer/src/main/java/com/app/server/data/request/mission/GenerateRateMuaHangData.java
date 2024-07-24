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
public class GenerateRateMuaHangData {
    @ApiModelProperty("0-ngẫu nhiên/1-theo thứ tự")
    private int type;
    @ApiModelProperty("Tỷ lệ nhiệm vụ ưu tiên")
    private MisssionPriorityRateData nhiem_vu_uu_tien;

    public ClientResponse validate() {
        if (!(type == 0 || type == 1)) {
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "type: " + type);
        }

        if (nhiem_vu_uu_tien == null) {
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "nhiem_vu_uu_tien: " + nhiem_vu_uu_tien);
        }

        ClientResponse crNhiemVuUuTien = nhiem_vu_uu_tien.validate();
        if (crNhiemVuUuTien.failed()) {
            return crNhiemVuUuTien;
        }

        return ResponseConstants.success;
    }
}