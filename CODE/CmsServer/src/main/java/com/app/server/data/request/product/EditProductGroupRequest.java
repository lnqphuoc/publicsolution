package com.app.server.data.request.product;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class EditProductGroupRequest {
    private int id;
    private String code;
    private String name;
    private int status;
    private int category_id;
    private String similar_name = "";
    private String created_date;

    public ClientResponse validate() {
        if (StringUtils.isBlank(code)) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CODE_INVALID);
        }
        if (StringUtils.isBlank(name)) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.NAME_INVALID);
        }
        if (category_id == 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CATEGORY_INVALID);
        }
        if (status < 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
        }
        return ClientResponse.success(null);
    }
}