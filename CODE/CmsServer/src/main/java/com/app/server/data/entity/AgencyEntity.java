package com.app.server.data.entity;

import com.app.server.data.dto.agency.Membership;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import java.nio.channels.MembershipKey;

import static javax.persistence.GenerationType.IDENTITY;

@Getter
@Setter
public class AgencyEntity {
    private Integer id;
    private String code;
    private String avatar;
    private String full_name;
    private String phone;
    private String email;
    private String shop_name;
    private String address;
    private String tax_number;
    private int region_id;
    private int city_id;
    private int district_id;
    private int ward_id;
    private int gender;
    private String birthday;
    private String password;
    private String ltImage;
    private String images;
    private int status;
    private int membership_id;
    private Integer business_department_id;
    private Integer business_type;
    private Integer mainstay_industry_id;
    private Integer current_point = 0;
    private int membership_cycle_start_id = 1;
}