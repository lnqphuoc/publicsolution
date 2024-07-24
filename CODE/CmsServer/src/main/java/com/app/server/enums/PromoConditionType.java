package com.app.server.enums;

import lombok.Getter;

@Getter
public enum PromoConditionType {
    PRODUCT_QUANTITY(1, "PRODUCT_QUANTITY", "Số lượng sản phẩm"),
    ORDER_PRICE(2, "ORDER_PRICE", "Giá trị đơn hàng"),
    PRODUCT_PRICE(3, "PRODUCT_PRICE", "Doanh số sản phẩm"),
    STEP(4, "STEP", "Bước nhảy"),
    DTT(5, "DTT", "Doanh thu thuần");

    private int id;
    private String key;
    private String label;

    PromoConditionType(int id, String key, String label) {
        this.id = id;
        this.key = key;
        this.label = label;
    }

    public static PromoConditionType from(String condition_type) {
        for (PromoConditionType type : PromoConditionType.values()) {
            if (type.key.equals(condition_type)) {
                return type;
            }
        }

        return null;
    }
}