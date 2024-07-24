package com.app.server.enums;

import lombok.Getter;

@Getter
public enum TransactionEffectValueType {
    NONE(1, "NONE", "Không đổi"),
    INCREASE(2, "INCREASE", "Tăng"),
    DECREASE(3, "DECREASE", "Giảm"),
    ;

    private int id;
    private String code;
    private String label;

    TransactionEffectValueType(int id, String code, String label) {
        this.id = id;
        this.code = code;
        this.label = label;
    }

    public static TransactionEffectValueType from(int id) {
        for (TransactionEffectValueType type : TransactionEffectValueType.values()) {
            if (type.id == id) {
                return type;
            }
        }
        return null;
    }
}