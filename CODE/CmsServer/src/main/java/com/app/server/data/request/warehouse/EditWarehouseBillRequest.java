package com.app.server.data.request.warehouse;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EditWarehouseBillRequest {
    private int id;
    private int warehouse_id;
    private Integer target_info;
    private String note;
    private List<WarehouseBillDetailRequest> products = new ArrayList<>();

    public ClientResponse validate() {
        if (products == null || products.isEmpty()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PRODUCT_EMPTY);
        }

        if (warehouse_id <= 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.WAREHOUSE_EMPTY);
        }

        return ClientResponse.success(null);
    }
}