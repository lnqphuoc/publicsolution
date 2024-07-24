package com.app.server.data.request.commit;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SettingNumberDayNQHMissCommitRequest {
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
    @ApiModelProperty(name = "Đại lý thiết lập")
    private List<String> agency_setting_data = new ArrayList<>();
    @ApiModelProperty(name = "Số ngày NQH bị trừ sai cam kết")
    private int number_day_nqh_miss_commit;
}