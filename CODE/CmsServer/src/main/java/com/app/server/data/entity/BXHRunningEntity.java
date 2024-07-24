package com.app.server.data.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

@Data
@Entity
@Table(name = "bxh_running")
public class BXHRunningEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;
    private String promo_data;
    private int promo_id;
    private int status = 1;
    private Date created_date = new Date();
    private String agency_data;
}