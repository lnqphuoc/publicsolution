package com.app.server.enums;

import lombok.Getter;

@Getter
public enum DeptOrderStatus {
    WAITING(1, "NONE", "Chưa thanh toán"),
    FINISH(2, "FINISH", "Đã thanh toán"),
    ;

    private int id;
    private String code;
    private String label;

    DeptOrderStatus(int id, String code, String label) {
        this.id = id;
        this.code = code;
        this.label = label;
    }
}