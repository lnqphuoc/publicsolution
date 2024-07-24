package com.app.server.enums;

import lombok.Getter;

@Getter
public enum AccountType {
    PRIMARY(1),
    SUB(0);

    private int value;

    AccountType(int value) {
        this.value = value;
    }
}