package com.app.server.enums;

import lombok.Getter;

@Getter
public enum LockDataStatus {
    RUNNING(3, "", "Đang áp dụng"),
    PENDING(4, "", "Tạm dừng");

    private int id;
    private String code;
    private String label;

    LockDataStatus(int id, String code, String label) {
        this.id = id;
        this.code = code;
        this.label = label;
    }
}