package com.app.server.data.dto.product;

import lombok.Data;
import lombok.Getter;

@Data
public class ProductCache {
    private int id;
    private String full_name;
    private String images;
    private String code;
    private double price;
    private int step;
    private int minimum_purchase;
    private int product_small_unit_id;
    private int product_group_id;
    private int pltth_id;
    private int plsp_id;
    private int mat_hang_id;
    private int nganh_hang_id;
    private int status;
    private Integer brand_id;
    private int app_active;
    private int visibility;
    private int item_type;
    private double private_price;
    private double common_price;
    private int bravo_id;

    public boolean filterSearch(String productSearch) {
        if (productSearch == null || productSearch.isEmpty()) {
            return true;
        }

        if (code.contains(productSearch) || code.toLowerCase().contains(productSearch.toLowerCase())) {
            return true;
        }

        if (full_name.contains(productSearch) ||
                full_name.toLowerCase().contains(productSearch.toLowerCase())) {
            return true;
        }

        return false;
    }
}