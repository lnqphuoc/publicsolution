package com.app.server.enums;

import lombok.Getter;

@Getter
public enum MembershipType {
    THANH_VIEN(1, "Thành viên", "M"),
    BAC(2, "Bạc", "B"),
    TITAN(3, "Titan", "T"),
    VANG(4, "Vàng", "V"),
    BACH_KIM(5, "Bạch kim", "K");

    private int key;
    private String value;
    private String code;

    MembershipType(int key, String value, String code) {
        this.key = key;
        this.value = value;
        this.code = code;
    }

    public static MembershipType from(int key) {
        for (MembershipType type : MembershipType.values()) {
            if (type.key == key) {
                return type;
            }
        }
        return null;
    }
}