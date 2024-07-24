package com.app.server.data.entity;

import com.app.server.enums.BannerStatus;
import com.app.server.utils.JsonUtils;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import lombok.Data;
import org.json.simple.JSONObject;

import java.util.Date;

@Data
public class AgencyDealPriceRoundEntity {
    private Integer id;
    private int agency_deal_price_id;
    private Date request_delivery_date_cms;
    private Date request_delivery_date_app;
    private int deposit_percent_cms;
    private int deposit_percent_app;
    private int payment_duration_cms;
    private int payment_duration_app;
    private int complete_payment_duration_cms;
    private int complete_payment_duration_app;
    private int product_total_quantity_cms;
    private int product_total_quantity_app;
    private long product_price_cms;
    private long product_price_app;
    private String note_cms;
    private String note_app;
    private int round;
    private int creator_id;
    private int modifier_id;
    private int status;
    private Date created_date;
    private Date update_status_date;
    private Date modified_date;
    private Date updated_date_app;
    private Date updated_date_cms;

    public static AgencyDealPriceRoundEntity from(JSONObject js) {
        AgencyDealPriceRoundEntity entity = JsonUtils.DeSerialize(
                JsonUtils.Serialize(js), AgencyDealPriceRoundEntity.class
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
        entity.setUpdate_status_date(js.get("update_status_date") == null ? null :
                DateTimeUtils.getDateTime(
                        ConvertUtils.toString(
                                js.get("update_status_date")))
        );
        entity.setRequest_delivery_date_cms(js.get("request_delivery_date_cms") == null ? null :
                DateTimeUtils.getDateTime(
                        ConvertUtils.toString(
                                js.get("request_delivery_date_cms")))
        );
        entity.setRequest_delivery_date_app(js.get("request_delivery_date_app") == null ? null :
                DateTimeUtils.getDateTime(
                        ConvertUtils.toString(
                                js.get("request_delivery_date_app")))
        );
        return entity;
    }
}