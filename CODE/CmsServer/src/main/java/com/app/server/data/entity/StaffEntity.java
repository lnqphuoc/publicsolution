package com.app.server.data.entity;

import com.app.server.enums.BannerStatus;
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
public class StaffEntity {
    private Integer id;
    private String code;
    private String phone;
    private String full_name;
    private String username;
    private String email;
    private String password;
    private String address;
    private int status = BannerStatus.WAITING.getId();
    private Date created_date = new Date();
    private Integer creator_id;
    private Date modified_date;
    private Integer modifier_id = 0;
    private int staff_group_permission_id;
    private int is_account_system;

    public static StaffEntity from(JSONObject js) {
        StaffEntity entity =
                JsonUtils.DeSerialize(
                        JsonUtils.Serialize(js), StaffEntity.class
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
        return entity;
    }
}