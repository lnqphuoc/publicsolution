package com.app.server.enums;

import lombok.Getter;

@Getter
public enum CSDMTransactionType {
    DON_HANG(1, "ORDER", "Đơn hàng"),
    HBTL(2, "HBTL", "Đơn hàng trả"),
    PHIEU(6, "PHIEU", "Phiếu điều chỉnh giảm"),
    ;

    private int id;
    private String key;
    private String label;

    CSDMTransactionType(int id, String key, String label) {
        this.id = id;
        this.key = key;
        this.label = label;
    }

    public static CSDMTransactionType from(int id) {
        for (CSDMTransactionType type : CSDMTransactionType.values()) {
            if (type.getId() == id) {
                return type;
            }
        }

        return null;
    }

    public static CSDMTransactionType fromKey(String key) {
        for (CSDMTransactionType type : CSDMTransactionType.values()) {
            if (type.getKey().equals(key)) {
                return type;
            }
        }

        return null;
    }
}