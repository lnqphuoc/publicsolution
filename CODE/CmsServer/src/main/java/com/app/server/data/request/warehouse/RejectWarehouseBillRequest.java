package com.app.server.data.request.warehouse;

import lombok.Data;

@Data
public class RejectWarehouseBillRequest {
    private int id;
    private String note;
}