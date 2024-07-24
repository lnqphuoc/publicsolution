package com.app.server.enums;

import lombok.Getter;

@Getter
public enum ProductDataType {
    QUANTITY(1, "QUANTITY", "Số lượng"),
    PRICE(2, "PRICE", "Giá");

    private int id;
    private String code;
    private String label;

    ProductDataType(int id, String code, String label) {
        this.id = id;
        this.code = code;
        this.label = label;
    }

    public static ProductDataType from(String data) {
        for (ProductDataType type : ProductDataType.values()) {
            if (type.getCode().equals(data)) {
                return type;
            }
        }
        return null;
    }
}