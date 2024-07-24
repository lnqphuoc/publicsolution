package com.app.server.enums;

import lombok.Getter;

@Getter
public enum OrderErrorType {
    NONE(0, "SUCCESS", "Thỏa điều kiện"),
    NQH_CK(1, "NQH_CK", "Nợ quá hạn - Cam kết"),
    NQH_TT(2, "NQH_TT", "Nợ quá hạn - Thanh toán"),
    KHN_0(3, "KHN_0", "Kỳ hạn nợ: 0"),
    V_HMKD(4, "V_HMKD", "Vượt hạn mức khả dụng"),
    NQH(5, "NQH", "Nợ quá hạn"),
    GTTT(6, "GTTT", "Giá trị tối thiểu"),
    SLTT(7, "SLTT", "Sản phẩm không thỏa điều kiện mua hàng"),
    BUOC_NHAY(8, "BUOC_NHAY", "Bước nhảy"),
    TON_KHO(9, "TON_KHO", "Tồn kho");

    private int id;
    private String key;
    private String label;

    OrderErrorType(int id, String key, String label) {
        this.id = id;
        this.key = key;
        this.label = label;
    }


    public static StuckType getStuckTypeBy(OrderErrorType orderErrorType) {
        switch (orderErrorType) {
            case NQH_CK:
                return StuckType.NQH_CK;
            case NQH_TT:
                return StuckType.NQH_TT;
            case NQH:
                return StuckType.NQH;
            case KHN_0:
                return StuckType.KHN_0;
            case V_HMKD:
                return StuckType.V_HMKD;
            default:
                return StuckType.NONE;
        }
    }

    public static OrderErrorType from(int error_type) {
        for (OrderErrorType orderErrorType : OrderErrorType.values()) {
            if (orderErrorType.getId() == error_type) {
                return orderErrorType;
            }
        }
        return NONE;
    }
}