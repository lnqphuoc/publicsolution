package com.app.server.data.request.dept;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Data;

@Data
public class EditDeptCycleOfDeptOrderRequest {
    private int dept_order_id;
    private int dept_cycle;

    public ClientResponse validate() {
        if (dept_cycle < 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.DEPT_CYCLE_INVALID);
        }
        return ClientResponse.success(null);
    }
}