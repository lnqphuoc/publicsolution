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
public class DeptClearingEntity {
    private Integer id;
    private Integer dept_order_id;
    private Integer dept_transaction_id;

    /**
     * giá trị/số tiền phân bổ
     */
    private Long dept_clearing_value;

    /**
     * mã đơn hàng,...
     */
    private String dept_clearing_data;

    private Integer status = 1;

    private Date created_date;

    private Integer modifier_id;

    private Integer creator_id = 0;

    private Date modified_date;

    /**
     * ghi chú
     */
    private String note;

    public static DeptClearingEntity from(JSONObject js) {
        DeptClearingEntity entity = JsonUtils.DeSerialize(
                JsonUtils.Serialize(js), DeptClearingEntity.class
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
        return entity;
    }
}