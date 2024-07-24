package com.app.server.data.request;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.RepeatType;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RepeatDataRequest {
    private int type;
    private List<Integer> data = new ArrayList<>();
    private String time_from = "";
    private String time_to = "";

    public ClientResponse validate() {
        if (type != RepeatType.NONE.getValue()) {
            if (time_from.isEmpty() || time_to.isEmpty()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.TIME_REPEAT_INVALID);
            }
        }

        return ClientResponse.success(null);
    }
}