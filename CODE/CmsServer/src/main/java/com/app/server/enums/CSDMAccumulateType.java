package com.app.server.enums;

import lombok.Getter;

@Getter
public enum CSDMAccumulateType {
    DON_HANG(1, "ORDER", "Đơn hàng"),
    HBTL(2, "HBTL", "Đơn hàng trả"),
    PHIEU_GIAM(3, "PHIEU_GIAM", "Phiếu điều chỉnh giảm"),
    PHIEU_TANG(4, "PHIEU_TANG", "Phiếu điều chỉnh tăng"),
    ;

    private int id;
    private String key;
    private String label;

    CSDMAccumulateType(int id, String key, String label) {
        this.id = id;
        this.key = key;
        this.label = label;
    }

    public static CSDMAccumulateType from(int id) {
        for (CSDMAccumulateType type : CSDMAccumulateType.values()) {
            if (type.getId() == id) {
                return type;
            }
        }

        return null;
    }

    public static CSDMAccumulateType fromKey(String key) {
        for (CSDMAccumulateType type : CSDMAccumulateType.values()) {
            if (type.getKey().equals(key)) {
                return type;
            }
        }

        return null;
    }
}