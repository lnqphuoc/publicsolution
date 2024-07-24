package com.app.server.data.request;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class FilterRequest {
    @NotBlank
    private String type;
    private String key;
    private String value;
    private Long value1; // start date
    private Long value2; // end date
}