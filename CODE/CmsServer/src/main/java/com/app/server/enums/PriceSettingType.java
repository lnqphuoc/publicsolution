package com.app.server.enums;

import lombok.Getter;

@Getter
public enum PriceSettingType {
    INCREASE(1, "INCREASE", "Tăng"),
    DECREASE(2, "DECREASE", "Giảm"),
    CONSTANT(3, "CONSTANT", "Cố định"),
    CONTACT(4, "CONTACT", "Liên hệ");

    private int id;
    private String code;
    private String label;

    PriceSettingType(int id, String code, String label) {
        this.id = id;
        this.code = code;
        this.label = label;
    }

    public static PriceSettingType from(String data) {
        for (PriceSettingType type : PriceSettingType.values()) {
            if (type.getCode().equals(data)) {
                return type;
            }
        }
        return null;
    }
}