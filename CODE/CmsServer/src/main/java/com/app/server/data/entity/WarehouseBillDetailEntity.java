package com.app.server.data.entity;

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
public class WarehouseBillDetailEntity {
    private Integer id;
    private Integer warehouse_bill_id;
    private Integer product_id;
    private Integer product_quantity;
    /**
     * ghi chú
     */
    private String note;

    private Date created_date;

    public static WarehouseBillDetailEntity from(JSONObject js) {
        WarehouseBillDetailEntity entity = JsonUtils.DeSerialize(
                JsonUtils.Serialize(js), WarehouseBillDetailEntity.class
        );
        if (entity == null) {
            return null;
        }

        entity.setCreated_date(js.get("created_date") == null ? null :
                DateTimeUtils.getDateTime(
                        ConvertUtils.toString(
                                js.get("created_date")))
        );

        return entity;
    }
}