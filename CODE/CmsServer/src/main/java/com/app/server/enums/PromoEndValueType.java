package com.app.server.enums;

import lombok.Getter;

@Getter
public enum PromoEndValueType {
    IS_NULL(1, "IS_NULL", "Không có giá trị đến"),
    IS_NOT_NULL(2, "IS_NOT_NULL", "Có giá trị đến");

    private int id;
    private String key;
    private String label;

    PromoEndValueType(int id, String key, String label) {
        this.id = id;
        this.key = key;
        this.label = label;
    }
}