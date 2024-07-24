package com.app.server.data.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

@Data
public class WarehouseStockHistoryEntity {
    private Integer id;
    private Integer product_id;
    private Integer before_value = 0;
    private Integer change_value = 0;
    private Integer after_value = 0;
    private String type;
    private String note;
    private int status;
    private Integer creator_id;
    private Date created_date;
    private Date modified_date;
}