package com.app.server.enums;

public enum SanSaleOfferType {
    NONE(0),
    PERCENT_DISCOUNT(1),
    MONEY_DISCOUNT(2);

    private final int value;

    private SanSaleOfferType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}