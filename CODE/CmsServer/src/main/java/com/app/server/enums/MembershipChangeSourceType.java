package com.app.server.enums;

import lombok.Getter;

@Getter
public enum MembershipChangeSourceType {
    THANH_TOAN(1, "Thanh toán", "THANH_TOAN"),
    RESET(2, "Tính lại cuối năm", "RESET"),
    GOC(3, "Gốc", "GOC");

    private int key;
    private String value;
    private String code;

    MembershipChangeSourceType(int key, String value, String code) {
        this.key = key;
        this.value = value;
        this.code = code;
    }

    public static MembershipChangeSourceType from(int key) {
        for (MembershipChangeSourceType type : MembershipChangeSourceType.values()) {
            if (type.key == key) {
                return type;
            }
        }
        return null;
    }
}