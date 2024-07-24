package com.app.server.data.request.promo;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Data;

import java.util.List;

@Data
public class SortPromoRequest {
    private List<Integer> ids;

    public ClientResponse validate() {
        return ClientResponse.success(null);
    }
}