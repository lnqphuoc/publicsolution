package com.app.server.enums;

import lombok.Getter;

@Getter
public enum StaffStatus {
    DELETE(-1),
    WAITING(1),
    ACTIVATED(2),
    CANCEL(3);

    private int value;

    StaffStatus(int value) {
        this.value = value;
    }

    public static StaffStatus from(int value) {
        for (StaffStatus type : StaffStatus.values()) {
            if (type.value == value) {
                return type;
            }
        }
        return null;
    }
}