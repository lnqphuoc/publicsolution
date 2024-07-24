package com.app.server.data.request.banner;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ActivateBannerRequest {
    private List<Integer> ids = new ArrayList<>();
    private String note;

    public ClientResponse validate() {
        if (ids.isEmpty()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
        }
        return ClientResponse.success(null);
    }
}