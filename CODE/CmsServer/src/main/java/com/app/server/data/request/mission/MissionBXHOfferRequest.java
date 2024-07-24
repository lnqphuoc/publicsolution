package com.app.server.data.request.mission;

import com.app.server.constants.ResponseMessage;
import com.app.server.data.request.promo.PromoOfferBonusRequest;
import com.app.server.data.request.promo.PromoOfferProductRequest;
import com.app.server.enums.PromoOfferType;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

@Data
public class MissionBXHOfferRequest {
    @ApiModelProperty("Không bắt buộc")
    private int id;
    @ApiModelProperty("Giá trị giảm tiền được hưởng")
    private Long offer_value;
    @ApiModelProperty("Giá trị A-coin được hưởng")
    private Long offer_acoin_value;
    @ApiModelProperty("Giá trị % DTT được hưởng")
    private Long offer_dtt_value;
    @ApiModelProperty("Danh sách nguyên tắc: [{\"id\" : 1, \"expire_day\" : 10, \"offer_value\" : 1}]")
    private String voucher_data;

    private List<JSONObject> offer_voucher_info = new ArrayList<>();

    public ClientResponse validate() {
        if (!(offer_value != 0 || offer_acoin_value != 0 || offer_dtt_value != 0)) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_OFFER_NOT_EMPTY);
        }
        return ClientResponse.success(null);
    }
}