package com.app.server.enums;

import lombok.Getter;

@Getter
public enum MissionAgencyStatus {
    DELETE(-1, "Xóa"),
    RUNNING(2, "Chưa nhận"),
    FINISH(4, "Đã nhận"),
    REPLACED(5, "Đã đổi");
    private int id;
    private String key;
    private String label;

    MissionAgencyStatus(int id, String label) {
        this.id = id;
        this.key = "" + id;
        this.label = label;
    }

    public static MissionAgencyStatus from(int id) {
        for (MissionAgencyStatus type : MissionAgencyStatus.values()) {
            if (type.id == id) {
                return type;
            }
        }
        return null;
    }
}