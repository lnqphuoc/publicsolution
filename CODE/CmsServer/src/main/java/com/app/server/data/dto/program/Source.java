package com.app.server.data.dto.program;

public enum Source {
    APP(1),
    WEB(2);
    private final int value;

    private Source(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}