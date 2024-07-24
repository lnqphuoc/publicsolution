package com.app.server.enums;

import lombok.Getter;

@Getter
public enum ItemGroupType {
    GROUP(1, "GROUP", "Nhóm sản phẩm"),
    COMBO(2, "COMBO", "Combo");

    private int id;
    private String code;
    private String label;

    ItemGroupType(int id, String code, String label) {
        this.id = id;
        this.code = code;
        this.label = label;
    }

    public static ItemGroupType fromCode(String code) {
        for (ItemGroupType type : ItemGroupType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}