package com.app.server.enums;

import lombok.Getter;

@Getter
public enum CategoryLevel {
    NGANH_HANG(1, "Ngành hàng", 2),
    MAT_HANG(2, "Mặt hàng", 2),
    PHAN_LOAI_HANG(3, "Phân loại", 2),
    PHAN_LOAI_HANG_THEO_THUONG_HIEU(4, "Phân loại theo thương hiệu", 3);

    private int key;
    private String value;
    private int number;

    CategoryLevel(int key, String value, int number) {
        this.key = key;
        this.value = value;
        this.number = number;
    }

    public static int isBranch(int key) {
        if (key == CategoryLevel.NGANH_HANG.getKey()) {
            return 1;
        }
        return 0;
    }

    public static CategoryLevel from(int categoryLevel) {
        for (CategoryLevel level : CategoryLevel.values()) {
            if (level.key == categoryLevel) {
                return level;
            }
        }
        return null;
    }
}