package com.app.server.enums;

import lombok.Getter;

@Getter
public enum TransactionFunctionType {
    DON_HANG(1, "DON_HANG", "Đơn hàng"),
    THANH_TOAN(2, "THANH_TOAN", "Thanh toán"),
    TANG_GIAM(3, "TANG_GIAM", "Tăng giảm"),
    PHI_PHAT_SINH(4, "PHI_PHAT_SINH", "Phí phát sinh"),
    ;

    private int id;
    private String code;
    private String label;

    TransactionFunctionType(int id, String code, String label) {
        this.id = id;
        this.code = code;
        this.label = label;
    }
}