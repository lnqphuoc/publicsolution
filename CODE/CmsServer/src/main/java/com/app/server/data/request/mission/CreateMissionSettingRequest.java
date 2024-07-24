package com.app.server.data.request.mission;

import com.app.server.data.dto.mission.ApplyObjectRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateMissionSettingRequest {
    @ApiModelProperty(value = "Tên thiết lập", required = true)
    protected String name;
    @ApiModelProperty(value = "Nhóm nhiệm vụ", required = true)
    protected int mission_group_id;
    @ApiModelProperty(value = "Bộ lọc", required = true)
    protected ApplyObjectRequest apply_object;
    @ApiModelProperty(value = "Bộ lọc", required = true)
    protected GenerateRateData generate_rate_data;
    @ApiModelProperty(value = "Mức ưu đãi", required = true)
    protected MissionOfferRequest offer_data;
    @ApiModelProperty(value = "Thời gian bắt đầu", required = true)
    private long start_date_millisecond;
}