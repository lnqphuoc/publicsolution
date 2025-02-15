package com.app.server.data.request.order;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class CancelOrderByAppRequest {
    private int id;

    public ClientResponse validate() {
        if (id < 1) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
        }
        return ClientResponse.success(null);
    }
}