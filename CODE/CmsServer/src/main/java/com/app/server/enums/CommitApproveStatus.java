package com.app.server.enums;

import lombok.Getter;

@Getter
public enum CommitApproveStatus {
    WAITING(0, "WAITING", "Chờ duyệt"),
    APPROVED(1, "APPROVED", "Đã duyệt"),
    REJECT(2, "REJECT", "Từ chối"),
    ;

    private int id;
    private String code;
    private String label;

    CommitApproveStatus(int id, String code, String label) {
        this.id = id;
        this.code = code;
        this.label = label;
    }
}