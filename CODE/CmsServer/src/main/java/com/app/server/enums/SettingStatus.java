package com.app.server.enums;

import lombok.Getter;

@Getter
public enum SettingStatus {
    DRAFT(1, "", "Nháp"),
    ACTIVE(2, "", "Đã kích hoạt"),
    RUNNING(3, "", "Đã áp dụng"),
    PENDING(4, "", "Tạm dừng");

    private int id;
    private String code;
    private String label;

    SettingStatus(int id, String code, String label) {
        this.id = id;
        this.code = code;
        this.label = label;
    }
}