package com.app.server.enums;

import lombok.Getter;

@Getter
public enum IncreaseDeptStatus {
    NO(0),
    YES(1);

    private int value;

    IncreaseDeptStatus(int value) {
        this.value = value;
    }
}