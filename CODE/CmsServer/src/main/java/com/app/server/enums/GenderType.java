package com.app.server.enums;

import lombok.Getter;

@Getter
public enum GenderType {
    WOMEN(2),
    MEN(1);

    private int value;

    GenderType(int value) {
        this.value = value;
    }

    public static GenderType from(int value) {
        for (GenderType type : GenderType.values()) {
            if (type.value == value) {
                return type;
            }
        }
        return null;
    }

    public static boolean valid(int value) {
        if (from(value) == null) {
            return false;
        }
        return true;
    }
}