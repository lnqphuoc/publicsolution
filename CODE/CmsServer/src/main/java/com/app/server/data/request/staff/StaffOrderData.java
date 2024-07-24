package com.app.server.data.request.staff;

import lombok.Data;

import java.util.List;

@Data
public class StaffOrderData {
    private String type = "ALL";
    private List<String> status;

}