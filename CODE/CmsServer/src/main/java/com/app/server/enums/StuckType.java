package com.app.server.enums;

import lombok.Getter;

@Getter
public enum StuckType {
    NONE(0, "SUCCESS", "Thỏa điều kiện"),
    NQH_CK(1, "NQH_CK", "Nợ quá hạn - Cam kết"),
    NQH_TT(2, "NQH_TT", "Nợ quá hạn - Thanh toán"),
    KHN_0(3, "KHN_0", "Kỳ hạn nợ: 0"),
    V_HMKD(4, "V_HMKD", "Vượt hạn mức khả dụng"),
    NQH(5, "NQH", "Nợ quá hạn"),
    GTTT(6, "GTTT", "Không đạt giá trị tối thiểu"),
    SLTT(7, "SLTT", "Sản phẩm không thỏa điều kiện mua hàng");

    private int id;
    private String key;
    private String label;

    StuckType(int id, String key, String label) {
        this.id = id;
        this.key = key;
        this.label = label;
    }

    public static StuckType from(int stuck_type) {
        for (StuckType type : StuckType.values()) {
            if (type.getId() == stuck_type) {
                return type;
            }
        }
        return NONE;
    }

    public static StuckType from(String key) {
        for (StuckType type : StuckType.values()) {
            if (type.getKey().equals(key)) {
                return type;
            }
        }
        return NONE;
    }
}