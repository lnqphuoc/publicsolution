package com.app.server.data.dto.order;

import lombok.Data;

@Data
public class EstimateOrderData {
    private int product_total_quantity;
    private int gift_total_quantity;
    private long total_order;
}