package com.app.server.enums;

import lombok.Getter;

@Getter
public enum ProcessStatus {
    DELETE(-1),
    WAITING(0),
    INACTIVE(1),
    SUCCESS(2),
    FAIL(3),
    CANCEL(4);

    private int value;

    ProcessStatus(int value) {
        this.value = value;
    }

    public static ProcessStatus from(int value) {
        for (ProcessStatus type : ProcessStatus.values()) {
            if (type.value == value) {
                return type;
            }
        }
        return null;
    }
}