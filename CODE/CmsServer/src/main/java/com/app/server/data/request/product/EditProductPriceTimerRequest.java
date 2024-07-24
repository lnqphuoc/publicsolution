package com.app.server.data.request.product;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import com.ygame.framework.utils.DateTimeUtils;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EditProductPriceTimerRequest {
    private int id;
    private String name = "";
    private String note = "";
    private long start_date_millisecond;
    List<ProductPriceTimerRequest> products = new ArrayList<>();

    public ClientResponse validate() {
        if (name.isEmpty()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.NAME_INVALID);
        }

        if (start_date_millisecond <= 0
                || start_date_millisecond < DateTimeUtils.getMilisecondsNow()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.TIME_BEGIN_INVALID);
        }

        if (products.isEmpty()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PRODUCT_EMPTY);
        }

        for (int iProduct = 0; iProduct < products.size(); iProduct++) {
            ClientResponse crProduct = products.get(iProduct).validate();
            if (crProduct.failed()) {
                crProduct.setMessage("[Thứ " + (iProduct + 1) + "]" + crProduct.getMessage());
                return crProduct;
            }
        }

        start_date_millisecond = start_date_millisecond - (start_date_millisecond % 60000);
        return ClientResponse.success(null);
    }
}