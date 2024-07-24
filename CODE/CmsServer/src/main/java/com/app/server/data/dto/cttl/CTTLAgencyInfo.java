package com.app.server.data.dto.cttl;

import com.app.server.utils.JsonUtils;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;

import java.util.Date;

@Getter
@Setter
public class CTTLAgencyInfo {
    private int id;
    private int agency_id;
    private int program_id;
    private Date created_date;
    private String data;

    public CTTLAgencyInfo from(JSONObject js) {
        CTTLAgencyInfo entity = JsonUtils.DeSerialize(
                JsonUtils.Serialize(js),
                CTTLAgencyInfo.class
        );
        if (entity == null) {
            return null;
        }

        entity.setCreated_date(js.get("created_date") == null ? null :
                DateTimeUtils.getDateTime(
                        ConvertUtils.toString(
                                js.get("created_date")))
        );
        return entity;
    }
}