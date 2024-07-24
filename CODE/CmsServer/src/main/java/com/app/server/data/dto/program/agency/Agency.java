package com.app.server.data.dto.program.agency;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

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
    private int wardId;
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
    private Date lastCompletedOrderDate;
    private Integer blockCsbh;
    private Integer blockCtkm;
    private Integer blockCtsn;
    private Integer blockPrice;
    private Integer blockCttl;
    private Integer blockCtss;
    private Integer blockCsdm;

    public Agency() {
    }

    public boolean isBlockCsbh() {
        return (blockCsbh == 1);
    }

    public boolean isBlockCtkm() {
        return (blockCtkm == 1);
    }

    public boolean isBlockCtsn() {
        return (blockCtsn == 1);
    }

    public boolean isBlockPrice() {
        return (blockPrice == 1);
    }

    public boolean isBlockCttl() {
        return (blockCttl == 1);
    }

    public boolean isBlockCtss() {
        return (blockCtss == 1);
    }

    public boolean isBlockCsdm() {
        return (blockCsdm == 1);
    }
}