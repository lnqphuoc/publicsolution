package com.app.server.enums;

import lombok.Getter;

@Getter
public enum WarehouseBillType {
    IMPORT(1),
    EXPORT(2);
    private int value;

    private WarehouseBillType(int value) {
        this.value = value;
    }


    public static WarehouseBillType from(int warehouse_bill_type_id) {
        for (WarehouseBillType type : WarehouseBillType.values()) {
            if (type.getValue() == warehouse_bill_type_id) {
                return type;
            }
        }
        return null;
    }
}