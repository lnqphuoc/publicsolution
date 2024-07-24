package com.app.server.data.request.dept;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.DeptTransactionMainType;
import com.app.server.enums.DeptType;
import com.app.server.enums.ResponseStatus;
import com.app.server.enums.TransactionEffectType;
import com.app.server.response.ClientResponse;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class EditDeptDttRequest {
    private int agency_id;
    private int type;
    private long data;
    private String note;

    public ClientResponse validate() {
        if (agency_id <= 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
        }
        if (type <= 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.TYPE_INVALID);
        }
        if (data <= 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SETTING_VALUE_TYPE_INVALID);
        }
        if (StringUtils.isBlank(note)) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.NOTE_NOT_EMPTY);
        }
        return ClientResponse.success(null);
    }
}