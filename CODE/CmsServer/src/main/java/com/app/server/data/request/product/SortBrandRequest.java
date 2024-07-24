package com.app.server.data.request.product;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SortBrandRequest {
    List<Integer> ids;

    public ClientResponse validate() {
        if (ids == null || ids.isEmpty()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.INFO_INVALID);
        }
        return ClientResponse.success(null);
    }
}