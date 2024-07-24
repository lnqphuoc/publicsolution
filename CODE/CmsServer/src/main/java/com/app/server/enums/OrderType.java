package com.app.server.enums;

import lombok.Getter;

@Getter
public enum OrderType {
    INSTANTLY(1, "DON_HANG_BAN", "Đơn hàng bán"),
    APPOINTMENT(2, "PHIEU_HEN_GIAO", "Phiếu hẹn giao"),
    CONTRACT(3, "DON_HOP_DONG", "Đơn hợp đồng"),
    RETAIL(4, "DON_BAN_LE", "Đơn bán lẻ");
    private int value;
    private String code;
    private String label;

    OrderType(int value, String code, String label) {
        this.value = value;
        this.code = code;
        this.label = label;
    }
}