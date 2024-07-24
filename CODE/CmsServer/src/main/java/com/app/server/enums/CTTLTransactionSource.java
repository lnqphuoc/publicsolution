package com.app.server.enums;

import lombok.Getter;

@Getter
public enum CTTLTransactionSource {
    AUTO(1, "AUTO", "Tự động"),
    ADMIN(2, "ADMIN", "Admin"),
    ;

    private int id;
    private String key;
    private String label;

    CTTLTransactionSource(int id, String key, String label) {
        this.id = id;
        this.key = key;
        this.label = label;
    }

    public static CTTLTransactionSource from(int id) {
        for (CTTLTransactionSource type : CTTLTransactionSource.values()) {
            if (type.getId() == id) {
                return type;
            }
        }

        return null;
    }

    public static CTTLTransactionSource fromKey(String key) {
        for (CTTLTransactionSource type : CTTLTransactionSource.values()) {
            if (type.getKey().equals(key)) {
                return type;
            }
        }

        return null;
    }
}