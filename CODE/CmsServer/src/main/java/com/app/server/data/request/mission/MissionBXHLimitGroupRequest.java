package com.app.server.data.request.mission;

import com.app.server.constants.ResponseMessage;
import com.app.server.data.request.promo.PromoOfferRequest;
import com.app.server.enums.ItemGroupType;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Data;
import org.json.simple.JSONObject;

@Data
public class MissionBXHLimitGroupRequest {
    private Long from_value;
    private Long end_value;
    private int data_index;
    private MissionBXHOfferRequest offer;

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
}