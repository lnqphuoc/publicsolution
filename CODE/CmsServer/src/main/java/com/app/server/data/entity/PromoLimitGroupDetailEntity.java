package com.app.server.data.entity;

import com.app.server.enums.PromoConditionType;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

@Data
@Entity
@Table(name = "promo_limit_group_detail")
public class PromoLimitGroupDetailEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;
    private int promo_limit_group_id;
    private int product_id;
    private int type = 2;
    private int status;
    private Date created_date = new Date();
    private Integer modifier_id;
    private Integer creator_id;
    private Date modify_date = new Date();
    private int promo_id;
}