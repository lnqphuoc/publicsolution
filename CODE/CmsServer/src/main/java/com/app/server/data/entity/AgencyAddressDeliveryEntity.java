package com.app.server.data.entity;

import com.app.server.utils.JsonUtils;
import lombok.Data;
import org.json.simple.JSONObject;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import static javax.persistence.GenerationType.IDENTITY;

@Data
public class AgencyAddressDeliveryEntity {
    private Integer id;
    private int agency_id;
    private String full_name;
    private String phone;
    private String address;
    private String truck_number;
    private int is_default;

    public static AgencyAddressDeliveryEntity from(JSONObject js) {
        AgencyAddressDeliveryEntity entity = JsonUtils.DeSerialize(
                JsonUtils.Serialize(js), AgencyAddressDeliveryEntity.class
        );

        return entity;
    }
}