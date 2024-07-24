package com.app.server.enums;

import lombok.Getter;

@Getter
public enum AgencyCatalogDetailStatus {
    WAITING(1, "WAITING", "Chờ duyệt"),
    APPROVED(2, "APPROVED", "Đã duyệt"),
    REJECT(3, "REJECT", "Từ chối");

    private int id;
    private String code;
    private String label;

    AgencyCatalogDetailStatus(int id, String code, String label) {
        this.id = id;
        this.code = code;
        this.label = label;
    }
}