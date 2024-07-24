package com.app.server.enums;

import lombok.Getter;

@Getter
public enum PromoDamMeConditionType {
    PRODUCT_QUANTITY(1, "PRODUCT_QUANTITY", "Tổng số lượng sản phẩm"),
    PRODUCT_PRICE(2, "PRODUCT_PRICE", "Tổng doanh số sản phẩm");

    private int id;
    private String key;
    private String label;

    PromoDamMeConditionType(int id, String key, String label) {
        this.id = id;
        this.key = key;
        this.label = label;
    }

    public static PromoDamMeConditionType from(String condition_type) {
        for (PromoDamMeConditionType type : PromoDamMeConditionType.values()) {
            if (type.key.equals(condition_type)) {
                return type;
            }
        }

        return null;
    }

    public boolean isCTTLProduct() {
        if (id == PRODUCT_QUANTITY.id) {
            return true;
        }
        if (id == PRODUCT_PRICE.id) {
            return true;
        }
        return false;
    }

    public boolean isCTTLProductQuantity() {
        if (id == PRODUCT_QUANTITY.id) {
            return true;
        }
        return false;
    }
}