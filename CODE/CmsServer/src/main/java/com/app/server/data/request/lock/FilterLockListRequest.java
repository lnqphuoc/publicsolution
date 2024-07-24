package com.app.server.data.request.lock;

import com.app.server.constants.ResponseMessage;
import com.app.server.data.request.FilterRequest;
import com.app.server.data.request.SortByRequest;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class FilterLockListRequest {
    private List<FilterRequest> filters = new ArrayList<>();
    private List<SortByRequest> sorts = new ArrayList<>();
    private int page = 1;
    private int isLimit = 1;
    private int agency_id = 0;
    private int city_id = 0;
    private int region_id = 0;
    private int membership_id = 0;

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