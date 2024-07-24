package com.app.server.data.request.promo;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Data;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

@Data
public class PromoItemGroupRequest {
    private int id;
    private String code;
    private String images;
    private String full_name;
    private int data_index;
    private String type = "GROUP";
    private long price;
    private String note;
    private int combo_id;
    private long max_offer_per_promo;
    private long max_offer_per_agency;
    private List<PromoItemGroupDetailRequest> products = new ArrayList<>();

    public ClientResponse validate() {
        if (type.equals("COMBO") && combo_id == 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.COMBO_INVALID);
        }

        if (products.isEmpty()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PRODUCT_EMPTY);
        }

        ClientResponse clientResponse = ClientResponse.success(null);
        for (int iItem = 0; iItem < products.size(); iItem++) {
            PromoItemGroupDetailRequest promoItemGroupDetailRequest = products.get(iItem);
            clientResponse = promoItemGroupDetailRequest.validate();
            if (clientResponse.failed()) {
                clientResponse.setMessage("[Sản phẩm " + (iItem + 1) + "]" + clientResponse.getMessage());
                return clientResponse;
            }
        }
        return clientResponse;
    }
}