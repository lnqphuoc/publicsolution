package com.app.server.data.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

@Data
public class WarehouseInfoDateEntity {
    private Integer id;
    private Integer product_id;
    private Integer quantity_start_today = 0;
    private Integer quantity_import_today = 0;
    private Integer quantity_export_today = 0;
    private Integer quantity_waiting_approve_today = 0;
    private Integer quantity_waiting_ship_today = 0;
    private Integer quantity_end_today = 0;
    private Integer status = 1;
    private Date created_date;
}