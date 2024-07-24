package com.app.server.data.request.promo;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.PromoConditionType;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PromoLimitRequest {
    private int id;
    private String condition_type;
    private String offer_type;
    private int level;
    private List<PromoLimitGroupRequest> promo_limit_groups = new ArrayList<>();

    public ClientResponse validate() {
        if (PromoConditionType.from(condition_type) == null) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_CONDITION_TYPE_INVALID);
        }

        for (int iLimit = 0; iLimit < promo_limit_groups.size(); iLimit++) {
            PromoLimitGroupRequest promoLimitGroupRequest = promo_limit_groups.get(iLimit);
            ClientResponse clientResponse = promoLimitGroupRequest.validate();
            if (clientResponse.failed()) {
                clientResponse.setMessage("[Nhóm " + (iLimit + 1) + "]" + clientResponse.getMessage());
                return clientResponse;
            }
        }
        return ClientResponse.success(null);
    }

    public ClientResponse validateUpperValue(PromoLimitRequest param) {
        if (param == null) {
            return ClientResponse.success(null);
        }

        if (PromoConditionType.STEP.getKey().equals(condition_type)) {
            return ClientResponse.success(null);
        }

        for (int iGroup = 0; iGroup < param.getPromo_limit_groups().size(); iGroup++) {
            PromoLimitGroupRequest paramGroup = param.getPromo_limit_groups().get(iGroup);
            PromoLimitGroupRequest sourceGroup = promo_limit_groups.get(iGroup);

            if (!sourceGroup.upperValue(paramGroup)) {
                ClientResponse clientResponse = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_LIMIT_UPPER_INVALID);
                clientResponse.setMessage("[Nhóm " + (iGroup + 1) + "]" + clientResponse.getMessage());
                return clientResponse;
            }
        }
        return ClientResponse.success(null);
    }

    public ClientResponse validateLowerValue(PromoLimitRequest param) {
        if (param == null) {
            return ClientResponse.success(null);
        }

        if (PromoConditionType.STEP.getKey().equals(condition_type)) {
            return ClientResponse.success(null);
        }

        for (int iGroup = 0; iGroup < param.getPromo_limit_groups().size(); iGroup++) {
            PromoLimitGroupRequest paramGroup = param.getPromo_limit_groups().get(iGroup);
            PromoLimitGroupRequest sourceGroup = promo_limit_groups.get(iGroup);

            if (!sourceGroup.lowerValue(paramGroup)) {
                ClientResponse clientResponse = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_OFFER_UPPER_INVALID);
                clientResponse.setMessage("[Nhóm " + (iGroup + 1) + "]" + clientResponse.getMessage());
                return clientResponse;
            }
        }
        return ClientResponse.success(null);
    }
}