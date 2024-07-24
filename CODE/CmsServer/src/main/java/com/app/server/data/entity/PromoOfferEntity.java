package com.app.server.data.entity;

import com.app.server.enums.PromoOfferType;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

@Data
@Entity
@Table(name = "promo_offer")
public class PromoOfferEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;
    private int promo_limit_id;
    private int promo_limit_group_id;
    private Long offer_value;
    private String offer_type;
    private Double conversion_ratio;
    private int status;
    private Date created_date = new Date();
    private Integer modifier_id;
    private Integer creator_id;
    private Date modify_date = new Date();
    private int promo_id;
    private String voucher_data;
}