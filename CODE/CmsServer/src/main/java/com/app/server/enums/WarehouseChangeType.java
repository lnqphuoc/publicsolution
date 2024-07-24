package com.app.server.enums;

import lombok.Getter;

@Getter
public enum WarehouseChangeType {
    IMPORT(1, "IMPORT", "Nhập kho"),
    EXPORT(2, "EXPORT", "Xuất kho"),
    WAITING_APPROVE(3, "WAITING_APPROVE", "Chờ xác nhận"),
    WAITING_SHIP(3, "WAITING_SHIP", "Chờ giao");

    private int id;
    private String code;
    private String label;

    WarehouseChangeType(int id, String code, String label) {
        this.id = id;
        this.code = code;
        this.label = label;
    }
}