package com.app.server.enums;

import lombok.Getter;

@Getter
public enum WarehouseExportBillType {
    XUAT_BAN(1, "Xuất bán"),
    CHUYEN_KHO(2, "Chuyển kho");
    private int value;
    private String label;

    private WarehouseExportBillType(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public WarehouseExportBillType from(int value) {
        for (WarehouseExportBillType type : WarehouseExportBillType.values()) {
            if (type.value == value) {
                return type;
            }
        }
        return null;
    }
}