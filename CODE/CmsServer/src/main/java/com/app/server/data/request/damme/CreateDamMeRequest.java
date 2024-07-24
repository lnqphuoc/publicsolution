package com.app.server.data.request.damme;

import com.app.server.constants.ResponseMessage;
import com.app.server.data.request.RepeatDataRequest;
import com.app.server.data.request.promo.*;
import com.app.server.enums.PromoConditionType;
import com.app.server.enums.PromoType;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Data;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

@Data
public class CreateDamMeRequest {
    protected PromoInfoRequest promo_info;
    protected PromoApplyObjectRequest promo_apply_object;
    protected List<PromoItemGroupRequest> promo_item_groups = new ArrayList<>();
    protected List<PromoItemIgnoreRequest> promo_item_ignores = new ArrayList<>();
    protected List<PromoLimitRequest> promo_limits = new ArrayList<>();
    protected RepeatDataRequest repeat_data = new RepeatDataRequest();
    protected String repeat_data_info;
    protected PromoTimeRequest order_date_data;
    protected String order_date_data_info;
    protected PromoTimeRequest payment_date_data;
    protected String payment_date_data_info;
    protected PromoTimeRequest reward_date_data;
    protected String reward_date_data_info;
    protected PromoStructureRequest promo_structure = new PromoStructureRequest();
    protected JSONObject promo_structure_info;
    protected PromoTimeRequest confirm_result_date_data;
    protected String confirm_result_date_data_info;

    public ClientResponse validate() {
        /**
         * Thông tin ưu đãi rỗng
         */
        if (promo_info == null) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_INFO_EMPTY);
        }

        /**
         * Thông tin ưu đãi không hợp lệ
         */
        ClientResponse clientResponse = promo_info.validate();
        if (clientResponse.failed()) {
            return clientResponse;
        }

        /**
         * Thông tin đối tượng áp dụng
         */
        if (promo_apply_object != null) {
            clientResponse = promo_apply_object.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }
        }

        if (promo_info.getCondition_type().equals(PromoConditionType.PRODUCT_QUANTITY.getKey()) ||
                promo_info.getCondition_type().equals(PromoConditionType.PRODUCT_PRICE.getKey())) {
            if (promo_item_groups.isEmpty()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PRODUCT_EMPTY);
            }
        }

        /**
         * Nhóm sản phẩm áp dụng không hợp lệ
         */
        for (int iItemGroup = 0; iItemGroup < promo_item_groups.size(); iItemGroup++) {
            PromoItemGroupRequest promoItemGroupRequest = promo_item_groups.get(iItemGroup);
            clientResponse = promoItemGroupRequest.validate();
            if (clientResponse.failed()) {
                clientResponse.setMessage("[Nhóm sản phẩm áp dụng " + (iItemGroup + 1) + "]" + clientResponse.getMessage());
                return clientResponse;
            }
        }

        if (promo_limits.isEmpty()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_LIMIT_EMPTY);
        }

        for (int iLimit = 0; iLimit < promo_limits.size(); iLimit++) {
            PromoLimitRequest promoLimitRequest = promo_limits.get(iLimit);
            clientResponse = promoLimitRequest.validate();
            if (clientResponse.failed()) {
                clientResponse.setMessage("[Hạn mức " + (iLimit + 1) + "]" + clientResponse.getMessage());
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