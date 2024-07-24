package com.app.server.data.request.promo;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.enums.PromoStopType;
import com.app.server.response.ClientResponse;
import com.ygame.framework.utils.DateTimeUtils;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StopPromoRequest {
    private int id;
    private String stop_type = PromoStopType.STOP_NOW.getKey();
    private long stop_time;
    private String note;


    public ClientResponse validate() {
        if (id < 1) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
        }
        if (PromoStopType.from(stop_type) == null) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
        }
        if (PromoStopType.STOP_SCHEDULE.getKey().equals(stop_type)
                && DateTimeUtils.getMilisecondsNow() > stop_time) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.TIME_END_INVALID);
        }
        return ClientResponse.success(null);
    }
}