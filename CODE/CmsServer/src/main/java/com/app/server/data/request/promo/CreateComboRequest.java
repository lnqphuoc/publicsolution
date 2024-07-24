package com.app.server.data.request.promo;

import com.app.server.constants.ResponseMessage;
import com.app.server.data.dto.product.ProductData;
import com.app.server.data.request.BasicRequest;
import com.app.server.data.response.order.ItemOfferResponse;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CreateComboRequest {
    private int id;
    private String name = "";
    private String description = "";
    private String code = "";
    private String images = "";
    private int status = 1;
    private String hot_label = "";

    private List<ProductData> products = new ArrayList<>();

    public ClientResponse validate() {
        if (name.isEmpty() ||
                description.isEmpty() ||
                code.isEmpty() ||
                images.isEmpty() ||
                products.size() == 0
        ) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.INFO_INVALID);
        }
        return ClientResponse.success(null);
    }
}