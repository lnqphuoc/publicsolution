package com.app.server.enums;

import lombok.Getter;

@Getter
public enum PromoOfferType {
    GOODS_OFFER("GOODS_OFFER", "Tặng hàng", 1),
    MONEY_DISCOUNT("MONEY_DISCOUNT", "Giảm tiền", 2),
    PERCENT_DISCOUNT("PERCENT_DISCOUNT", "Chiết khấu", 3),
    GIFT_OFFER("GIFT_OFFER", "Tặng quà", 4),
    FIXED_PRICE("FIXED_PRICE", "Chỉ định", 5),
    VOUCHER("VOUCHER", "Voucher", 6);

    private int id;
    private String key;
    private String label;

    PromoOfferType(String key, String label, int id) {
        this.id = id;
        this.key = key;
        this.label = label;
    }

    public static PromoOfferType from(String offer_type) {
        for (PromoOfferType type : PromoOfferType.values()) {
            if (type.key.equals(offer_type)) {
                return type;
            }
        }
        return null;
    }
}