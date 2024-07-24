package com.app.server.enums;

import lombok.Getter;

@Getter
public enum Module {
    AUTH("AUTH"),
    AGENCY("AGENCY"),
    PRODUCT("PRODUCT"),
    ORDER("ORDER"),
    DEPT("DEPT"),
    PROMO("PROMO"),
    WAREHOUSE("WAREHOUSE"),
    STAFF("STAFF"),
    ACOIN("ACOIN"),
    REPORT("REPORT"),
    BANNER("BANNER"),
    NOTIFY("NOTIFY"),
    UTILITY("UTILITY"),
    DEAL("DEAL"),
    MISSION("MISSION"),
    ;

    private String value;

    Module(String value) {
        this.value = value;
    }
}