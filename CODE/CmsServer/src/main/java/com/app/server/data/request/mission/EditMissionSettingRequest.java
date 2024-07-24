package com.app.server.data.request.mission;

import com.app.server.data.dto.mission.ApplyObjectRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EditMissionSettingRequest extends CreateMissionSettingRequest {
    @ApiModelProperty("ID")
    private int id;
}