package com.app.server.data.entity;

import com.app.server.utils.AppUtils;
import com.app.server.utils.JsonUtils;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class AgencyLockSettingEntity {
    private int id;
    private String code;
    @ApiModelProperty(name = "Loại đối tượng")
    private String setting_object_type;
    @ApiModelProperty(name = "ID đối tượng")
    private String setting_object_data;
    @ApiModelProperty(name = "Khóa ngay: 1, Khóa cuối ngày: 2, Khóa theo n ngày: 3, Không khóa: 4")
    private int option_lock;
    @ApiModelProperty(name = "Số ngày")
    private int day_lock;
    private int status;
    private Date start_date;
    private int creator_id;

    public static AgencyLockSettingEntity from(JSONObject js) {
        AgencyLockSettingEntity entity = JsonUtils.DeSerialize(
                JsonUtils.Serialize(js),
                AgencyLockSettingEntity.class
        );

        entity.setStart_date(AppUtils.convertJsonToDate(js.get("start_date")));
        return entity;
    }
}