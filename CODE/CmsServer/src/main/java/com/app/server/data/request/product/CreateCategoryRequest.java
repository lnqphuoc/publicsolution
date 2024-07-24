package com.app.server.data.request.product;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.CategoryLevel;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class CreateCategoryRequest {
    @ApiModelProperty(value = "Tên danh mục")
    private String name;
    @ApiModelProperty(value = "Danh mục cha")
    private int parent_id;
    @ApiModelProperty(value = "Hình ảnh")
    private String image;
    @ApiModelProperty(value = "Thứ tự ưu tiên")
    private int priority;
    @ApiModelProperty(value = "Loại danh mục")
    private int category_level;
    @ApiModelProperty(value = "Trạng thái")
    private int status;

    public ClientResponse validate() {
        if (StringUtils.isBlank(name)) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.NAME_INVALID);
        }
        if (category_level == CategoryLevel.PHAN_LOAI_HANG.getKey() && StringUtils.isBlank(image)) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.IMAGE_INVALID);
        }
        if (CategoryLevel.from(category_level) == null) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CATEGORY_LEVEL_INVALID);
        }

        if (category_level != CategoryLevel.NGANH_HANG.getKey() && parent_id < 1) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CATEGORY_PARENT_INVALID);
        }

        if (status < 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
        }
        return ClientResponse.success(null);
    }
}