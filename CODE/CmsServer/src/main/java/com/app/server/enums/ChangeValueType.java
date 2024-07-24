package com.app.server.enums;

import lombok.Getter;

@Getter
public enum ChangeValueType {
    INCREASE(1, "INCREASE", "Tăng"),
    DECREASE(2, "DECREASE", "Giảm");

    private int id;
    private String code;
    private String label;

    ChangeValueType(int id, String code, String label) {
        this.id = id;
        this.code = code;
        this.label = label;
    }

    public static ChangeValueType from(String data) {
        for (ChangeValueType type : ChangeValueType.values()) {
            if (type.getCode().equals(data)) {
                return type;
            }
        }
        return null;
    }
}