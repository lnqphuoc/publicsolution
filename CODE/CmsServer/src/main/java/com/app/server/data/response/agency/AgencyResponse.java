package com.app.server.data.response.agency;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgencyResponse {
    private int id;
    private String shop_name;
    private String code;
    private int status;
    private Integer revenue;
    private Integer a_coin;
    private Integer dept_limit;
}
