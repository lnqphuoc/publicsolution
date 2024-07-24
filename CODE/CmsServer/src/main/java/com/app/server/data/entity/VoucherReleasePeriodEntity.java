package com.app.server.data.entity;

import com.app.server.enums.BannerStatus;
import com.app.server.utils.JsonUtils;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import lombok.Data;
import org.json.simple.JSONObject;

import java.util.Date;

@Data
public class VoucherReleasePeriodEntity {
    private Integer id;
    private String code;
    private String name;
    private String image;
    private String offer_type;
    private String limit_data;
    private int status = BannerStatus.WAITING.getId();
    private Date created_date = new Date();
    private Integer creator_id;
    private Date modified_date;
    private Integer modifier_id = 0;
    private int staff_group_permission_id;
    private int is_account_system;
    private Date active_date;
    private int max_percent_per_order;

    public static VoucherReleasePeriodEntity from(JSONObject js) {
        VoucherReleasePeriodEntity entity =
                JsonUtils.DeSerialize(
                        JsonUtils.Serialize(js), VoucherReleasePeriodEntity.class
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
        entity.setActive_date(js.get("active_date") == null ? null :
                DateTimeUtils.getDateTime(
                        ConvertUtils.toString(
                                js.get("active_date")))
        );
        return entity;
    }
}