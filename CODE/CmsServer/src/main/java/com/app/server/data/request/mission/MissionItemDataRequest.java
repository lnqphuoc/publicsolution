package com.app.server.data.request.mission;

import com.app.server.constants.ProductConstants;
import com.app.server.constants.ResponseConstants;
import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ItemType;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MissionItemDataRequest {
    private String type;
    private List<ItemInfo> items;

    public ClientResponse validate() {
        if (type == null || !(type.equals(ProductConstants.ALL) || type.equals(ProductConstants.LIST))) {
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, type);
        }

        if (type.equals(ProductConstants.ALL)) {
            if (!(items == null || items.isEmpty())) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, items);
            }
        } else if (type.equals(ProductConstants.LIST)) {
            if (items == null || items.isEmpty()) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, items);
            }

            for (ItemInfo itemInfo : items) {
                ClientResponse crItemInfo = itemInfo.validate();
                if (crItemInfo.failed()) {
                    return crItemInfo;
                }
            }
        }
        return ResponseConstants.success;
    }
}