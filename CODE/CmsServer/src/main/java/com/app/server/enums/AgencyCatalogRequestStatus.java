package com.app.server.enums;

import lombok.Getter;

@Getter
public enum AgencyCatalogRequestStatus {
    WAITING(1, "WAITING", "Chờ xử lý"),
    PROCESSING(2, "PROCESSING", "Đang xử lý"),
    FINISH(3, "FINISH", "Đã xử lý");

    private int id;
    private String code;
    private String label;

    AgencyCatalogRequestStatus(int id, String code, String label) {
        this.id = id;
        this.code = code;
        this.label = label;
    }
}