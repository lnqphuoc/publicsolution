package com.app.server.data.request.promo;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.PromoOfferType;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

@Data
public class PromoOfferRequest {
    private int id;
    private Long offer_value;
    private double conversion_ratio;
    private String offer_type;
    private List<PromoOfferProductRequest> offer_products = new ArrayList<>();
    private List<PromoOfferBonusRequest> offer_bonus = new ArrayList<>();
    @ApiModelProperty("Danh sách nguyên tắc: [{\"id\" : 1, \"expire_day\" : 10}]")
    private String voucher_data;
    private List<JSONObject> offer_voucher_info = new ArrayList<>();
    private List<JSONObject> offer_voucher_release_periods = new ArrayList<>();

    public ClientResponse validate() {
        if (PromoOfferType.from(offer_type) == null) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_OFFER_TYPE_INVALID);
        }

        if (!offer_products.isEmpty()) {
            for (int iProduct = 0; iProduct < offer_products.size(); iProduct++) {
                PromoOfferProductRequest promoOfferProductRequest = offer_products.get(iProduct);
                ClientResponse clientResponse = promoOfferProductRequest.validate();
                if (clientResponse.failed()) {
                    clientResponse.setMessage("[" + "Ưu đãi sản phẩm thứ: " + (iProduct + 1) + "]" + clientResponse.getMessage());
                    return clientResponse;
                }
            }
        }

        if (!offer_bonus.isEmpty()) {
            for (int iBonus = 0; iBonus < offer_bonus.size(); iBonus++) {
                PromoOfferBonusRequest promoOfferBonusRequest = offer_bonus.get(iBonus);
                ClientResponse clientResponse = promoOfferBonusRequest.validate();
                if (clientResponse.failed()) {
                    clientResponse.setMessage("[" + "Ưu đãi tặng kèm thứ: " + (iBonus + 1) + "]" + clientResponse.getMessage());
                    return clientResponse;
                }
            }
        }


        return ClientResponse.success(null);
    }
}