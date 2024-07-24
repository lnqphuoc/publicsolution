package com.app.server.data.request.product;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class ImportEditProductGroupRequest {
    private int id;
    private String code;
    private String name;
    private Integer status;
    private Integer category_id;
    private String similar_name;

    public ClientResponse validate() {
        if (code != null && (code.isEmpty() || code.trim().isEmpty())) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.INFO_INVALID);
        }
        if (name != null && (name.isEmpty() || name.trim().isEmpty())) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.INFO_INVALID);
        }
        if (category_id != null && (category_id <= 0)) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.INFO_INVALID);
        }
        if (status != null && (status != 0 && status != 1)) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.INFO_INVALID);
        }
        return ClientResponse.success(null);
    }
}