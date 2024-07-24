package com.app.server.data.entity;

import com.app.server.enums.SettingStatus;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

@Data
@Entity
@Table(name = "product_price_setting_detail_history")
public class ProductPriceSettingDetailHistoryEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;
    private int creator_id;
    private Date created_date;
    private Integer modifier_id;
    private Date modified_date;
    private Integer product_id;
    private int status = SettingStatus.DRAFT.getId();
    private Date start_date;
    private Date end_date;
    private int product_price_setting_id;
    private String price_setting_type;
    private String price_data_type;
    private double price_setting_value;
    private int is_auto = 0;
    private int minimum_purchase = 0;
    private Long price_original = 0L;
    private Long price_new = 0L;
}