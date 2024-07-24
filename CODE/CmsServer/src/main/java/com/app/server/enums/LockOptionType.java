package com.app.server.enums;

import lombok.Getter;

@Getter
public enum LockOptionType {
    /**
     * Khóa ngay: 1, Khóa cuối ngày: 2, Khóa theo n ngày: 3, Không khóa: 4
     */
    KHOA_NGAY(1, "KHOA_NGAY", "Khóa ngay"),
    KHOA_CUOI_NGAY(2, "KHOA_CUOI_NGAY", "Khóa cuối ngày"),
    KHOA_N_NGAY(3, "KHOA_N_NGAY", "Khóa n ngày"),
    KHONG_KHOA(4, "KHONG_KHOA", "Không khóa");
    private int id;
    private String code;
    private String label;

    LockOptionType(int id, String code, String label) {
        this.id = id;
        this.code = code;
        this.label = label;
    }
}