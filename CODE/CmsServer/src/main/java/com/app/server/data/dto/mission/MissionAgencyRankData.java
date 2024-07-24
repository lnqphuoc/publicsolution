package com.app.server.data.dto.mission;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MissionAgencyRankData {
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
    private int rank;
}