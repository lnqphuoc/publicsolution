package com.app.server.data.request.order;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApproveRequestOrderRequest {
    private int id;
    private String note;

    public ClientResponse validRequest() {
        if (id < 1) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
        }

        return ClientResponse.success(null);
    }
}