
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
@Table(name = "promo_schedule")
public class PromoScheduleEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;
    private int promo_id;
    private int status = 1;
    private Date created_date = new Date();
    private Date schedule_time = new Date();
    private String schedule_type;
    private String note;
}