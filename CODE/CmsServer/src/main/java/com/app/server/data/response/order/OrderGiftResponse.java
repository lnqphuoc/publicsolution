package com.app.server.data.response.order;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OrderGiftResponse {
    private int id;
    private String code;
    private String full_name;
    private int product_small_unit_id;
    private int step;
    private int minimum_purchase;
    private String images;
    private int price;
    private int quantity;
    private Long total_begin_price;
    private Long total_promo_price;
    private Long total_end_price;

    private List<OrderPromoResponse> promos = new ArrayList<>();
}