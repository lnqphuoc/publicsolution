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
public class ProductVisibilitySettingDetailHistoryEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;
    private int creator_id;
    private Date created_date;
    private Date modified_date;
    private Integer modifier_id;
    private Integer product_id;
    private Integer product_group_id;
    private Integer category_level_1_id;
    private Integer category_level_2_id;
    private Integer category_level_3_id;
    private Integer category_level_4_id;
    private Integer brand_id;
    private int status = SettingStatus.DRAFT.getId();
    private Date start_date;
    private Date end_date;
    private int visibility;
    private String visibility_data_type;
    private int visibility_data_id;
    private int product_visibility_setting_id;
}