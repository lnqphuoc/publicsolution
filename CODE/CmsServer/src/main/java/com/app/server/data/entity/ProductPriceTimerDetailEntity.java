package com.app.server.data.entity;

import com.app.server.enums.ProductPriceTimerStatus;
import com.app.server.utils.JsonUtils;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import io.swagger.models.auth.In;
import lombok.Data;
import org.json.simple.JSONObject;

import java.util.Date;

@Data
public class ProductPriceTimerDetailEntity {
    private Integer id;
    private Integer product_id;
    private Integer product_price_timer_id;
    private Long price;
    private String note = "";

    public static ProductPriceTimerDetailEntity from(JSONObject js) {
        ProductPriceTimerDetailEntity entity = JsonUtils.DeSerialize(
                JsonUtils.Serialize(js), ProductPriceTimerDetailEntity.class
        );

        if (entity == null) {
            return null;
        }

        return entity;
    }
}