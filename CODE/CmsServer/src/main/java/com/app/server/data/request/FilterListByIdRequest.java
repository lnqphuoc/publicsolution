package com.app.server.data.request;

import com.app.server.constants.ResponseMessage;
import com.app.server.data.request.FilterRequest;
import com.app.server.data.request.SortByRequest;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FilterListByIdRequest extends FilterListRequest {
    private int allow = 0;


    public ClientResponse validate() {
        if (isLimit == 1 && page < 1) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
        }
        if (isLimit != 0 && isLimit != 1) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
        }

        return ClientResponse.success(null);
    }
}