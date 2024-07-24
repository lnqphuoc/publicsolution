package com.app.server.enums;

import lombok.Getter;

@Getter
public enum AgencyOrderPromoType {
    GOODS_OFFER("GOODS_OFFER", "Tặng hàng", 1),
    GOODS_BONUS("GOODS_BONUS", "Hàng tặng kèm theo", 2),
    GIFT_BONUS("GIFT_BONUS", "Quà tặng kèm theo", 3),
    GIFT_SGR("GIFT_SGR", "Quà tặng SGR", 4),
    GIFT_VOUCHER("GIFT_VOUCHER", "Quà tặng trong voucher", 5);

    private int id;
    private String key;
    private String label;

    AgencyOrderPromoType(String key, String label, int id) {
        this.id = id;
        this.key = key;
        this.label = label;
    }

    public static AgencyOrderPromoType from(String offer_type) {
        for (AgencyOrderPromoType type : AgencyOrderPromoType.values()) {
            if (type.key.equals(offer_type)) {
                return type;
            }
        }
        return null;
    }
}