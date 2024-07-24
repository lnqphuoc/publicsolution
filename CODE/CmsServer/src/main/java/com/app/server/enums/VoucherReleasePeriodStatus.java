package com.app.server.enums;

import lombok.Getter;

@Getter
public enum VoucherReleasePeriodStatus {
    WAITING(1, "WAITING", "Chưa kích hoạt"),
    ACTIVATED(2, "ACTIVATED", "Đã kích hoạt"),
    PENDING(3, "PENDING", "Kết thúc"),
    ;

    private int id;
    private String code;
    private String label;

    VoucherReleasePeriodStatus(int id, String code, String label) {
        this.id = id;
        this.code = code;
        this.label = label;
    }
}