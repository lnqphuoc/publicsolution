package com.app.server.enums;

import lombok.Getter;

@Getter
public enum DeptTransactionStatus {
    WAITING(1, "WAITING", "Chờ xác nhận"),
    CONFIRMED(2, "CONFIRMED", "Đã xác nhận"),
    REJECT(3, "REJECT", "Từ chối"),
    ;

    private int id;
    private String code;
    private String label;

    DeptTransactionStatus(int id, String code, String label) {
        this.id = id;
        this.code = code;
        this.label = label;
    }
}