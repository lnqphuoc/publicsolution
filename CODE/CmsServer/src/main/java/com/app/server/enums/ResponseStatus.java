package com.app.server.enums;

public enum ResponseStatus {
    LOGIN_EXPIRED(-1),
    FAIL(0),
    SUCCESS(1),
    EXCEPTION(2),
    NOT_PERMISSION(3);
    private final int value;

    private ResponseStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}