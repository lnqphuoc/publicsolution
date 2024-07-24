package com.app.server.data.request.product;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Data;

@Data
public class ProductPriceTimerRequest {
    private int id;
    private String full_name = "";
    private long price;
    private String note = "";
    private String error = "";

    public ClientResponse validate() {
        if (id <= 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PRODUCT_INVALID);
        }
        if (price <= 0 && price != -1) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PRICE_INVALID);
        }
        return ClientResponse.success(null);
    }
}