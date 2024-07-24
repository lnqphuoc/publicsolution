package com.app.server.data.request.product;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Data;

import java.util.List;

@Data
public class ActiveProductVisibilitySettingRequest {
    private List<Integer> ids;
    private String note;

    public ClientResponse validate() {
        if (ids.isEmpty()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
        }
        return ClientResponse.success(null);
    }
}