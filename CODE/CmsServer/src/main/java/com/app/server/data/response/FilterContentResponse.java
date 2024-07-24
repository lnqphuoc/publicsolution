package com.app.server.data.response;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;

@Getter
@Setter
public class FilterContentResponse extends ClientResponse {
    private JSONObject data;

    public static FilterContentResponse success(JSONObject data) {
        FilterContentResponse response = new FilterContentResponse();
        response.setData(data);
        response.setMessage(ResponseMessage.SUCCESS.getValue());
        response.setStatus(ResponseStatus.SUCCESS);
        return response;
    }
}