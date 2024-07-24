package com.app.server.enums;

import lombok.Getter;

@Getter
public enum CTXHOfferType {
    VOUCHER("VOUCHER", "Voucher", 6);

    private int id;
    private String key;
    private String label;

    CTXHOfferType(String key, String label, int id) {
        this.id = id;
        this.key = key;
        this.label = label;
    }

    public static CTXHOfferType from(String offer_type) {
        for (CTXHOfferType type : CTXHOfferType.values()) {
            if (type.key.equals(offer_type)) {
                return type;
            }
        }
        return null;
    }
}