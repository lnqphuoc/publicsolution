package com.app.server.data.request.mission;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OnOffMissionBXHRequest {
    @ApiModelProperty(value = "ID Bảng thành tích", required = true)
    protected int id;
    @ApiModelProperty(value = "Lặp lại: 0-không lặp, 1-có lặp", required = true)
    private int is_repeat;
}