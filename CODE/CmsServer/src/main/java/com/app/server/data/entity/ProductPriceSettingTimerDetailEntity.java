package com.app.server.data.entity;

import com.app.server.enums.SettingStatus;
import com.app.server.utils.JsonUtils;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import lombok.Data;
import org.json.simple.JSONObject;

import java.util.Date;

@Data
public class ProductPriceSettingTimerDetailEntity {
    private Integer id;
    private Integer product_id;
    private int product_price_setting_timer_id;
    private String note;
    private String data;
    private Date start_date;
    private Date end_date;
    private Integer status;

    public static ProductPriceSettingTimerDetailEntity from(JSONObject js) {
        ProductPriceSettingTimerDetailEntity entity = JsonUtils.DeSerialize(
                JsonUtils.Serialize(js), ProductPriceSettingTimerDetailEntity.class
        );

        if (entity == null) {
            return null;
        }

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