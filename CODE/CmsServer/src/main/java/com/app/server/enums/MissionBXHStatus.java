package com.app.server.enums;

import lombok.Getter;

@Getter
public enum MissionBXHStatus {
    DELETE(-1, "Xóa"),
    DRAFT(0, "Nháp"),
    RUNNING(2, "Đang chạy"),
    STOP(3, "Kết thúc"),
    CANCEL(4, "Hủy");
    private int id;
    private String key;
    private String label;

    MissionBXHStatus(int id, String label) {
        this.id = id;
        this.key = "" + id;
        this.label = label;
    }

    public static MissionBXHStatus from(int id) {
        for (MissionBXHStatus type : MissionBXHStatus.values()) {
            if (type.id == id) {
                return type;
            }
        }
        return null;
    }
}