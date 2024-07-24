package com.app.server.data.dto.mission;

import com.app.server.utils.JsonUtils;
import com.ygame.framework.utils.ConvertUtils;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;

import java.util.List;

@Getter
@Setter
public class MissionTransactionData {
    private int id;
    private int orderId;
    private String code;
    private String type;
    private long totalValue;
    private Long createdTime;
    private List<MissionChangedTransactionData> ltChangedTransaction;
    private int status;
    private boolean isAdd;

    public static MissionTransactionData from(JSONObject js) {
        MissionTransactionData missionTransactionData = JsonUtils.DeSerialize(
                JsonUtils.Serialize(js), MissionTransactionData.class
        );
        missionTransactionData.isAdd = ConvertUtils.toBoolean(js.get("isAdd"));
        return missionTransactionData;
    }
}