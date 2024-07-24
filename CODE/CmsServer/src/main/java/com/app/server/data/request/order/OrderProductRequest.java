package com.app.server.data.request.order;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderProductRequest {
    private int product_id;
    private int product_quantity;
    private Long product_price;
}