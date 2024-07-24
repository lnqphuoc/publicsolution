package com.app.server.data.request.product;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Data;

@Data
public class ProductHotRequest {
    private int id;
    private String hot_label;

    public ClientResponse validate() {
        if (id <= 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PRODUCT_INVALID);
        }
        return ClientResponse.success(null);
    }
}