package com.app.server.enums;

import lombok.Getter;

@Getter
public enum WarehouseStatus {
    WAITING(1, "WAITING", "Chờ kích hoạt"),
    ACTIVATED(2, "ACTIVATED", "Đang hoạt động"),
    PENDING(3, "PENDING", "Tạm dừng"),
    ;

    private int id;
    private String code;
    private String label;

    WarehouseStatus(int id, String code, String label) {
        this.id = id;
        this.code = code;
        this.label = label;
    }
}