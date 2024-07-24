package com.app.server.data.request.product;

import com.app.server.constants.ResponseMessage;
import com.app.server.data.dto.product.Product;
import com.app.server.enums.ProductDataType;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Data;

@Data
public class ProductDataRequest {
    private String code = "";
    private String full_name = "";
    private int quantity = 0;
    private long price = 0;
    private String note = "";
    private String error = "";
    private String type = "";
    private int index;

    public ClientResponse validate() {
        if (code.isEmpty()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CODE_INVALID);
        }

        ProductDataType productDataType = ProductDataType.from(type);
        if (productDataType == null) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.TYPE_INVALID);
        }

        if (productDataType.getId() == ProductDataType.QUANTITY.getId() && quantity <= 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.QUANTITY_INVALID);
        }

        if (productDataType.getId() == ProductDataType.PRICE.getId() &&
                price <= 0 &&
                price != -1) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PRICE_INVALID);
        }
        return ClientResponse.success(null);
    }
}