package com.app.server.data.request.order;

import com.app.server.constants.ResponseMessage;
import com.app.server.data.dto.order.EstimateOrderData;
import com.app.server.data.dto.order.HuntSaleOrderDetail;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class EditOrderProductRequest {
    private int id;
    private int agency_id;
    private String note_internal;
    private int address_delivery_id;
    private int address_export_billing_id;
    private long delivery_time_request = 0;
    //private List<OrderProductRequest> products = new ArrayList<>();
    private List<OrderProductRequest> goods = new ArrayList<>();

    private List<HuntSaleOrderDetail> hunt_sale_products = new ArrayList<>();
    private EstimateOrderData estimate_order_data = new EstimateOrderData();
    private List<Integer> vouchers = new ArrayList<>();

    public ClientResponse validRequest() {
        if (id < 1) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_NOT_FOUND);
        }
        if (hunt_sale_products.size() == 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PRODUCT_EMPTY);
        }
        if (address_delivery_id < 1) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_ADDRESS_INVALID);
        }
        if (!checkProductDuplicate()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PRODUCT_CAN_NOT_DUPLICATE);
        }
        return ClientResponse.success(null);
    }

    public boolean checkProductDuplicate() {
        return true;
        //products.size() == 0 ? true : products.stream().map(OrderProductRequest::getProduct_id).distinct().count() == products.size();
    }
}