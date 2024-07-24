package com.app.server.data.response.agency;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgencyInfoResponse {
    @JsonProperty("membership_id")
    private int membershipId;
    @JsonProperty("membership_name")
    private String membershipName;
    private int rank;
    private long aCoin;
    private String code;
    private String avatar;
    @JsonProperty("full_name")
    private String fullName;
    private String phone;
    private String email;
    private String address;
    @JsonProperty("tax_number")
    private String taxNumber;
    @JsonProperty("city_id")
    private int cityId;
    @JsonProperty("city_name")
    private String cityName;
    @JsonProperty("district_id")
    private int districtId;
    @JsonProperty("district_name")
    private String districtName;
    @JsonProperty("ward_id")
    private int wardId;
    @JsonProperty("ward_name")
    private String wardName;
    private int gender;
    private String birthday;
    private int status;
    @JsonProperty("business_type")
    private int business_type;
    @JsonProperty("business_department_id")
    private int business_department_id;
    @JsonProperty("mainstay_industry_id")
    private int mainstay_industry_id;
    @JsonProperty("image_url")
    private String imageUrl;
}