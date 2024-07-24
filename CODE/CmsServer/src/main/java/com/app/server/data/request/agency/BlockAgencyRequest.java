package com.app.server.data.request.agency;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Data;

@Data
public class BlockAgencyRequest {
    private int agency_id;
    private int block;

    public ClientResponse validate() {
        if (agency_id <= 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
        }
        return ClientResponse.success(null);
    }
}