package com.app.server.data.request;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public class FilterListRequest {
    protected List<FilterRequest> filters = new ArrayList<>();
    protected List<SortByRequest> sorts = new ArrayList<>();
    protected int page = 1;
    protected int isLimit = 1;
    protected int agency_id = 0;
    protected int promo_id = 0;
    protected int id = 0;

    public ClientResponse validate() {
        if (isLimit == 1 && page < 1) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
        }
        if (isLimit != 0 && isLimit != 1) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
        }

        return ClientResponse.success(null);
    }
}