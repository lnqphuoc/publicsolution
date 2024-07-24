package com.app.server.data.entity;

import com.app.server.enums.PromoConditionType;
import com.app.server.enums.PromoType;
import com.app.server.utils.JsonUtils;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import lombok.Data;
import org.json.simple.JSONObject;

import javax.persistence.*;

import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

@Data
@Entity
@Table(name = "promo")
public class PromoEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;
    private String name;
    private String code;
    private String description;
    private String image;
    private String promo_type;
    private int status = 1;
    private Date created_date = new Date();
    private int use_limit;
    private Date start_date;
    private Date end_date;
    private String condition_type;
    private int is_automatic_allocation;
    private Integer creator_id;
    private Date modified_date = new Date();
    private String offer_info;
    private String promo_end_value_type;
    private Integer priority = 0;
    private Long promo_max_value;
    private int use_limit_per_agency;
    private int payment_duration;
    private int repeat_type;
    private int apply_for_private_price;
    private int show_at_hunt_sale;
    private String repeat_data;
    private String order_date_data;
    private String payment_date_data;
    private String reward_date_data;
    private String form_of_reward;
    private String show_on_tab;
    private int require_confirm_join;
    private String confirm_result_date_data;
    private int is_full_payment;
    private String hot_label;
    private String circle_type;
    private int agency_position_rank_limit;
    private int require_accumulate_value;
    private Integer business_department_id;

    public static PromoEntity from(JSONObject js) {
        PromoEntity entity = JsonUtils.DeSerialize(
                JsonUtils.Serialize(js), PromoEntity.class
        );
        entity.setCreated_date(js.get("created_date") == null ? null :
                DateTimeUtils.getDateTime(
                        ConvertUtils.toString(
                                js.get("created_date")))
        );
        entity.setModified_date(js.get("modified_date") == null ? null :
                DateTimeUtils.getDateTime(
                        ConvertUtils.toString(
                                js.get("modified_date")))
        );
        entity.setStart_date(js.get("start_date") == null ? null :
                DateTimeUtils.getDateTime(
                        ConvertUtils.toString(
                                js.get("start_date")))
        );
        entity.setEnd_date(js.get("end_date") == null ? null :
                DateTimeUtils.getDateTime(
                        ConvertUtils.toString(
                                js.get("end_date")))
        );

        return entity;
    }
}