package com.app.server.data.dto.product;

import lombok.Data;

@Data
public class ProductData {
    private int id;
    private String full_name;
    private String images;
    private String code;
    private int step;
    private int minimum_purchase;
    private int product_small_unit_id;
    private int quantity;
    private int product_quantity;
    private double price;
    private double total_begin_price;
    private double total_promo_price;
    private double total_end_price;
    private int is_error;
    private String note = "";
    private long common_price;

    public ProductData() {
    }

    public ProductData(int id, String full_name, int quantity) {
        this.id = id;
        this.full_name = full_name;
        this.quantity = quantity;
    }

    public ProductData(int id, String code, String full_name, int quantity, String images) {
        this.id = id;
        this.full_name = full_name;
        this.code = code;
        this.quantity = quantity;
        this.images = images;
    }

    public ProductData(int id, String code, String full_name, int quantity, String images, double price) {
        this.id = id;
        this.full_name = full_name;
        this.code = code;
        this.quantity = quantity;
        this.images = images;
        this.price = price;
    }
}