package com.app.server.data.dto.promo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PromoProductBasic {
    private int item_id;
    private int item_quantity;
    private Double item_price;
    private Double total_price;
    private String offer_type;

    public PromoProductBasic(int item_id, int item_quantity, Double item_price, String offer_type) {
        this.item_id = item_id;
        this.item_quantity = item_quantity;
        this.item_price = item_price;
        this.total_price = item_quantity * item_price;
        this.offer_type = offer_type;
    }

    public PromoProductBasic(PromoProductBasic promoProductBasic) {
        this.item_id = promoProductBasic.getItem_id();
        this.item_quantity = promoProductBasic.getItem_quantity();
        this.item_price = promoProductBasic.getItem_price();
        this.total_price = item_quantity * item_price;
        this.offer_type = promoProductBasic.getOffer_type();
    }
}