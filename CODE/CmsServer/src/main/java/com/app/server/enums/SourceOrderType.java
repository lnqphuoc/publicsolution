package com.app.server.enums;

import lombok.Getter;

@Getter
public enum SourceOrderType {
    APP(1, "APP"),
    CMS(2, "CMS");
    private int value;
    private String label;

    SourceOrderType(int value, String label) {
        this.value = value;
        this.label = label;
    }
}