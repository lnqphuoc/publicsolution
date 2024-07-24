package com.app.server.data.dto.mission;

import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;

import java.util.List;

@Getter
@Setter
public class UpdateMissionConfigRequest {
    private JSONObject data;
}