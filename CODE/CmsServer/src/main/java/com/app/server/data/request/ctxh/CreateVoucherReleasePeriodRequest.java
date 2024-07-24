package com.app.server.data.request.ctxh;

import com.app.server.config.ConfigInfo;
import com.app.server.constants.ResponseMessage;
import com.app.server.enums.PromoOfferType;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import com.app.server.utils.JsonUtils;
import com.google.gson.reflect.TypeToken;
import com.ygame.framework.utils.ConvertUtils;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;

import java.util.List;

@Getter
@Setter
public class CreateVoucherReleasePeriodRequest {
    @ApiModelProperty("Mã")
    private String code;
    @ApiModelProperty("Tên")
    private String name;
    @ApiModelProperty("Hình ảnh")
    private String image;
    @ApiModelProperty("loại ưu đãi: giảm tiền-MONEY_DISCOUNT, quà tặng-GIFT_OFFER")
    private String offer_type;
    @ApiModelProperty("Danh sách điều kiện: [{\"limit_value\":\"100000\",\"offer_value\":\"500000\"}]")
    private String limit_data = "[]";
    @ApiModelProperty("Danh sách quà tặng: [{\"item_id\":\"1\",\"item_quantity\":\"1\"}]")
    private String item_data = "[]";
    @ApiModelProperty("Phần trăm áp dụng tối đa trên đơn hàng [1-100]")
    private int max_percent_per_order;

    public ClientResponse validate() {
        if (code == null || code.isEmpty()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CODE_INVALID);
        }
        if (name == null || name.isEmpty()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.NAME_INVALID);
        }
        if (offer_type == null || offer_type.isEmpty()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_OFFER_TYPE_INVALID);
        }
        if (PromoOfferType.MONEY_DISCOUNT.getKey().equals(offer_type) &&
                (limit_data == null || limit_data.isEmpty() || limit_data.equals("[]") || !this.validateLimitData())) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_CONDITION_TYPE_INVALID);
        }
        if (max_percent_per_order < 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PERCENT_INVALID);
        }

        if (PromoOfferType.GIFT_OFFER.getKey().equals(offer_type) &&
                (item_data == null || item_data.isEmpty() || item_data.equals("[]") || !this.validateItemData())) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_OFFER_PRODUCT_INVALID);
        }
        return ClientResponse.success(null);
    }

    private boolean validateLimitData() {
        List<JSONObject> limits = JsonUtils.DeSerialize(
                limit_data,
                new TypeToken<List<JSONObject>>() {
                }.getType());
        for (JSONObject limit : limits) {
            if (ConvertUtils.toInt(limit.get("limit_value")) <= 0 || ConvertUtils.toInt(limit.get("offer_value")) <= 0) {
                return false;
            }
        }
        return true;
    }

    private boolean validateItemData() {
        List<JSONObject> gifts = JsonUtils.DeSerialize(
                item_data,
                new TypeToken<List<JSONObject>>() {
                }.getType());
        for (JSONObject gift : gifts) {
            if (ConvertUtils.toInt(gift.get("item_id")) == 0 || ConvertUtils.toInt(gift.get("item_quantity")) == 0) {
                return false;
            }
        }
        return true;
    }
}