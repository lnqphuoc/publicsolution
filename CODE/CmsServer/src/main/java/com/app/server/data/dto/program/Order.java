package com.app.server.data.dto.program;

import lombok.Data;

import java.util.Date;

@Data
public class Order {
    private int id;
    private String code;
    private Date confirmDeliveryDate;
}