package com.app.server.data.dto.promo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PromoGiftClaim {
    private int item_id;
    private int item_quantity;
    private Double item_price;
    private String offer_type;
    private List<PromoBasicData> promoClaims = new ArrayList<>();

    public PromoGiftClaim(int item_id, int item_quantity, Double item_price, String offer_type) {
        this.item_id = item_id;
        this.item_quantity = item_quantity;
        this.item_price = item_price;
        this.offer_type = offer_type;
    }
}