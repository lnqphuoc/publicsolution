package com.app.server.enums;

import lombok.Getter;

@Getter
public enum ProductInOrderType {
    PRODUCT_BUY(1, "PRODUCT_BUY", "Sản phẩm mua"),
    PRODUCT_GIFT(2, "PRODUCT_GIFT", "Quà tặng");

    private int id;
    private String code;
    private String label;

    ProductInOrderType(int id, String code, String label) {
        this.id = id;
        this.code = code;
        this.label = label;
    }

    public static ProductInOrderType from(String data) {
        for (ProductInOrderType type : ProductInOrderType.values()) {
            if (type.getCode().equals(data)) {
                return type;
            }
        }
        return null;
    }
}