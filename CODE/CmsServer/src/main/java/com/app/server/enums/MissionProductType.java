package com.app.server.enums;

import lombok.Getter;

@Getter
public enum
MissionProductType {
    PRODUCT(1, "PRODUCT", "Sản phẩm"),
    PRODUCT_GROUP(2, "PRODUCT_GROUP", "Nhóm hàng"),
    CATEGORY(3, "CATEGORY", "Danh mục"),
    ITEM_TYPE(4, "ITEM_TYPE", "Loại sản phẩm"),
    ;

    private int id;
    private String key;
    private String label;

    MissionProductType(int id, String key, String label) {
        this.id = id;
        this.key = key;
        this.label = label;
    }

    public static MissionProductType from(int id) {
        for (MissionProductType type : MissionProductType.values()) {
            if (type.getId() == id) {
                return type;
            }
        }

        return null;
    }

    public static MissionProductType fromKey(String key) {
        for (MissionProductType type : MissionProductType.values()) {
            if (type.getKey().equals(key)) {
                return type;
            }
        }

        return null;
    }
}