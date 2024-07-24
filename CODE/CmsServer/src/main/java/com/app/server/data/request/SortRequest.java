package com.app.server.data.request;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SortRequest extends BasicRequest {
    private int priority;

    public ClientResponse validate() {
        if (priority < 1) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PRIORITY_INVALID);
        }

        return ClientResponse.success(null);
    }
}