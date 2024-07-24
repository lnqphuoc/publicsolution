package com.app.server.data.entity;

import com.app.server.enums.BannerStatus;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

@Data
public class StaffManageDataEntity {
    private Integer id;
    private int status = 1;
    private int staff_id;
    private String agency_data;
    private String order_data;
}