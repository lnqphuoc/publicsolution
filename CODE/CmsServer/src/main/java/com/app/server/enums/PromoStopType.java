package com.app.server.enums;

import lombok.Getter;

@Getter
public enum PromoStopType {
    STOP_NOW("STOP_NOW", "Dừng ngay"),
    STOP_SCHEDULE("STOP_SCHEDULE", "Hẹn dừng");
    private int id;
    private String key;
    private String label;

    PromoStopType(String key, String label) {
        this.key = key;
        this.label = label;
    }


    public static PromoStopType from(String stop_type) {
        for (PromoStopType type : PromoStopType.values()) {
            if (type.key.equals(stop_type)) {
                return type;
            }
        }
        return null;
    }
}