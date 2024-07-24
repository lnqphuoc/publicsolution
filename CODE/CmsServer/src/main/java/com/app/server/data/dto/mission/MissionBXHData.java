package com.app.server.data.dto.mission;

import com.app.server.constants.ResponseMessage;
import com.app.server.data.request.RepeatDataRequest;
import com.app.server.data.request.mission.MissionBXHInfoRequest;
import com.app.server.data.request.mission.MissionBXHLimitRequest;
import com.app.server.data.request.promo.*;
import com.app.server.enums.PromoConditionType;
import com.app.server.enums.PromoType;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class MissionBXHData {
    protected MissionBXHInfo info;
    protected ApplyObjectRequest apply_object;
    protected List<MissionBXHLimitRequest> limits = new ArrayList<>();
}