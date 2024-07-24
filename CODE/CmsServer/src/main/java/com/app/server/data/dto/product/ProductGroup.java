package com.app.server.data.dto.product;

import lombok.Data;

@Data
public class ProductGroup {
    private int id;
    private String code;
    private String name;
    private int category_id;
    private int item_type;
    private String similar_name;
    private int status;
}