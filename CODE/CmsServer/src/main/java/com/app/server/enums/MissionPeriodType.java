package com.app.server.enums;

import lombok.Getter;

@Getter
public enum MissionPeriodType {
    TUAN(1, "Nhiệm vụ tuần", "TUAN"),
    THANG(2, "Nhiệm vụ tháng", "THANG"),
    QUY(3, "Nhiệm vụ quý", "QUY");
    private int id;
    private String key;
    private String label;

    MissionPeriodType(int id, String label, String key) {
        this.id = id;
        this.key = key;
        this.label = label;
    }

    public static MissionPeriodType from(int id) {
        for (MissionPeriodType type : MissionPeriodType.values()) {
            if (type.id == id) {
                return type;
            }
        }
        return null;
    }
}