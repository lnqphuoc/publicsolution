package com.app.server.data.request.deal;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class ResponseDealPriceRequest {
    private int id;
    private String note = "";
    private Long request_delivery_date_millisecond;
    private int deposit_percent;
    private int payment_duration;
    private int complete_payment_duration;
    private int product_total_quantity;
    private long product_price;
    private String product_full_name = "";
    private String product_description = "";
    private List product_images = new ArrayList();

    public ClientResponse validate() {
        if (id <= 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.INFO_INVALID);
        }
        if (deposit_percent <= 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.INFO_INVALID);
        }
        if (payment_duration <= 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.INFO_INVALID);
        }
        if (complete_payment_duration <= 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.INFO_INVALID);
        }
        if (product_total_quantity <= 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.INFO_INVALID);
        }
        if (product_price <= 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.INFO_INVALID);
        }
        return ClientResponse.success(null);
    }
}