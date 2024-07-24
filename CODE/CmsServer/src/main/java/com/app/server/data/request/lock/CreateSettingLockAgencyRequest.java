package com.app.server.data.request.lock;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.LockOptionType;
import com.app.server.enums.ResponseStatus;
import com.app.server.enums.SettingObjectType;
import com.app.server.response.ClientResponse;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CreateSettingLockAgencyRequest {
    @ApiModelProperty(name = "Loại đối tượng")
    private String setting_object_type;
    @ApiModelProperty(name = "ID đối tượng")
    private List<String> setting_object_data = new ArrayList<>();
    @ApiModelProperty(name = "Khóa ngay: 1, Khóa cuối ngày: 2, Khóa theo n ngày: 3, Không khóa: 4")
    private int option_lock;
    @ApiModelProperty(name = "Số ngày")
    private int day_lock;
    private long start_date_millisecond;

    public ClientResponse validate() {
        SettingObjectType settingObjectType = SettingObjectType.from(setting_object_type);
        if (settingObjectType == null) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SETTING_OBJECT_TYPE_INVALID);
        }

        if (setting_object_data == null || setting_object_data.isEmpty()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SETTING_TYPE_EMPTY);
        }

        if (option_lock == LockOptionType.KHOA_N_NGAY.getId() &&
                day_lock < 1
        ) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.DAY_LOCK_INVALID);
        }
        return ClientResponse.success(null);
    }
}