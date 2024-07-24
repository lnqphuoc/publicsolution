package com.app.server.data.request.catalog;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CreateAgencyCatalogRequest {
    private int agency_id;
    private String note;
    private List<Integer> catalogs = new ArrayList<>();

    public ClientResponse validate() {
        if (catalogs.isEmpty()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CATEGORY_INVALID);
        }
        return ClientResponse.success(null);
    }
}