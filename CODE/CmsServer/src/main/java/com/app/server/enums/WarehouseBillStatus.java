package com.app.server.enums;

import lombok.Getter;

@Getter
public enum WarehouseBillStatus {
    DRAFT(1, "WAITING", "Nháp"),
    WAITING(2, "WAITING", "Chờ xác nhận"),
    CONFIRMED(3, "CONFIRMED", "Hoàn thành"),
    CANCEL(4, "CANCEL", "Hủy"),
    ;

    private int id;
    private String code;
    private String label;

    WarehouseBillStatus(int id, String code, String label) {
        this.id = id;
        this.code = code;
        this.label = label;
    }
}