package com.app.server.enums;

import lombok.Getter;

@Getter
public enum MissionStatus {
    DELETE(-1, "Xóa"),
    RUNNING(2, "Đang chạy"),
    CANCEL(4, "Hủy");
    private int id;
    private String key;
    private String label;

    MissionStatus(int id, String label) {
        this.id = id;
        this.key = "" + id;
        this.label = label;
    }

    public static MissionStatus from(int id) {
        for (MissionStatus type : MissionStatus.values()) {
            if (type.id == id) {
                return type;
            }
        }
        return null;
    }
}