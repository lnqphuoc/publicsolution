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
public class DeptSettingEntity {
    private Integer id;

    /**
     * trạng thái: 1:chờ duyệt, 2:đã duyệt, 3-kết thúc, -1:xóa
     */
    private Integer status;

    private Date created_date = DateTimeUtils.getNow();

    private Integer modifier_id;

    private Integer creator_id;

    private Date modified_date;

    /**
     * hạn mức nợ
     */
    private Long dept_limit;

    /**
     * hạn mức gối đầu
     */
    private Long ngd_limit;

    /**
     * kỳ hạn nợ
     */
    private Integer dept_cycle;

    private Date start_date;

    private Date end_date;
    private Date confirmed_date;
    private String agency_include = "[]";
    private String agency_ignore = "[]";
    private Integer agency_id;
    private String note = "";

    public static DeptSettingEntity from(JSONObject js) {
        DeptSettingEntity entity = JsonUtils.DeSerialize(
                JsonUtils.Serialize(js), DeptSettingEntity.class
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
        entity.setConfirmed_date(js.get("confirmed_date") == null ? null :
                DateTimeUtils.getDateTime(
                        ConvertUtils.toString(
                                js.get("confirmed_date")))
        );
        return entity;
    }
}