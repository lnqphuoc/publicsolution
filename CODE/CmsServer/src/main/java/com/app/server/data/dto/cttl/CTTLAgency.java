package com.app.server.data.dto.cttl;

import com.app.server.data.dto.agency.AgencyBasicData;
import com.app.server.utils.JsonUtils;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;

import java.util.Date;

@Getter
@Setter
public class CTTLAgency {
    private int id;
    private int agency_id;
    private AgencyBasicData agency_info;
    private int program_id;
    private Date created_date;
    private int limit;

    public static CTTLAgency from(JSONObject js) {
        CTTLAgency entity = new CTTLAgency();
        entity.setId(ConvertUtils.toInt(js.get("id")));
        entity.setCreated_date(js.get("created_date") == null ? null :
                DateTimeUtils.getDateTime(
                        ConvertUtils.toString(
                                js.get("created_date")))
        );

        AgencyBasicData agency = new AgencyBasicData();
        agency.parseInfo(
                entity.agency_id,
                ConvertUtils.toString(
                        js.get("code")),
                ConvertUtils.toString(
                        js.get("shop_name")),
                ConvertUtils.toInt(js.get("membership_id")),
                ConvertUtils.toInt(js.get("business_department_id")),
                null,
                null,
                ConvertUtils.toString(
                        js.get("shop_name")),
                ConvertUtils.toString(
                        js.get("avatar"))
        );
        entity.setAgency_info(agency);
        return entity;
    }
}