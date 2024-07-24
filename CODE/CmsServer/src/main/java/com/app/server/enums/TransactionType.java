package com.app.server.enums;

import lombok.Getter;

@Getter
public enum TransactionType {
    DON_HANG(1, "ORDER", "Đơn hàng"),
    CNO(2, "CNO", "Tăng/giảm công nợ"),
    DIEU_CHINH_DTT(3, "DTT", "Điều chỉnh DTT"),
    HBTL(4, "HBTL", "Đơn hàng trả");

    private int id;
    private String key;
    private String label;

    TransactionType(int id, String key, String label) {
        this.id = id;
        this.key = key;
        this.label = label;
    }

    public static TransactionType from(int id) {
        for (TransactionType type : TransactionType.values()) {
            if (type.getId() == id) {
                return type;
            }
        }

        return null;
    }

    public static TransactionType fromKey(String key) {
        for (TransactionType type : TransactionType.values()) {
            if (type.getKey().equals(key)) {
                return type;
            }
        }

        return null;
    }
}