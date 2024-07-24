package com.app.server.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum CircleType {
    DATE(1, "DATE", "Kỳ"),
    YEAR(2, "YEAR", "Năm"),
    ;

    private int id;
    private String code;
    private String label;

    CircleType(int id, String code, String label) {
        this.id = id;
        this.code = code;
        this.label = label;
    }
}