package com.app.server.data.request.promo;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.PromoOfferType;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Data;

@Data
public class PromoOfferProductRequest {
    private Double offer_value;
    private int product_id;
    private String product_name;
    private String product_code;
    private String offer_type = PromoOfferType.GOODS_OFFER.getKey();

    public ClientResponse validate() {
        if (product_id < 1) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_OFFER_PRODUCT_INVALID);
        }
        if (offer_value < 1) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_OFFER_PRODUCT_INVALID);
        }
        if (PromoOfferType.from(offer_type) == null) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_OFFER_TYPE_INVALID);
        }
        return ClientResponse.success(null);
    }
}