package com.app.server.enums;

import java.util.Arrays;

public enum RepeatType {
    NONE(0),
    DAILY(1),
    WEEKLY(2),
    MONTHLY(3);
    private final int value;

    private RepeatType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static RepeatType getByValue(int value) {
        return Arrays.stream(values()).filter(object -> object.value == value).findFirst().orElse(null);
    }
}