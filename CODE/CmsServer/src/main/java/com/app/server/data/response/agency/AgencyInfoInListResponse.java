package com.app.server.data.response.agency;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgencyInfoInListResponse {
    private int id;
    private String code;
    private String shop_name;
    private String avatar;

    public AgencyInfoInListResponse(int id, String code, String shop_name, String avatar) {
        this.id = id;
        this.code = code;
        this.shop_name = shop_name;
        this.avatar = avatar;
    }
}