package com.app.server.data.request.agency;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class FilterListAgencyRequest {
    @ApiModelProperty(name = "Chỉ định đại lý")
    private List<String> agency_include_data = new ArrayList<>();
    @ApiModelProperty(name = "Tỉnh thành")
    private List<String> city_data = new ArrayList<>();
    @ApiModelProperty(name = "Khu vực")
    private List<String> region_data = new ArrayList<>();
    @ApiModelProperty(name = "Cấp bậc đại lý")
    private List<String> membership_data = new ArrayList<>();
    @ApiModelProperty(name = "Loại trừ đại lý")
    private List<String> agency_ignore_data = new ArrayList<>();
}