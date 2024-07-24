package com.app.server.enums;

import lombok.Getter;

@Getter
public enum PriceDataType {
    PERCENT(1, "PERCENT", "Phần trăm"),
    MONEY(2, "MONEY", "Giá trị");

    private int id;
    private String code;
    private String label;

    PriceDataType(int id, String code, String label) {
        this.id = id;
        this.code = code;
        this.label = label;
    }

    public static PriceDataType from(String data) {
        for (PriceDataType type : PriceDataType.values()) {
            if (type.getCode().equals(data)) {
                return type;
            }
        }
        return null;
    }
}