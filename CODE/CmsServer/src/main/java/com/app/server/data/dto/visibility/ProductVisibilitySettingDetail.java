package com.app.server.data.dto.visibility;

import com.app.server.data.entity.ProductVisibilitySettingDetailEntity;
import com.app.server.enums.SettingStatus;
import com.app.server.utils.JsonUtils;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;

import java.util.Date;

@Getter
@Setter
public class ProductVisibilitySettingDetail {
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

    public static ProductVisibilitySettingDetail from(JSONObject js) {
        ProductVisibilitySettingDetail entity = JsonUtils.DeSerialize(
                JsonUtils.Serialize(js), ProductVisibilitySettingDetail.class
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

    public boolean checkRunning(String visibility_data_type, int visibility_data_id, Date date) {
        if (this.visibility_data_type.equals(visibility_data_type) &&
                this.visibility_data_id == visibility_data_id
        ) {
            return true;
        }
        return false;
    }
}