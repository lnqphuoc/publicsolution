package com.app.server.enums;

import lombok.Getter;

@Getter
public enum MissionUnitType {
    VND(1, "VNĐ"),
    SP(2, "Sản phẩm");
    private int id;
    private String key;
    private String label;

    MissionUnitType(int id, String label) {
        this.id = id;
        this.key = "" + id;
        this.label = label;
    }

    public static MissionUnitType from(int id) {
        for (MissionUnitType type : MissionUnitType.values()) {
            if (type.id == id) {
                return type;
            }
        }
        return null;
    }
}