package com.app.server.data.request.promo;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Data;

@Data
public class GetPromoByAgencyRequest {
    private int agency_id;

    public ClientResponse validate() {
        if (agency_id == 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
        }
        return ClientResponse.success(null);
    }
}