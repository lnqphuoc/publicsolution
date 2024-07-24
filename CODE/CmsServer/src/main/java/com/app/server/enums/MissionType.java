package com.app.server.enums;

import lombok.Getter;

@Getter
public enum MissionType {
    THANH_TOAN(1, "Thanh toán", "[3,6]"),
    MUA_HANG(2, "Mua hàng", "[1,2,3,4,5,6]"),
    NQH(3, "Không phát sinh nợ quá hạn", "[]");
    private int id;
    private String data;
    private String label;

    MissionType(int id, String label, String data) {
        this.id = id;
        this.data = data;
        this.label = label;
    }

    public static MissionType from(int id) {
        for (MissionType type : MissionType.values()) {
            if (type.id == id) {
                return type;
            }
        }
        return null;
    }
}