package com.app.server.enums;

import lombok.Getter;

@Getter
public enum PermissionStatus {
    WAITING(1, "WAITING", "Chờ kích hoạt"),
    ACTIVATED(2, "ACTIVATED", "Đang hoạt động"),
    PENDING(3, "PENDING", "Tạm ngưng"),
    ;

    private int id;
    private String code;
    private String label;

    PermissionStatus(int id, String code, String label) {
        this.id = id;
        this.code = code;
        this.label = label;
    }
}