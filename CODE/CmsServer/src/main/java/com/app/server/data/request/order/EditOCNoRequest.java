package com.app.server.data.request.order;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import com.ygame.framework.utils.DateTimeUtils;
import lombok.Data;

@Data
public class EditOCNoRequest {
    private int order_id;
    private String doc_no;

    public ClientResponse validate() {
        if (order_id <= 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_NOT_FOUND);
        }

        if (doc_no == null || doc_no.isEmpty()
        ) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CODE_INVALID);
        }

        return ClientResponse.success(null);
    }
}