package com.app.server.enums;

import lombok.Getter;

@Getter
public enum LockSettingStatus {
    WAITING(1, "WAITING", "Chờ áp dụng"),
    RUNNING(2, "RUNNING", "Đã áp dụng"),
    CANCEL(3, "CANCEL", "Hủy"),
    DRAFT(4, "DRAFT", "Nháp");

    private int id;
    private String code;
    private String label;

    LockSettingStatus(int id, String code, String label) {
        this.id = id;
        this.code = code;
        this.label = label;
    }
}