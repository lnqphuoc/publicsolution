package com.app.server.data.response.order;

import lombok.Data;

@Data
public class OrderPromoResponse {
    private int promo_id;
    private String promo_name;
    private String promo_code;
}