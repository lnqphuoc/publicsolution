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
public class EditCategoryRequest {
    @ApiModelProperty(value = "Id")
    private int id;
    @ApiModelProperty(value = "Tên danh mục")
    private String name;
    @ApiModelProperty(value = "Hình ảnh")
    private String image;
    @ApiModelProperty(value = "Trạng thái")
    private int status;

    @ApiModelProperty(value = "Danh mục cha")
    private int parent_id;

    public ClientResponse validate() {
        if (StringUtils.isBlank(name)) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
        }
        if (status < 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
        }
        return ClientResponse.success(null);
    }
}