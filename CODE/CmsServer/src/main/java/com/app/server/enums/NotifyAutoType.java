package com.app.server.enums;

import lombok.Getter;

@Getter
public enum NotifyAutoType {
    ORDER(1, "ORDER", "Đơn hàng"),
    ACCOUNT(2, "ACCOUNT", "Tài khoản"),
    ;

    private int id;
    private String code;
    private String label;

    NotifyAutoType(int id, String code, String label) {
        this.id = id;
        this.code = code;
        this.label = label;
    }
}