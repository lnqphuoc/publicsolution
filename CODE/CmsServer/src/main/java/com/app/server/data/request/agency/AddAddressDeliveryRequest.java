package com.app.server.data.request.agency;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class AddAddressDeliveryRequest {
    private int agency_id;
    private String full_name = "";
    private String phone = "";
    private String address = "";
    private String truck_number = "";
    private int is_default;

    public ClientResponse validate() {
        if (this.agency_id == 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
        }
        if (StringUtils.isBlank(this.full_name)) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FULL_NAME_INVALID);
        }
        if (StringUtils.isBlank(this.phone)) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PHONE_INVALID);
        }
        if (StringUtils.isBlank(this.address)) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ADDRESS_INVALID);
        }
        return ClientResponse.success(null);
    }
}