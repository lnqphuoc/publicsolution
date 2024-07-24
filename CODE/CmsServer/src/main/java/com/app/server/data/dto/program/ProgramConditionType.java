package com.app.server.data.dto.program;

public enum ProgramConditionType {
    ORDER_PRICE(1),
    PRODUCT_QUANTITY(2),
    PRODUCT_PRICE(3),
    STEP(4),
    DTT(5);
    private final int value;

    private ProgramConditionType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}