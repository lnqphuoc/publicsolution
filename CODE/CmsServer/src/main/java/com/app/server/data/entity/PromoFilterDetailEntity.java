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
@Table(name = "promo_filter_detail")
public class PromoFilterDetailEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;
    private int promo_id;
    private int promo_filter_id;
    private String filter_data;
    private String filter_type;
    private int status;
    private Date created_date = new Date();
    private Integer modifier_id;
    private Integer creator_id;
    private Date modify_date = new Date();
}