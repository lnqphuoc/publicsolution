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
public class DeptTransactionEntity {
    private Integer id;

    private Integer dept_transaction_sub_type_id;

    private Integer dept_transaction_main_type_id;

    private Integer dept_type_id;

    private String cn_effect_type;

    private String dtt_effect_type;

    private String tt_effect_type;

    private String acoin_effect_type;

    private Long transaction_value;

    private Integer agency_id;

    private Long dept_cycle_end;

    private String dept_type_data;

    private String note;

    private Integer status;

    private Date created_date;

    private Integer modifier_id;

    private String dept_function_type;

    private Integer creator_id;

    private Date modified_date;

    private Date dept_time;

    private Date confirmed_time;

    private String description;

    private Long acoin = 0L;

    private Long transaction_used_value = 0L;
    private String doc_no;
    private String source;

    public static DeptTransactionEntity from(JSONObject js) {
        DeptTransactionEntity entity = JsonUtils.DeSerialize(
                JsonUtils.Serialize(js), DeptTransactionEntity.class
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
        entity.setDept_time(js.get("dept_time") == null ? null :
                DateTimeUtils.getDateTime(
                        ConvertUtils.toString(
                                js.get("dept_time")))
        );
        entity.setConfirmed_time(js.get("confirmed_time") == null ? null :
                DateTimeUtils.getDateTime(
                        ConvertUtils.toString(
                                js.get("confirmed_time")))
        );
        return entity;
    }
}