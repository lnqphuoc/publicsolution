package com.app.server.enums;

import lombok.Getter;
import org.omg.PortableInterceptor.NON_EXISTENT;

@Getter
public enum SettingType {
    CSBH_CTKM(1, "CSBH_CTKM", "CSBH hoặc CTKM"),
    LOAI_SAN_PHAM(2, "LOAI_SAN_PHAM", "Loại sản phẩm"),
    THUONG_HIEU(3, "THUONG_HIEU", "Thương hiệu"),
    MAT_HANG(4, "MAT_HANG", "Mặt hàng"),
    PHAN_LOAI_THEO_SP(5, "PHAN_LOAI_THEO_SP", "Phân loại theo sản phẩm"),
    PHAN_LOAI_THEO_THUONG_HIEU(6, "PHAN_LOAI_THEO_THUONG_HIEU", "Phân loại theo thương hiệu"),
    NHOM_HANG(7, "NHOM_HANG", "Nhóm hàng"),
    DANH_SACH_SAN_PHAM(8, "DANH_SACH_SAN_PHAM", "Danh sách sản phẩm"),
    SAN_PHAM(9, "SAN_PHAM", "Chi tiết 1 sản phẩm"),
    QUANG_BA(10, "QUANG_BA", "Quảng bá"),
    NHIEM_VU(11, "NHIEM_VU", "Nhiệm vụ"),
    BANG_THANH_TICH(12, "BANG_THANH_TICH", "Bảng thành tích");

    private int id;
    private String code;
    private String label;

    SettingType(int id, String code, String label) {
        this.id = id;
        this.code = code;
        this.label = label;
    }

    public static SettingType from(int value) {
        for (SettingType type : SettingType.values()) {
            if (type.getId() == value) {
                return type;
            }
        }
        return null;
    }

    public static SettingType from(String code) {
        for (SettingType type : SettingType.values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}