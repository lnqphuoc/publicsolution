package com.app.server.data.request.warehouse;

import lombok.Data;

@Data
public class ApproveWarehouseBillRequest {
    private int id;
    private String note;
}