package com.app.server.data.response.order;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OrderProductResponse {
    private int id;
    private String code;
    private String full_name;
    private int product_small_unit_id;
    private int step;
    private int minimum_purchase;
    private String images;
    private double price;
    private int quantity;
    private Double total_begin_price;
    private Double total_promo_price;
    private Double total_csbh_price;
    private Double total_end_price;
    private Double uu_dai_dam_me;
    private String note;
    private int is_error = 0;
    private int item_type;
    private List<OrderPromoResponse> promos = new ArrayList<>();
}