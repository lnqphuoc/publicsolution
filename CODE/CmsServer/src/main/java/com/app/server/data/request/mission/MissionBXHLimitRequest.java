package com.app.server.data.request.mission;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.PromoConditionType;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MissionBXHLimitRequest {
    private int level;
    private List<MissionBXHLimitGroupRequest> groups = new ArrayList<>();

    public ClientResponse validate() {
        return ClientResponse.success(null);
    }
}