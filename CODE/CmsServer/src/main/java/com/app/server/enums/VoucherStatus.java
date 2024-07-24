package com.app.server.enums;

import lombok.Getter;

@Getter
public enum VoucherStatus {
    READY(1, "READY", "Chưa sử dụng"),
    USED(2, "USED", "Đã sử dụng"),
    CANCEL(3, "CANCEL", "Hủy"),
    EXPIRED(4, "EXPIRED", "Đã hết hạn"),
    ;

    private int id;
    private String code;
    private String label;

    VoucherStatus(int id, String code, String label) {
        this.id = id;
        this.code = code;
        this.label = label;
    }
}