package com.app.server.enums;

public enum YesNoStatus {
    NO(0),
    YES(1);
    private final int value;

    private YesNoStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static YesNoStatus from(int value) {
        for (YesNoStatus type : YesNoStatus.values()) {
            if (type.value == value) {
                return type;
            }
        }
        return null;
    }
}