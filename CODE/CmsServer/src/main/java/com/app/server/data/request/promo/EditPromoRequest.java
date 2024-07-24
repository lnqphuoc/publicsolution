package com.app.server.data.request.promo;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Data;

@Data
public class EditPromoRequest extends CreatePromoRequest {
    private String note;

    public ClientResponse validate() {
        if (promo_info == null) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_INFO_EMPTY);
        }

        if (promo_info.getId() < 1) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_INFO_EMPTY);
        }

        ClientResponse clientResponse = promo_info.validate();
        if (clientResponse.failed()) {
            return clientResponse;
        }

        for (PromoItemGroupRequest promoItemGroupRequest : promo_item_groups) {
            clientResponse = promoItemGroupRequest.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }
        }

        for (PromoLimitRequest promoLimitRequest : promo_limits) {
            clientResponse = promoLimitRequest.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }
        }

        ClientResponse crRepeatValidate = repeat_data.validate();
        if (crRepeatValidate.failed()) {
            return crRepeatValidate;
        }
        return ClientResponse.success(null);
    }
}