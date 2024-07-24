package com.app.server.data.request.order;

import com.app.server.constants.ResponseMessage;
import com.app.server.data.dto.order.HuntSaleOrder;
import com.app.server.data.dto.order.HuntSaleOrderDetail;
import com.app.server.enums.ResponseStatus;
import com.app.server.enums.SourceOrderType;
import com.app.server.response.ClientResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class EstimateCostOrderRequest {
    private int order_id;
    private int agency_id;
    //private List<OrderProductRequest> products = new ArrayList<>();
    private List<OrderProductRequest> goods = new ArrayList<>();
    private int has_gift_hint = 1;

    private List<HuntSaleOrderDetail> hunt_sale_products = new ArrayList<>();

    private List<Integer> vouchers = new ArrayList<>();

    public ClientResponse validRequest() {
//        if ((products == null || products.isEmpty())
//                && (hunt_sale_products == null || hunt_sale_products.isEmpty())) {
//            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PRODUCT_EMPTY);
//        }

//        /**
//         *
//         */
        for (int iProduct = 0; iProduct < hunt_sale_products.size(); iProduct++) {
            if (hunt_sale_products.get(iProduct).getQuantity() <= 0) {
                hunt_sale_products.get(iProduct).setQuantity(1);
            }
        }
        return ClientResponse.success(null);
    }
}