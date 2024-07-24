package com.app.server.enums;

import lombok.Getter;

@Getter
public enum VoucherOfferType {
    MONEY_DISCOUNT("MONEY_DISCOUNT", "Giảm tiền", 2),
    GIFT_OFFER("GIFT_OFFER", "Tặng quà", 4),
    ACOIN("ACOIN", "A Coin", 5);

    private int id;
    private String key;
    private String label;

    VoucherOfferType(String key, String label, int id) {
        this.id = id;
        this.key = key;
        this.label = label;
    }

    public static VoucherOfferType from(String offer_type) {
        for (VoucherOfferType type : VoucherOfferType.values()) {
            if (type.key.equals(offer_type)) {
                return type;
            }
        }
        return null;
    }
}