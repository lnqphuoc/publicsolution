package com.app.server.enums;

import lombok.Getter;

@Getter
public enum BusinessType {
    NONE(0),
    SI(1),
    LE(2),
    ALL(3);

    private int value;

    BusinessType(int value) {
        this.value = value;
    }

    public static BusinessType from(int value) {
        for (BusinessType type : BusinessType.values()) {
            if (type.value == value) {
                return type;
            }
        }
        return NONE;
    }

    public static boolean valid(int value) {
        if (from(value) == null) {
            return false;
        }
        return true;
    }
}