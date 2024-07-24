package com.app.server.enums;

import lombok.Getter;

@Getter
public enum VisibilityType {

    SHOW(1, "", "Hiện"),
    HIDE(2, "", "Ẩn");

    private int id;
    private String code;
    private String label;

    VisibilityType(int id, String code, String label) {
        this.id = id;
        this.code = code;
        this.label = label;
    }

    public static VisibilityType from(int visibility) {
        for (VisibilityType type : VisibilityType.values()) {
            if (type.getId() == visibility) {
                return type;
            }
        }
        return null;
    }
}