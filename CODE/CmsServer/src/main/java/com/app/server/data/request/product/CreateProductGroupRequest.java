package com.app.server.data.request.product;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class CreateProductGroupRequest {
    @ApiModelProperty(value = "Mã nhóm")
    private String code;
    @ApiModelProperty(value = "Tên nhóm")
    private String name;
    @ApiModelProperty(value = "Tên gọi khác")
    private String similar_name = "";
    @ApiModelProperty(value = "Trạng thái")
    private int status;
    @ApiModelProperty(value = "Danh mục")
    private int category_id;

    public ClientResponse validate() {
        if (StringUtils.isBlank(code)) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CODE_INVALID);
        }
        if (StringUtils.isBlank(name)) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.NAME_INVALID);
        }

        return ClientResponse.success(null);
    }
}