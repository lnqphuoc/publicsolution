package com.app.server.data.dto.agency;

import com.app.server.data.entity.AgencyEntity;
import lombok.Data;

@Data
public class AgencyBasicData {
    private int id;
    private String shop_name;
    private String code;
    private Integer membership_id;
    private Integer business_department_id;
    private String phone;
    private String address;
    private String name;
    private String avatar;
    private Integer status;
    private Integer city_id;
    private String nick_name;

    public void initInfo(AgencyBasicData agencyCache) {
        this.id = agencyCache.getId();
        this.code = agencyCache.getCode();
        this.shop_name = agencyCache.getShop_name();
        this.membership_id = agencyCache.getMembership_id();
        this.business_department_id = agencyCache.getBusiness_department_id();
        this.phone = agencyCache.getPhone();
        this.address = agencyCache.getAddress();
        this.name = shop_name;
        this.avatar = agencyCache.getAvatar();
        this.status = agencyCache.getStatus();
    }

    public void initInfo(AgencyEntity agencyCache) {
        this.id = agencyCache.getId();
        this.code = agencyCache.getCode();
        this.shop_name = agencyCache.getShop_name();
        this.membership_id = agencyCache.getMembership_id();
        this.business_department_id = agencyCache.getBusiness_department_id();
        this.phone = agencyCache.getPhone();
        this.address = agencyCache.getAddress();
        this.name = shop_name;
        this.avatar = agencyCache.getAvatar();
        this.status = agencyCache.getStatus();
    }

    public void parseInfo(
            int id,
            String code,
            String shop_name,
            int membership_id,
            int business_department_id,
            String phone,
            String address,
            String name,
            String avatar
    ) {
        this.id = id;
        this.code = code;
        this.shop_name = shop_name;
        this.membership_id = membership_id;
        this.business_department_id = business_department_id;
        this.phone = phone;
        this.address = address;
        this.name = shop_name;
        this.avatar = avatar;
    }
}