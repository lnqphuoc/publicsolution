package com.app.server.enums;

import lombok.Getter;

@Getter
public enum VisibilityDataType {
    PRODUCT(1, "PRODUCT", "Sản phẩm"),
    PRODUCT_GROUP(2, "PRODUCT_GROUP", "Nhóm hàng"),
    BRAND_CATEGORY(3, "BRAND_CATEGORY", "PLSP theo thương hiệu"),
    PRODUCT_CATEGORY(4, "PRODUCT_CATEGORY", "Phân loại sản phẩm"),
    ITEM_CATEGORY(5, "ITEM_CATEGORY", "Mặt hàng"),
    BRAND(6, "BRAND", "Thương hiệu");

    private int id;
    private String code;
    private String label;

    VisibilityDataType(int id, String code, String label) {
        this.id = id;
        this.code = code;
        this.label = label;
    }

    public static VisibilityDataType from(String visibility_data_type) {
        for (VisibilityDataType type : VisibilityDataType.values()) {
            if (type.getCode().equals(visibility_data_type)) {
                return type;
            }
        }
        return null;
    }
}