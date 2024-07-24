package com.app.server.enums;

public enum SanSaleItemType {
    PRODUCT(1),
    COMBO(2);
    private final int value;

    private SanSaleItemType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}