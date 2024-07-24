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
public class EditBrandRequest {
    @ApiModelProperty(value = "Id")
    private int id;
    @ApiModelProperty(value = "Tên thương hiệu")
    private String name;
    @ApiModelProperty(value = "Hình ảnh thương hiệu")
    private String image = "";
    @ApiModelProperty(value = "Nổi bật: 0-không, 1-có")
    private int is_highlight;
    @ApiModelProperty(value = "Thứ tự sắp xếp khi nổi bật")
    private int highlight_priority;
    @ApiModelProperty(value = "Trạng thái")
    private int status;

    public ClientResponse validate() {
        if (StringUtils.isBlank(name)) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
        }
        if (is_highlight < 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
        }
        if (highlight_priority < 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
        }

        if (is_highlight == 1 && StringUtils.isBlank(image)) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.IMAGE_EMPTY);
        }

        if (status < 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
        }
        return ClientResponse.success(null);
    }
}