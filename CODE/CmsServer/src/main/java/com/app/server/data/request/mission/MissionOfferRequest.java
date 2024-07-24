
package com.app.server.data.request.mission;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MissionOfferRequest {
    @ApiModelProperty("Giá trị huy hiệu đảm bảo")
    private int require_value;
    @ApiModelProperty("Mức ưu đãi")
    private List<MissionSettingLimitRequest> limits;
}