package com.app.server.enums;

import lombok.Getter;

@Getter
public enum ForceUpdateStatus {
    NONE(0),
    FORCE_LOGOUT(1);

    private int value;

    ForceUpdateStatus(int value) {
        this.value = value;
    }
}