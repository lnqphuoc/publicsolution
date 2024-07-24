package com.app.server.data.dto.warehouse;

import lombok.Data;

@Data
public class WarehouseBasicData {
    private int id;
    private String code;
    private String name;
    private int warehouse_type_id;
    private int allow_sell;
}