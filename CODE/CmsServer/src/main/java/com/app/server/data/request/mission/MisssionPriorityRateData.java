package com.app.server.data.request.mission;

import com.app.server.constants.ResponseConstants;
import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MisssionPriorityRateData {
    private int sltt;
    private int rate;
    private List<Integer> nhiem_vu;

    public ClientResponse validate() {
        if (sltt < 0) {
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "sltt: " + sltt);
        }
        if (rate < 0 || rate > 100) {
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "rate: " + rate);
        }

        if (nhiem_vu == null) {
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "nhiem_vu: " + nhiem_vu);
        }
        return ResponseConstants.success;
    }
}