package com.app.server.data.dto.product;

import lombok.Data;

@Data
public class ProductPrice {
    private int id;
    private long price;
    private int minimum_purchase;
}