package com.app.server.enums;

import lombok.Getter;

@Getter
public enum PromoStructureType {
    LIEN_KET(1, "LIEN_KET", "Liên kết"),
    DONG_THOI(2, "DONG_THOI", "Đồng thời"),
    DONG_THOI_TRU_GIA_TRI_DA_TINH_THUONG(3, "DONG_THOI_TRU_GIA_TRI_DA_TINH_THUONG", "Đồng thời trừ giá trị đã tính thưởng"),
    LOAI_TRU(4, "LOAI_TRU", "Loại trừ");

    private int id;
    private String key;
    private String label;

    PromoStructureType(int id, String key, String label) {
        this.id = id;
        this.key = key;
        this.label = label;
    }

    public static PromoStructureType from(String condition_type) {
        for (PromoStructureType type : PromoStructureType.values()) {
            if (type.key.equals(condition_type)) {
                return type;
            }
        }

        return null;
    }
}