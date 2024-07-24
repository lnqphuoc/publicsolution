package com.app.server.enums;

import lombok.Getter;

@Getter
public enum NotifyDisplayType {
    POPUP(1, "POPUP", "Popup"),
    NOTIFY(2, "NOTIFY", "Notify"),
    ;

    private int id;
    private String code;
    private String label;

    NotifyDisplayType(int id, String code, String label) {
        this.id = id;
        this.code = code;
        this.label = label;
    }

    public static NotifyDisplayType from(int id) {
        for (NotifyDisplayType type : NotifyDisplayType.values()) {
            if (type.id == id) {
                return type;
            }
        }
        return null;
    }
}