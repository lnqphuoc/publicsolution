package com.app.server.enums;

import lombok.Getter;

@Getter
public enum PromoConditionCTTLType {
    PRODUCT_QUANTITY(1, "PRODUCT_QUANTITY", "Tổng số lượng sản phẩm"),
    ORDER_PRICE(2, "ORDER_PRICE", "Tổng giá trị đơn hàng"),
    PRODUCT_PRICE(3, "PRODUCT_PRICE", "Tổng doanh số sản phẩm"),
    DTT(5, "DTT", "Tổng doanh thu thuần");

    private int id;
    private String key;
    private String label;

    PromoConditionCTTLType(int id, String key, String label) {
        this.id = id;
        this.key = key;
        this.label = label;
    }

    public static PromoConditionCTTLType from(String condition_type) {
        for (PromoConditionCTTLType type : PromoConditionCTTLType.values()) {
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