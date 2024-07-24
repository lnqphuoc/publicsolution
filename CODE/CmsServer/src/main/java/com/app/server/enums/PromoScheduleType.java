package com.app.server.enums;

import lombok.Getter;

@Getter
public enum PromoScheduleType {
    STOP("STOP", "STOP"),
    START("START", "START");
    private int id;
    private String key;
    private String label;

    PromoScheduleType(String key, String label) {
        this.key = key;
        this.label = label;
    }


    public static PromoScheduleType from(PromoScheduleType stop_type) {
        for (PromoScheduleType type : PromoScheduleType.values()) {
            if (type.key.equals(stop_type)) {
                return type;
            }
        }
        return null;
    }
}