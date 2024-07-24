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
public class AddressExportBillingEntity {
    private Integer id;
    private int agency_id;
    private String address;
    private String billing_label;
    private String billing_name;
    private String email;
    private String tax_number;
    private int is_default;

    public static AddressExportBillingEntity from(JSONObject js) {
        AddressExportBillingEntity entity = JsonUtils.DeSerialize(
                JsonUtils.Serialize(js), AddressExportBillingEntity.class
        );

        return entity;
    }
}