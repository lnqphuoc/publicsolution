package com.app.server.data.request.warehouse;

import lombok.Data;

@Data
public class WarehouseBillDetailRequest {
    private int product_id;
    private int product_quantity;
    private String note;
}