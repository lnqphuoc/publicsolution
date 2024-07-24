package com.app.server.enums;

import lombok.Getter;

@Getter
public enum DeptType {
    DEPT_DON_HANG(1, "DEPT_DON_HANG", "Đơn hàng"),
    DEPT_INCREASE(2, "DEPT_INCREASE", "Tăng công nợ"),
    DEPT_DECREASE(3, "DEPT_DECREASE", "Giảm công nợ"),
    ;

    private int id;
    private String code;
    private String label;

    DeptType(int id, String code, String label) {
        this.id = id;
        this.code = code;
        this.label = label;
    }

    public static DeptType fromLabel(String label) {
        for (DeptType type : DeptType.values()) {
            if (type.label.equals(label)) {
                return type;
            }
        }
        return null;
    }
}