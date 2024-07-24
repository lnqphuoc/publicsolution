package com.app.server.enums;

import lombok.Getter;

@Getter
public enum SettingObjectType {
    AGENCY(1, "AGENCY", "Đại lý"),
    CITY(2, "CITY", "Tỉnh thành"),
    REGION(3, "REGION", "Khu vực"),
    MEMBERSHIP(4, "MEMBERSHIP", "Cấp bậc");

    private int id;
    private String code;
    private String label;

    SettingObjectType(int id, String code, String label) {
        this.id = id;
        this.code = code;
        this.label = label;
    }

    public static SettingObjectType from(String visibility_data_type) {
        for (SettingObjectType type : SettingObjectType.values()) {
            if (type.getCode().equals(visibility_data_type)) {
                return type;
            }
        }
        return null;
    }
}