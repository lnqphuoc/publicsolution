package com.app.server.data.dto.visibility;

import com.app.server.data.entity.ProductVisibilitySettingEntity;
import com.app.server.enums.SettingStatus;
import com.app.server.utils.JsonUtils;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import lombok.Data;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class ProductVisibilitySetting {
    private Integer id;
    private String name;
    private int creator_id;
    private Date created_date;
    private Date modified_date;
    private Integer modifier_id;
    private Integer agency_id;
    private Integer city_id;
    private Integer region_id;
    private Integer membership_id;
    private int status = SettingStatus.DRAFT.getId();
    private Date start_date;
    private Date end_date;
    private String visibility_object_type;
    private int visibility_object_id;
    private List<ProductVisibilitySettingDetail> data = new ArrayList<>();


    public static ProductVisibilitySetting from(JSONObject js) {
        ProductVisibilitySetting entity = JsonUtils.DeSerialize(
                JsonUtils.Serialize(js), ProductVisibilitySetting.class
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

    public boolean checkRunning(String visibility_object_type, int visibility_object_id, Date date) {
        /**
         * AND t1.status = " + SettingStatus.RUNNING.getId()
         * AND t1.start_date <= NOW()
         * AND (t1.end_date is NULL OR t1.end_date >= NOW())" +
         */
        if (this.visibility_object_type.equals(visibility_object_type) &&
                this.visibility_object_id == visibility_object_id
        ) {
            return true;
        }
        return false;
    }

    public JSONObject checkDataRunning(
            Integer id,
            String visibility_data_type,
            int visibility_data_id,
            Date date) {
        ProductVisibilitySettingDetail productVisibilitySettingDetail =
                data.stream().filter(
                        x -> x.checkRunning(visibility_data_type, visibility_data_id, date) == true
                ).findFirst().orElse(null);
        if (productVisibilitySettingDetail == null) {
            return null;
        }

        return JsonUtils.DeSerialize(JsonUtils.Serialize(productVisibilitySettingDetail),
                JSONObject.class);
    }
}