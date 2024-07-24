package com.app.server.enums;

import lombok.Getter;

@Getter
public enum ItemType {
    MAY_MOC(1, "Máy móc"),
    PHU_TUNG(2, "Phụ tùng");

    private int key;
    private String value;

    ItemType(int key, String value) {
        this.key = key;
        this.value = value;
    }

    public static ItemType from(int key) {
        for (ItemType type : ItemType.values()) {
            if (type.key == key) {
                return type;
            }
        }
        return null;
    }
}