package com.app.server.enums;

import lombok.Getter;

@Getter
public enum PromoFormOfRewardType {
    BAC_THANG(1, "BAC_THANG", "Bậc thang"),
    MUC_CAO_NHAT(2, "MUC_CAO_NHAT", "Mức cao nhất");

    private int id;
    private String key;
    private String label;

    PromoFormOfRewardType(int id, String key, String label) {
        this.id = id;
        this.key = key;
        this.label = label;
    }

    public static PromoFormOfRewardType from(String condition_type) {
        for (PromoFormOfRewardType type : PromoFormOfRewardType.values()) {
            if (type.key.equals(condition_type)) {
                return type;
            }
        }

        return null;
    }
}