package com.app.server.data.dto.program;

public enum ProgramGoodsType {
    CONVERSION(1),
    AUTO(2);
    private final int value;

    private ProgramGoodsType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}