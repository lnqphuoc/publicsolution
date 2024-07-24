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
public class WarehouseBillEntity {
    private Integer id;
    /**
     * mã phiếu
     */
    private String code;
    private Integer warehouse_id;
    private Integer warehouse_bill_type_id;
    private Integer warehouse_export_bill_type_id;
    /**
     * nguon
     */
    private String data;

    /**
     * ghi chú
     */
    private String note;

    /**
     * ghi chú
     */
    private String reason;

    /**
     * nguon
     */
    private Date confirmed_date;

    /**
     * order_code
     */
    private String order_code;

    /**
     * agency
     */
    private Integer agency_id;

    /**
     * xuất từ kho/nhập bởi kho
     */
    private Integer target_warehouse_id;

    /**
     * người xác nhận
     */
    private Integer confirmer_id;

    private Integer status = 1;

    private Date created_date;

    private Integer modifier_id;

    private Integer creator_id = 0;

    private Date modified_date;

    public static WarehouseBillEntity from(JSONObject js) {
        WarehouseBillEntity entity = JsonUtils.DeSerialize(
                JsonUtils.Serialize(js), WarehouseBillEntity.class
        );
        if (entity == null) {
            return null;
        }

        entity.setCreated_date(js.get("created_date") == null ? null :
                DateTimeUtils.getDateTime(
                        ConvertUtils.toString(
                                js.get("created_date")))
        );
        entity.setCreated_date(js.get("modified_date") == null ? null :
                DateTimeUtils.getDateTime(
                        ConvertUtils.toString(
                                js.get("modified_date")))
        );
        /**
         * confirmed_date
         */
        entity.setCreated_date(js.get("confirmed_date") == null ? null :
                DateTimeUtils.getDateTime(
                        ConvertUtils.toString(
                                js.get("confirmed_date")))
        );
        return entity;
    }
}