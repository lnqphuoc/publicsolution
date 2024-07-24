package com.app.server.data.request.lock;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SettingLockAgencyRequest {
    @ApiModelProperty(name = "Loại đối tượng")
    private int setting_object_type;
    @ApiModelProperty(name = "ID đối tượng")
    private List<Integer> setting_object_data = new ArrayList<>();
    @ApiModelProperty(name = "Khóa: 1, Mở: 0")
    private int lock;
    @ApiModelProperty(name = "Khóa ngay: 1, Khóa cuối ngày: 2, Khóa theo n ngày: 3")
    private int option_lock;
    @ApiModelProperty(name = "Số ngày khóa")
    private int day_lock;
}