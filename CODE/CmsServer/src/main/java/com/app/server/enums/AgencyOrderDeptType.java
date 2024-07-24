package com.app.server.enums;

import lombok.Getter;
import lombok.Setter;

@Getter
public enum AgencyOrderDeptType {
    NORMAL(1),
    HUNT_SALE(2);

    private int id;

    AgencyOrderDeptType(int id) {
        this.id = id;
    }
}