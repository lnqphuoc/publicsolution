package com.app.server.data.request;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class CancelRequest {
    private int id;
    private String note = "";

    public ClientResponse validate() {
        if (id < 1) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
        }
        if (StringUtils.isBlank(note)) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.NOTE_NOT_EMPTY);
        }

        return ClientResponse.success(null);
    }
}