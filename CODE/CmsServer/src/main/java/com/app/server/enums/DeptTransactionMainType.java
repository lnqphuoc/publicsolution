package com.app.server.enums;

import lombok.Getter;

@Getter
public enum DeptTransactionMainType {
    INCREASE(1, "INCREASE", "Tăng công nợ"),
    DECREASE(2, "DECREASE", "Giảm công nợ"),
    ;

    private int id;
    private String code;
    private String label;

    DeptTransactionMainType(int id, String code, String label) {
        this.id = id;
        this.code = code;
        this.label = label;
    }

    public static DeptTransactionMainType fromLabel(String label) {
        for (DeptTransactionMainType type : DeptTransactionMainType.values()) {
            if (type.getLabel().equals(label)) {
                return type;
            }
        }
        return null;
    }
}