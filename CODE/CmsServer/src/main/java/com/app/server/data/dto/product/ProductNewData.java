package com.app.server.data.dto.product;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductNewData {
    private int product_id;
    private String code;
    private int priority;
    private long created_date;
}