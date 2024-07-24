package com.app.server.enums;

import lombok.Getter;
import org.omg.PortableInterceptor.INACTIVE;

@Getter
public enum ActiveStatus {
    DELETE(-1),
    ACTIVATED(1),
    INACTIVE(0),
    APPROVE(2),
    CANCEL(3);

    private int value;

    ActiveStatus(int value) {
        this.value = value;
    }

    public static ActiveStatus from(int value) {
        for (ActiveStatus type : ActiveStatus.values()) {
            if (type.value == value) {
                return type;
            }
        }
        return null;
    }
}