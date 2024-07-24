package com.app.server.enums;

import lombok.Getter;

@Getter
public enum DamMeDieuChinhType {
    HUY_TICH_LUY(1, "HUY_TICH_LUY", "Hủy tích lũy"),
    GIAM_GIA_TRI_SAN_PHAM(2, "GIAM_GIA_TRI_SAN_PHAM", "Giảm giá trị sản phẩm"),
    GIAM_GIA_TRI_DON_HANG(3, "GIAM_GIA_TRI_DON_HANG", "Giảm giá trị đơn hàng"),
    ;

    private int id;
    private String key;
    private String label;

    DamMeDieuChinhType(int id, String key, String label) {
        this.id = id;
        this.key = key;
        this.label = label;
    }

    public static DamMeDieuChinhType from(int id) {
        for (DamMeDieuChinhType type : DamMeDieuChinhType.values()) {
            if (type.getId() == id) {
                return type;
            }
        }

        return null;
    }

    public static DamMeDieuChinhType fromKey(String key) {
        for (DamMeDieuChinhType type : DamMeDieuChinhType.values()) {
            if (type.getKey().equals(key)) {
                return type;
            }
        }

        return null;
    }
}