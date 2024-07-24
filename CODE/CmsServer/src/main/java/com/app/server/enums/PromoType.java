package com.app.server.enums;

import lombok.Getter;

@Getter
public enum PromoType {
    SALE_POLICY("SALE_POLICY", "Chính sách bán hàng", "CSBH"),
    PROMO("PROMOTION", "Chương trình trả ngay", "CTTN"),
    CTSS("CTSS", "Chương trình săn sale", "QUOTA"),
    CTTL("CTTL", "Chương trình tích lũy", "CTTL"),
    DAMME("DAMME", "Chính sách đam mê", "CSDM"),
    BXH("BXH", "Bảng xếp hạng", "CTXH"),
    NHIEM_VU_BXH("NHIEM_VU_BXH", "Nhiệm vụ bảng xếp hạng", "NVBXH"),
    ;

    private int id;
    private String key;
    private String label;
    private String code;

    PromoType(String key, String label, String code) {
        this.key = key;
        this.label = label;
        this.code = code;
    }

    public static PromoType from(String promo_type) {
        for (PromoType type : PromoType.values()) {
            if (type.getKey().equals(promo_type)) {
                return type;
            }
        }
        return null;
    }
}