package com.app.server.data.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

@Data
public class AgencyAcoinHistoryEntity {
    private Integer id;
    private int agency_id;
    private Date created_date;
    private long point;
    private long old_point;
    private long current_point;
    private String note;
}