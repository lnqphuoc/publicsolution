package com.app.server.data.request.banner;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.enums.SettingObjectType;
import com.app.server.enums.SettingType;
import com.app.server.response.ClientResponse;
import com.ygame.framework.utils.DateTimeUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Data
public class CreateBannerRequest {
    private String name;
    private String image;
    private String description = "";
    private List<String> agency_data = new ArrayList<>();
    private List<String> city_data = new ArrayList<>();
    private List<String> region_data = new ArrayList<>();
    private List<String> membership_data = new ArrayList<>();
    private String setting_type;
    private List<String> setting_data = new ArrayList<>();

    private Long start_date_millisecond;
    private Long end_date_millisecond;

    private String type = "IMAGE";

    public ClientResponse validate() {
        if (agency_data.isEmpty() &&
                city_data.isEmpty() &&
                region_data.isEmpty() &&
                membership_data.isEmpty()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SETTING_OBJECT_TYPE_INVALID);
        }

        if (StringUtils.isBlank(name)) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.NAME_INVALID);
        }

        if (end_date_millisecond != null && end_date_millisecond != 0) {
            if (end_date_millisecond < start_date_millisecond) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.TIME_BETWEEN_INVALID);
            }
            if (DateTimeUtils.getMilisecondsNow() > end_date_millisecond) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.TIME_END_INVALID);
            }
        }

        SettingType settingType = SettingType.from(setting_type);
        if (settingType == null) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SETTING_OBJECT_TYPE_INVALID);
        }

        if (settingType.getId() != SettingType.QUANG_BA.getId() &&
                settingType.getId() != SettingType.NHIEM_VU.getId() &&
                settingType.getId() != SettingType.BANG_THANH_TICH.getId() &&
                setting_data.isEmpty()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SETTING_TYPE_EMPTY);
        }
        return ClientResponse.success(null);
    }
}