package com.app.server.data.entity;

import com.app.server.enums.SettingStatus;
import com.app.server.utils.JsonUtils;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import lombok.Data;
import org.json.simple.JSONObject;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

@Data
public class ProductPriceSettingDetailEntity {
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
    private int is_auto = 1;
    private int minimum_purchase = 0;
    private Long price_original = 0L;
    private Long price_new = 0L;

    public static ProductPriceSettingDetailEntity from(JSONObject js) {
        ProductPriceSettingDetailEntity entity = JsonUtils.DeSerialize(
                JsonUtils.Serialize(js), ProductPriceSettingDetailEntity.class
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