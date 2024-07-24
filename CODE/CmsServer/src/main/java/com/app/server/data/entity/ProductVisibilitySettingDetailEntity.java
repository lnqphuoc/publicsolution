package com.app.server.data.entity;

import com.app.server.enums.SettingStatus;
import com.app.server.utils.JsonUtils;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

@Getter
@Setter
public class ProductVisibilitySettingDetailEntity {
    private Integer id;
    private int creator_id;
    private Date created_date = DateTimeUtils.getNow();
    private Date modified_date;
    private Integer modifier_id;
    private Integer product_id;
    private Integer product_group_id;
    private Integer category_level_1_id;
    private Integer category_level_2_id;
    private Integer category_level_3_id;
    private Integer category_level_4_id;
    private Integer brand_id;
    private int status = SettingStatus.RUNNING.getId();
    private Date start_date;
    private Date end_date;
    private int visibility;
    private String visibility_data_type;
    private int visibility_data_id;
    private int product_visibility_setting_id;

    public static ProductVisibilitySettingDetailEntity from(JSONObject js) {
        ProductVisibilitySettingDetailEntity entity = JsonUtils.DeSerialize(
                JsonUtils.Serialize(js), ProductVisibilitySettingDetailEntity.class
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