package com.app.server.data.dto.promo;

import lombok.Data;

@Data
public class PromoOfferClaim {
    private String offer_type;
    private Long offer_value;
}