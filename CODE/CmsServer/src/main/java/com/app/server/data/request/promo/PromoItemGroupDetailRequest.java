package com.app.server.data.request.promo;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Data;

@Data
public class PromoItemGroupDetailRequest {
    private int item_id;
    private String note = "";
    private String item_code = "";
    private String item_name = "";
    private int item_quantity;
    private double item_price;
    private long max_offer_per_promo;
    private long max_offer_per_agency;
    private String item_type = "";
    private int category_level;

    public ClientResponse validate() {
        if (item_id <= 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PRODUCT_INVALID);
        }
        return ClientResponse.success(null);
    }
}