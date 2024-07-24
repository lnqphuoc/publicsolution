package com.app.server.data.request.mission;

import com.app.server.data.dto.mission.ApplyObjectRequest;
import com.app.server.data.request.promo.PromoInfoRequest;
import com.app.server.data.request.promo.PromoLimitRequest;
import com.app.server.response.ClientResponse;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CreateMissionBXHRequest {
    @ApiModelProperty("Thông tin cơ bản")
    protected MissionBXHInfoRequest info;
    @ApiModelProperty("Đối tượng áp dụng: CAP_BAC, TINH_THANH, DOANH_THU_THUAN_NAM_TRUOC")
    protected ApplyObjectRequest apply_object;
    @ApiModelProperty("Hạng")
    protected List<MissionBXHLimitRequest> limits = new ArrayList<>();
}