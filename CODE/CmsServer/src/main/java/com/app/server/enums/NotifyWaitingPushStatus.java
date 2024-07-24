package com.app.server.enums;

import lombok.Getter;

@Getter
public enum NotifyWaitingPushStatus {
    WAITING(1, "WAITING", "Chưa gửi"),
    SENT(2, "SENT", "Đã gửi"),
    CANCEL(3, "CANCEL", "Hủy"),
    ;

    private int id;
    private String code;
    private String label;

    NotifyWaitingPushStatus(int id, String code, String label) {
        this.id = id;
        this.code = code;
        this.label = label;
    }
}