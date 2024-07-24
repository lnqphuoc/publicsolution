package com.app.server.enums;

public enum DataStatus {
    DEACTIVATE(0),
    ACTIVE(1);
    private final int value;

    private DataStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}