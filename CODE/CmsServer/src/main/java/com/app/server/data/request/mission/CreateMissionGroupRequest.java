package com.app.server.data.request.mission;

import com.app.server.data.dto.mission.ApplyObjectRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CreateMissionGroupRequest {
    @ApiModelProperty(value = "Tên", required = true)
    protected String name;
    @ApiModelProperty(value = "Danh sách nhiệm vụ", required = false)
    private List<MissionRequest> missions;
}