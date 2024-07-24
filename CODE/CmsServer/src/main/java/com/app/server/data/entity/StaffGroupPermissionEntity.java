package com.app.server.data.entity;

import com.app.server.enums.BannerStatus;
import com.app.server.enums.PermissionStatus;
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
public class StaffGroupPermissionEntity {
    private Integer id;
    private String name;
    private int status = PermissionStatus.WAITING.getId();
    private Date created_date;
    private Integer creator_id;
    private Date modified_date;
    private Integer modifier_id = 0;
    private int full_permission = 0;

    public static StaffGroupPermissionEntity from(JSONObject js) {
        StaffGroupPermissionEntity entity =
                JsonUtils.DeSerialize(
                        JsonUtils.Serialize(js), StaffGroupPermissionEntity.class
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