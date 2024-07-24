package com.app.server.data.dto.program;

public enum ProgramOfferType {
    GOODS_OFFER(1),
    MONEY_DISCOUNT(2),
    PERCENT_DISCOUNT(3),
    GIFT_OFFER(4),
    FIXED_PRICE(5),
    VOUCHER(6);
    private final int value;

    private ProgramOfferType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}