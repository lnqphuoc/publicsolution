package com.app.server.enums;

import lombok.Getter;

@Getter
public enum NotifyType {
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
    DON_HANG(11, "DON_HANG", "Đơn hàng"), // nhấn vào chi tiết đơn hàng
    CONG_NO_TANG_GIAM(12, "CONG_NO_TANG_GIAM", "Công nợ"), // nhấn vào phần công nợ theo đơn hàng
    CONG_NO_THANH_TOAN(13, "CONG_NO_THANH_TOAN", "Thông báo có khoản thanh toán"), // nhấn vào lịch sử thanh toán
    CONG_NO_CHI_TIET(14, "CONG_NO_NO_QUA_HAN", "Thông báo nợ quá hạn"), // nhấn vào chi tiết công nợ
    DUYET_TAI_KHOAN(15, "DUYET_TAI_KHOAN", "Thông báo duyệt tài khoản"), // nhấn vào phần chi tiết tài khoản
    TAB_SAN_PHAM(17, "TAB_SAN_PHAM", "Thông báo duyệt mặt hàng"), // nhấn vào phần tab sản phẩm
    TAB_CAMKET(18, "TAB_CAMKET", "tab công nợ cam kết"), // nhấn vào phần tab công nợ cam kết
    NHIEM_VU(19, "NHIEM_VU", "Nhiệm vụ"),
    BANG_THANH_TICH(20, "BANG_THANH_TICH", "Bảng thành tích"),
    UU_DAI_CUA_TOI(21, "UU_DAI_CUA_TOI", "Ưu đãi của tui"); // nhấn vào ưu đãi của tôi;

    private int id;
    private String code;
    private String label;

    NotifyType(int id, String code, String label) {
        this.id = id;
        this.code = code;
        this.label = label;
    }

    public static NotifyType from(int value) {
        for (NotifyType type : NotifyType.values()) {
            if (type.getId() == value) {
                return type;
            }
        }
        return null;
    }

    public static NotifyType from(String code) {
        for (NotifyType type : NotifyType.values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}