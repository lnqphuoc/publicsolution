package com.app.server.enums;

import lombok.Getter;

@Getter
public enum MissionGroupStatus {
    DELETE(-1, "Xóa"),
    DRAFT(0, "Nháp"),
    RUNNING(2, "Đang hoạt động"),
    STOP(3, "Ngưng hoạt động");
    private int id;
    private String key;
    private String label;

    MissionGroupStatus(int id, String label) {
        this.id = id;
        this.key = "" + id;
        this.label = label;
    }

    public static MissionGroupStatus from(int id) {
        for (MissionGroupStatus type : MissionGroupStatus.values()) {
            if (type.id == id) {
                return type;
            }
        }
        return null;
    }
}