package com.app.server.data.request.product;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Data;

@Data
public class ProductPriceRequest {
    private String code = "";
    private String full_name = "";
    private long price;
    private String note = "";
    private String error = "";

    public ClientResponse validate() {
        if (code.isEmpty()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
        }
        if (price <= 0 && price != -1) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PRICE_INVALID);
        }
        return ClientResponse.success(null);
    }
}