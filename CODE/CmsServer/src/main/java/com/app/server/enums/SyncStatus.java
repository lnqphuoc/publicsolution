package com.app.server.enums;

import lombok.Getter;

@Getter
public enum SyncStatus {
    NO(0),
    SUCCESS(1),
    FAIL(2);
    private int value;

    SyncStatus(int value) {
        this.value = value;
    }
}