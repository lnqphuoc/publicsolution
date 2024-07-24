package com.app.server.data.request.warehouse;

import lombok.Data;

@Data
public class CancelWarehouseBillRequest {
    private int id;
    private String note;
}