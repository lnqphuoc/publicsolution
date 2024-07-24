package com.app.server.data.request;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateStatusRequest extends BasicRequest {
    @ApiModelProperty(required = true, example = "0")
    @ApiParam(defaultValue = "0")
    private int status;
}