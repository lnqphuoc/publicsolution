package com.app.server.data.dto.agency;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Agency {
    private int id;
    private String code;
    private String avatar;
    @SerializedName("full_name")
    private String fullName;
    private String phone;
    private String email;
    private String shop_name;
    private String address;
    private String tax_number;
    @SerializedName("region_id")
    private int regionId;
    @SerializedName("city_id")
    private int cityId;
    @SerializedName("district_id")
    private int districtId;
    @SerializedName("ward_id")
    private Integer wardId;
    private int gender;
    private String birthday;
    private String password;
    @SerializedName("images")
    private String ltImage;
    private int status;
    @SerializedName("membership_id")
    private int membershipId;
    @SerializedName("business_department_id")
    private Integer businessDepartmentId;
    @SerializedName("business_type")
    private Integer businessType;
    @SerializedName("mainstay_industry_id")
    private Integer mainstayIndustryId;

    public Agency() {
    }
}