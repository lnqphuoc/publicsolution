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
@Table(name = "promo_history")
public class PromoHistoryEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;
    private String promo_data;
    private int promo_id;
    private int status = 1;
    private String note;
    private Date created_date = new Date();
    private Integer creator_id = 0;
    private Date start_date;
    private Date end_date;
}