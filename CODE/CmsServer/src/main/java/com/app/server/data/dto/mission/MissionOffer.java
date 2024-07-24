package com.app.server.data.dto.mission;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MissionOffer {
    private int level;
    private String image;
    private String offer_value;
    private String offer_type;
    private int expire_day;
}