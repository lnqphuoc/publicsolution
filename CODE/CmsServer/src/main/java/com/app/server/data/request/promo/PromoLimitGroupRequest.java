package com.app.server.data.request.promo;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ItemGroupType;
import com.app.server.enums.PromoOfferType;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Data;
import org.json.simple.JSONObject;

@Data
public class PromoLimitGroupRequest {
    private int id;
    private Long from_value;
    private Long end_value;
    private int data_index;
    private PromoOfferRequest offer;
    private String type = ItemGroupType.GROUP.getCode();
    private JSONObject group_info = new JSONObject();

    public ClientResponse validate() {
        if (from_value == 0 || (end_value != 0 &&
                end_value < from_value)) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_LIMIT_FROM_END_INVALID);
        }

        if (offer == null) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_OFFER_NOT_EMPTY);
        }

        ClientResponse clientResponse = offer.validate();
        if (clientResponse.failed()) {
            return clientResponse;
        }
        return ClientResponse.success(null);
    }

    public boolean upperValue(PromoLimitGroupRequest paramGroup) {
        if (paramGroup.getEnd_value() != 0 && from_value < paramGroup.getEnd_value()) {
            return false;
        } else if (paramGroup.getEnd_value() == 0 && from_value < paramGroup.getFrom_value()) {
            return false;
        } else if (offer.getOffer_type().equals(paramGroup.getOffer().getOffer_type())
                && paramGroup.getOffer().getOffer_value() != 0
                && offer.getOffer_value() != 0
                && !(offer.getOffer_value() > paramGroup.getOffer().getOffer_value())) {
            return false;
        }
        return true;
    }

    public boolean lowerValue(PromoLimitGroupRequest paramGroup) {
        if (offer.getOffer_type().equals(paramGroup.getOffer().getOffer_type())
                && paramGroup.getOffer().getOffer_value() != 0
                && offer.getOffer_value() != 0
                && !(offer.getOffer_value() > paramGroup.getOffer().getOffer_value())) {
            return false;
        }
        return true;
    }
}