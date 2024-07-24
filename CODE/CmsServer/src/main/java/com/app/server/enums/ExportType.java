package com.app.server.enums;

public enum ExportType {
    REPORT_INVENTORY(1, "Bao_cao_ton_kho"),
    REPORT_AGENCY(2, "Bao_cao_dai_ly"),
    REPORT_PRODUCT_PRICE_TIMER(3, "Cap_nhat_gia_chung"),
    EXPORT_ORDER(4, "Don_hang"),
    REPORT_ORDER_TEMP(5, "Don_hang_tam"),
    REPORT_TK_CTTL(6, "TK_CTTL"),
    REPORT_AGENCY_ACCESS_APP(7, "So_luong_truy_cap_app"),
    REPORT_WAREHOUSE_EXPORT_HISTORY(8, "Lich_su_xuat_kho"),
    REPORT_WAREHOUSE_IMPORT_HISTORY(9, "Lich_su_nhap_kho"),
    REPORT_PRODUCT_VISIBILITY(10, "San_pham_theo_an_hien"),
    REPORT_PRODUCT(11, "San_pham"),
    REPORT_PRODUCT_GROUP(12, "Nhom_hang"),
    REPORT_CATEGORY(13, "Danh_muc"),
    REPORT_CATALOG(14, "Catalog"),
    EXPORT_ORDER_CONFIRM_PRODUCT(15, "ocp");
    private int value;
    private String name;

    ExportType(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public static ExportType from(int value) {
        for (ExportType type : ExportType.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }

        return null;
    }
}