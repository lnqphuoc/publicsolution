package com.app.server.data.request.bravo;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BravoResponse {
    private int status;
    private String message;
    private Object data;

    public BravoResponse() {
        status = ResponseStatus.FAIL.getValue();
        message = ResponseMessage.FAIL.getValue();
        data = "";
    }

    public BravoResponse(ResponseStatus status, String message, Object data) {
        this.status = status.getValue();
        this.message = message;
        if (data == null)
            this.data = "";
        else
            this.data = data;
    }

    public boolean success() {
        return this.status == ResponseStatus.SUCCESS.getValue();
    }

    public void fail() {
        this.status = ResponseStatus.FAIL.getValue();
        this.message = ResponseMessage.FAIL.getValue();
    }
}