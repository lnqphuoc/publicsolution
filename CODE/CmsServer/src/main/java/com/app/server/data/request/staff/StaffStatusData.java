package com.app.server.data.request.staff;

import com.app.server.enums.Module;
import com.app.server.utils.JsonUtils;
import com.ygame.framework.common.LogUtil;
import lombok.Data;
import org.json.simple.JSONObject;

import java.util.List;

@Data
public class StaffStatusData {
    private String type = "ALL";
    private List<String> status;

    public static StaffStatusData from(String data) {
        try {
            return JsonUtils.DeSerialize(data, StaffStatusData.class);
        } catch (Exception e) {
            LogUtil.printDebug(Module.STAFF.getValue(), e);
        }
        return null;
    }
}