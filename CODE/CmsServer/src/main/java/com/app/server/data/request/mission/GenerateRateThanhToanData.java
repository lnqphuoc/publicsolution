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
public class GenerateRateThanhToanData {
    @ApiModelProperty("0-không sử dụng/1-có sử dụng")
    public int type;
    @ApiModelProperty("0-100")
    public int rate;

    public ClientResponse validate() {
        if (!(type == 0 || type == 1)) {
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "type: " + type);
        }

        if (rate < 0) {
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "rate: " + rate);
        }
        return ResponseConstants.success;
    }
}