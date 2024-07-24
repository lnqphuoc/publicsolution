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
public class BannerEntity {
    private Integer id;
    private String name;
    private String image;
    private String description;
    private int status = BannerStatus.WAITING.getId();
    private Date created_date;
    private Integer creator_id = 0;
    private Date modified_date;
    private Integer modifier_id = 0;
    private Date start_date;
    private Date end_date;
    private String agency_ids = "[]";
    private String city_ids = "[]";
    private String region_ids = "[]";
    private String membership_ids = "[]";
    private String setting_type;
    private String setting_value;
    private int priority = 0;
    private String type = "IMAGE";
    

    public static BannerEntity from(JSONObject js) {
        BannerEntity entity = JsonUtils.DeSerialize(
                JsonUtils.Serialize(js), BannerEntity.class
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
        return entity;
    }
}