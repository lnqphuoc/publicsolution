package com.app.server.enums;

import lombok.Getter;

@Getter
public enum MissionTransactionType {
    DON_HANG(1, "ORDER", "Đơn hàng"),
    TANG_CONG_NO(2, "TANGCN", "Tăng công nợ"),
    //    DIEU_CHINH_DTT(3, "DTT", "Điều chỉnh DTT"),
    HBTL(4, "HBTL", "Hàng bán trả lại"),
    GIAM_CONG_NO(5, "GIAMCN", "Giảm công nợ"),
    ;

    private int id;
    private String key;
    private String label;

    MissionTransactionType(int id, String key, String label) {
        this.id = id;
        this.key = key;
        this.label = label;
    }

    public static MissionTransactionType from(int id) {
        for (MissionTransactionType type : MissionTransactionType.values()) {
            if (type.getId() == id) {
                return type;
            }
        }

        return null;
    }

    public static MissionTransactionType fromKey(String key) {
        for (MissionTransactionType type : MissionTransactionType.values()) {
            if (type.getKey().equals(key)) {
                return type;
            }
        }

        return null;
    }
}