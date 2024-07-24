package com.app.server.enums;

import lombok.Getter;

@Getter
public enum DealPriceStatus {
    WAITING_CMS(1, "WAITING_CMS", "Cần xác nhận"),
    WAITING_APP(2, "WAITING_APP", "Chờ xác nhận"),
    CONFIRMED(3, "CONFIRMED", "Đã xác nhận"),
    CANCEL(4, "CANCEL", "Đã hủy"),
    ;

    private int id;
    private String code;
    private String label;

    DealPriceStatus(int id, String code, String label) {
        this.id = id;
        this.code = code;
        this.label = label;
    }
}