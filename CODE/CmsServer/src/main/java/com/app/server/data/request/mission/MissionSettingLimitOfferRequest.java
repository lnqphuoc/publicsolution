
package com.app.server.data.request.mission;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MissionSettingLimitOfferRequest {
    private int offer_value;
    private String offer_type;
    private int vrp_id;
    private int expire_day;
}