package com.app.server.data.entity;

import com.app.server.enums.ProductPriceTimerStatus;
import com.app.server.enums.SettingStatus;
import com.app.server.utils.JsonUtils;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import lombok.Data;
import org.json.simple.JSONObject;

import java.util.Date;

@Data
public class ProductPriceTimerEntity {
    private Integer id;
    private String name;
    private String note;
    private int creator_id;
    private Date created_date;
    private Date modified_date;
    private Integer modifier_id;
    private int status = ProductPriceTimerStatus.WAITING.getId();
    private Date start_date;
    private Integer confirmer_id;

    public static ProductPriceTimerEntity from(JSONObject js) {
        ProductPriceTimerEntity entity = JsonUtils.DeSerialize(
                JsonUtils.Serialize(js), ProductPriceTimerEntity.class
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
        return entity;
    }
}