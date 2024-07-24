package com.app.server.enums;

import lombok.Getter;

@Getter
public enum PromoDamMeColorType {
    HAN_MUC_1("HAN_MUC_1", "#E1E1E1", 1),
    HAN_MUC_2("HAN_MUC_2", "#90EE90", 2),
    HAN_MUC_3("HAN_MUC_3", "#74CAF9", 3),
    HAN_MUC_4("HAN_MUC_4", "#EAA73B", 4),
    HAN_MUC_5("HAN_MUC_5", "#DE355A", 5),
    ;

    private int id;
    private String key;
    private String label;

    PromoDamMeColorType(String key, String label, int id) {
        this.id = id;
        this.key = key;
        this.label = label;
    }

    public static PromoDamMeColorType from(String offer_type) {
        for (PromoDamMeColorType type : PromoDamMeColorType.values()) {
            if (type.key.equals(offer_type)) {
                return type;
            }
        }
        return null;
    }
}