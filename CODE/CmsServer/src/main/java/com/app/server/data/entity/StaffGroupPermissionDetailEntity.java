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
public class StaffGroupPermissionDetailEntity {
    private Integer id;
    private int status = 1;
    private int staff_group_permission_id;
    private int cms_action_id;
    private int allow;

    public static StaffGroupPermissionDetailEntity from(JSONObject js) {
        StaffGroupPermissionDetailEntity entity =
                JsonUtils.DeSerialize(
                        JsonUtils.Serialize(js), StaffGroupPermissionDetailEntity.class
                );
        return entity;
    }
}