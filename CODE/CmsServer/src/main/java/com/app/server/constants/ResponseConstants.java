package com.app.server.constants;

import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;

public class ResponseConstants {
    public static ClientResponse success = ClientResponse.success(null);
    public static ClientResponse failed = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
}