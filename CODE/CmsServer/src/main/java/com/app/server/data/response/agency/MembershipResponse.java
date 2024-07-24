package com.app.server.data.response.agency;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MembershipResponse {
    private int id;
    private String code;
    private String name;
    private double money_require;
}
