package com.app.server.data.request.mission;

import com.app.server.data.dto.mission.ApplyObjectRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class EditMissionBXHRequest {
    private int id;
    @ApiModelProperty("Thông tin cơ bản")
    protected MissionBXHInfoRequest info;
    @ApiModelProperty("Đối tượng áp dụng: CAP_BAC, TINH_THANH, DOANH_THU_THUAN_TU_DEN")
    protected ApplyObjectRequest apply_object;
    @ApiModelProperty("Hạng")
    protected List<MissionBXHLimitRequest> limits = new ArrayList<>();
    @ApiModelProperty("Ghi chú")
    private String note;
}