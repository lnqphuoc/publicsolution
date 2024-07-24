
package com.app.server.data.request.mission;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MissionSettingLimitRequest {
    @ApiModelProperty("Mức")
    private int level;
    @ApiModelProperty("Số lượng huy hiệu")
    private int value;
    @ApiModelProperty(value = "Hình ảnh", required = true)
    private String image;
    @ApiModelProperty("Danh sách ưu đãi")
    private List<MissionSettingLimitOfferRequest> offers;
}