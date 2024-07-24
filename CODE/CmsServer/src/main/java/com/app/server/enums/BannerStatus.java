package com.app.server.enums;

import lombok.Getter;

@Getter
public enum BannerStatus {
    WAITING(1, "WAITING", "Chờ hiển thị"),
    ACTIVATED(2, "ACTIVATED", "Đang hiển thị"),
    PENDING(3, "PENDING", "Không hiển thị"),
    ;

    private int id;
    private String code;
    private String label;

    BannerStatus(int id, String code, String label) {
        this.id = id;
        this.code = code;
        this.label = label;
    }
}