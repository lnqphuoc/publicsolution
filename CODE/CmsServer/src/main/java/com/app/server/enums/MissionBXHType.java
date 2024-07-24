package com.app.server.enums;

import lombok.Getter;

@Getter
public enum MissionBXHType {
    BANG_THANH_TICH_01(1, "Bảng thành tích 1"),
    BANG_THANH_TICH_02(2, "Bảng thành tích 2");
    private int id;
    private String key;
    private String label;

    MissionBXHType(int id, String label) {
        this.id = id;
        this.key = "" + id;
        this.label = label;
    }

    public static MissionBXHType from(int id) {
        for (MissionBXHType type : MissionBXHType.values()) {
            if (type.id == id) {
                return type;
            }
        }
        return null;
    }
}