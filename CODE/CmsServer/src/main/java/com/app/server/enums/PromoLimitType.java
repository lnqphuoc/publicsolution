package com.app.server.enums;

import lombok.Getter;

@Getter
public enum PromoLimitType {
    LIMITED("Giới hạn"),
    UNLIMITED("Vô tận"),
    RANGE("Khoảng");
    private int id;
    private String key;
    private String label;

    PromoLimitType(String label) {
        this.label = label;
    }
}