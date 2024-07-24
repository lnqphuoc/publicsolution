package com.app.server.enums;

import lombok.Getter;

@Getter
public enum CommitStatus {
    WAITING(1, "WAITING", "Chưa hoàn thành"),
    FINISH(2, "FINISH", "Hoàn thành"),
    CANCEL(3, "CANCEL", "Hủy"),
    ;

    private int id;
    private String code;
    private String label;

    CommitStatus(int id, String code, String label) {
        this.id = id;
        this.code = code;
        this.label = label;
    }
}