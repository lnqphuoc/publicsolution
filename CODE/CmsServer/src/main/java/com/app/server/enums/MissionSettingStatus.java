package com.app.server.enums;

import lombok.Getter;

@Getter
public enum MissionSettingStatus {
    DELETE(-1, "Xóa"),
    DRAFT(0, "Nháp"),
    WAITING(1, "Đã kích hoạt"),
    RUNNING(2, "Đang chạy"),
    STOP(3, "Kết thúc"),
    CANCEL(4, "Hủy");
    private int id;
    private String key;
    private String label;

    MissionSettingStatus(int id, String label) {
        this.id = id;
        this.key = "" + id;
        this.label = label;
    }

    public static MissionSettingStatus from(int id) {
        for (MissionSettingStatus type : MissionSettingStatus.values()) {
            if (type.id == id) {
                return type;
            }
        }
        return null;
    }
}