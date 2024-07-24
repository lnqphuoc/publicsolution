package com.app.server.data.entity;

import com.app.server.enums.SettingStatus;
import com.app.server.utils.JsonUtils;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import lombok.Data;
import org.json.simple.JSONObject;

import java.util.Date;

@Data
public class ProductPriceSettingTimerEntity {
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
    private String price_object_type;
    private int price_object_id;
    private String note;
    private Integer confirmer_id;

    public static ProductPriceSettingTimerEntity from(JSONObject js) {
        ProductPriceSettingTimerEntity entity = JsonUtils.DeSerialize(
                JsonUtils.Serialize(js), ProductPriceSettingTimerEntity.class
        );

        if (entity == null) {
            return null;
        }

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