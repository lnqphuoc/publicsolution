package com.app.server.data.response;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Data;

@Data
public class FilterAgencyResponse extends ClientResponse {
    private FilterAgencyDataInListResponse data;

    public FilterAgencyResponse success(FilterAgencyDataInListResponse data) {
        FilterAgencyResponse response = new FilterAgencyResponse();
        response.setData(data);
        response.setMessage(ResponseMessage.SUCCESS.getValue());
        response.setStatus(ResponseStatus.SUCCESS);
        return response;
    }
}