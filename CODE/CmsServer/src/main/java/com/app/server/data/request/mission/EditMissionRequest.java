package com.app.server.data.request.mission;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EditMissionRequest {
    @ApiModelProperty(value = "ID Nhóm", required = true)
    protected int mission_group_id;
    @ApiModelProperty(value = "Danh sách nhiệm vụ", required = false)
    private List<MissionRequest> missions;
}