package com.app.server.enums;

import lombok.Getter;

@Getter
public enum TransactionCTTLStatus {
    THOA(1, "Thỏa"),
    KHONG_THOA(2, "Không thỏa"),
    TU_CHOI(3, "Từ chối");
    private int id;
    private String key;
    private String label;

    TransactionCTTLStatus(int id, String label) {
        this.id = id;
        this.key = "" + id;
        this.label = label;
    }

    public static TransactionCTTLStatus from(int id) {
        for (TransactionCTTLStatus type : TransactionCTTLStatus.values()) {
            if (type.id == id) {
                return type;
            }
        }
        return null;
    }
}