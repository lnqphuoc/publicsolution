package com.app.server.data.request.mission;

import com.app.server.constants.ResponseConstants;
import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ItemType;
import com.app.server.enums.MissionProductType;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemInfo {
    private int item_id;
    private String note = "";
    private String item_code = "";
    private String item_name = "";
    private String item_type = "";
    private int category_level;

    public ClientResponse validate() {
        MissionProductType missionProductType = MissionProductType.fromKey(item_type);
        if (missionProductType == null) {
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, MissionProductType.class);
        }

        if (item_id <= 0) {
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, item_id);
        }
        return ResponseConstants.success;
    }
}