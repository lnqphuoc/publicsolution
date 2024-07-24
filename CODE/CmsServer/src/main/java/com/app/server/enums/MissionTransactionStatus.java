package com.app.server.enums;

import lombok.Getter;

@Getter
public enum MissionTransactionStatus {
    CHUA_TICH_LUY(0, "Chưa tích lũy"),
    DA_TICH_LUY(1, "Đã tích lũy"),
    HUY_TICH_LUY(3, "Hủy tích lũy");
    private int id;
    private String key;
    private String label;

    MissionTransactionStatus(int id, String label) {
        this.id = id;
        this.key = "" + id;
        this.label = label;
    }

    public static MissionTransactionStatus from(int id) {
        for (MissionTransactionStatus type : MissionTransactionStatus.values()) {
            if (type.id == id) {
                return type;
            }
        }
        return null;
    }
}