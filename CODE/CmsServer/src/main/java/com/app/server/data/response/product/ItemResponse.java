package com.app.server.data.response.product;

import lombok.Data;

@Data
public class ItemResponse {
    private int id;
    private String full_name;
    private String images;
    private String code;
    private int step;
    private int minimum_purchase;
    private int product_small_unit_id;
    private int is_combo;
}