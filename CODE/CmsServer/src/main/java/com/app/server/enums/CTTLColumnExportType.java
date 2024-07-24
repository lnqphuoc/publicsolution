package com.app.server.enums;

import lombok.Getter;

@Getter
public enum CTTLColumnExportType {
    /**
     * Ngày xuất
     * Mã CT:
     * Tên CT:
     * Điều kiện tham gia:
     * Hình thức ưu đãi:
     * Loại ưu đãi:
     * Thời gian bắt đầu đặt hàng:
     * Thời gian kết thúc đặt hàng:
     * Thời hạn thanh toán:
     * Thời gian kết thúc trả thưởng:
     * Mã đại lý
     * Tên đại lý
     * Ngày XN tham gia
     * SL XN ban đầu
     * SL đang hiển thị
     * SL/DTT tích lũy
     * Thanh toán hợp lệ
     * Hạn mức được hưởng
     * Giá trị ưu đãi được hưởng
     * Thanh toán còn thiếu
     * Phòng KD
     */
    NGAY_XUAT("NGAY_XUAT", "Ngày xuất", 1, 0),
    MA_CT("MA_CT", "Mã CT", 2, 0),
    TEN_CT("TEN_CT", "Tên CT", 3, 0),
    //* Hình thức ưu đãi:
    HINH_THUC_UU_DAI("HINH_THUC_UU_DAI", "Hình thức ưu đãi", 4, 0),
    //        * Loại ưu đãi:
    LOAI_UU_DAI("LOAI_UU_DAI", "Loại ưu đãi", 5, 0),
    //* Thời gian bắt đầu đặt hàng:
    THOI_GIAN_BAT_DAU_DAT_HANG("THOI_GIAN_BAT_DAU_DAT_HANG", "Thời gian bắt đầu đặt hàng", 6, 0),
    //* Thời gian kết thúc đặt hàng:
    THOI_GIAN_KET_THUC_DAT_HANG("THOI_GIAN_KET_THUC_DAT_HANG", "Thời gian kết thúc đặt hàng", 7, 0),
    //* Thời hạn thanh toán:
    THOI_HAN_THANH_TOAN("THOI_HAN_THANH_TOAN", "Thời hạn thanh toán", 8, 0),
    //* Thời gian kết thúc trả thưởng:
    THOI_GIAN_KET_THUC_TRA_THUONG("THOI_GIAN_KET_THUC_TRA_THUONG", "Thời gian kết thúc trả thưởng", 9, 0),
    //* Mã đại lý
    MA_DAI_LY("MA_DAI_LY", "Mã đại lý", 10, 1),
    //* Tên đại lý
    TEN_DAI_LY("TEN_DAI_LY", "Tên đại lý", 11, 1),
    //* Ngày XN tham gia
    NGAY_XN_THAM_GIA("NGAY_XN_THAM_GIA", "Ngày XN tham gia", 12, 1),
    //* SL XN ban đầu
    SL_XN_BAN_DAU("SL_XN_BAN_DAU", "SL XN ban đầu", 13, 1),
    //* SL đang hiển thị
    SL_DANG_HIEN_THI("SL_DANG_HIEN_THI", "SL đang hiển thị", 14, 1),
    //* SL Đại lý điều chỉnh
    SL_DAI_LY_DIEU_CHINH("SL_DAI_LY_DIEU_CHINH", "SL Đại lý điều chỉnh", 15, 1),
    //* SL/DTT tích lũy
    SL_DTT_TICH_LUY("SL_DTT_TICH_LUY", "SL/DTT tích lũy", 16, 1),
    //* Thanh toán hợp lệ
    THANH_TOAN_HOP_LE("THANH_TOAN_HOP_LE", "Thanh toán hợp lệ", 17, 1),
    //* Hạn mức được hưởng
    HAN_MUC_DUOC_HUONG("HAN_MUC_DUOC_HUONG", "Hạn mức được hưởng", 18, 1),
    //* Giá trị ưu đãi được hưởng
    GIA_TRI_UU_DAI_DUOC_HUONG("GIA_TRI_UU_DAI_DUOC_HUONG", "Giá trị ưu đãi được hưởng", 19, 1),
    //* Thanh toán còn thiếu
    THANH_TOAN_CON_THIEU("THANH_TOAN_CON_THIEU", "Thanh toán còn thiếu", 20, 1),
    //* Phòng KD
    PHONG_KD("PHONG_KD", "Phòng KD", 21, 1),
    DIEU_KIEN_THAM_GIA("DIEU_KIEN_THAM_GIA", "Điều kiện tham gia", 22, 0);

    private int id;
    private String key;
    private String label;
    private int level;

    CTTLColumnExportType(String key, String label, int id, int level) {
        this.id = id;
        this.key = key;
        this.label = label;
        this.level = level;
    }
}