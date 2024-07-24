package com.app.server.data.request.order;

import com.app.server.data.dto.order.OrderConfirmationProductData;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ImportOrderConfirmationRequest {
    private List<OrderConfirmationProductData> products;
}