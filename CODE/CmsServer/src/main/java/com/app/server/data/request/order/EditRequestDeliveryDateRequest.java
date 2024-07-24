package com.app.server.data.request.order;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import com.ygame.framework.utils.DateTimeUtils;
import lombok.Data;

@Data
public class EditRequestDeliveryDateRequest {
    private int order_id;
    private long request_delivery_date;

    public ClientResponse validate() {
        if (order_id <= 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_NOT_FOUND);
        }

        if (request_delivery_date != 0 &&
                DateTimeUtils.getDateTime(request_delivery_date).before(
                        DateTimeUtils.getDateTime(DateTimeUtils.getNow(), "yyyy-MM-dd")
                )
        ) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.TIME_INVALID);
        }

        return ClientResponse.success(null);
    }
}