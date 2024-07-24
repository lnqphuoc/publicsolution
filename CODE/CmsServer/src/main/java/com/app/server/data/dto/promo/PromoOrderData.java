package com.app.server.data.dto.promo;

import lombok.Data;

@Data
public class PromoOrderData {
    private int promo_id;
    private String promo_code;
    private String promo_name;
    private String promo_description;
    private int promo_percent;
}