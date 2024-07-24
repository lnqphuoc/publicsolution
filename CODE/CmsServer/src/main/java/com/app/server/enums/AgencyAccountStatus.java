package com.app.server.enums;

public enum AgencyAccountStatus {
    INACTIVE(0),
    ACTIVE(1);
    private final int value;
    private AgencyAccountStatus(int value) {
        this.value = value;
    }
    public int getValue() {
        return this.value;
    }
}
