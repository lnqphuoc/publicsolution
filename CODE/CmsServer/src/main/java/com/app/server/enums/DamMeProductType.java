package com.app.server.enums;

import lombok.Getter;

@Getter
public enum DamMeProductType {
    PRODUCT(1, "PRODUCT", "Sản phẩm"),
    PRODUCT_GROUP(2, "PRODUCT_GROUP", "Nhóm hàng"),
    CATEGORY(3, "CATEGORY", "Danh mục"),
    ;

    private int id;
    private String key;
    private String label;

    DamMeProductType(int id, String key, String label) {
        this.id = id;
        this.key = key;
        this.label = label;
    }

    public static DamMeProductType from(int id) {
        for (DamMeProductType type : DamMeProductType.values()) {
            if (type.getId() == id) {
                return type;
            }
        }

        return null;
    }

    public static DamMeProductType fromKey(String key) {
        for (DamMeProductType type : DamMeProductType.values()) {
            if (type.getKey().equals(key)) {
                return type;
            }
        }

        return null;
    }
}