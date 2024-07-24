package com.app.server.data.request.product;

import com.app.server.constants.ResponseMessage;
import com.app.server.data.request.FilterRequest;
import com.app.server.data.request.SortByRequest;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FilterListProductPriceBySettingHistoryRequest {
    private int id;
    private int product_id;
    private List<FilterRequest> filters = new ArrayList<>();
    private List<SortByRequest> sorts = new ArrayList<>();
    private int page = 1;
    private int isLimit = 1;

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