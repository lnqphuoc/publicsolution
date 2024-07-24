package com.app.server.data.dto.mission;

import com.app.server.constants.ResponseConstants;
import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.enums.YesNoStatus;
import com.app.server.response.ClientResponse;
import com.app.server.utils.JsonUtils;
import com.google.common.reflect.TypeToken;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;

import java.util.List;

@Getter
@Setter
public class MissionConfigData {
    private String data;
    private String time;
    private int is_effect_now;
    private String day_of_week;
    private String key;

    public static MissionConfigData from(String key, String value) {
        MissionConfigData missionConfigData = JsonUtils.DeSerialize(value, MissionConfigData.class);
        missionConfigData.setKey(key);
        return missionConfigData;
    }

    public ClientResponse validateThoiGianKetThucNhiemVuTuan() {
        try {
            if (YesNoStatus.from(ConvertUtils.toInt(is_effect_now)).getValue() == YesNoStatus.YES.getValue()) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "Thời gian bắt đầu giao nhiệm vụ tuần: áp dụng ngay");
            }

            if (DateTimeUtils.getDateTime(time, "HH:mm") == null ||
                    ConvertUtils.toInt(day_of_week) < 2 || ConvertUtils.toInt(day_of_week) > 8
            ) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, key);
            }
            return ResponseConstants.success;
        } catch (Exception e) {
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, e.getMessage());
        }
    }

    public ClientResponse validateThoiGianKetThucTichLuyNhiemVuThang() {
        try {
            if (YesNoStatus.from(ConvertUtils.toInt(is_effect_now)).getValue() == YesNoStatus.YES.getValue()) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "Thời gian kết thúc tích lũy nhiệm vụ tháng: áp dụng ngay");
            }
            if (DateTimeUtils.getDateTime(
                    time,
                    "HH:mm") == null) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, key + ": " + time);
            }
            return ResponseConstants.success;
        } catch (Exception e) {
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, e.getMessage());
        }
    }

    public ClientResponse validateThoiGianKetThucTichLuyNhiemVuQuy() {
        try {
            if (YesNoStatus.from(ConvertUtils.toInt(is_effect_now)).getValue() == YesNoStatus.YES.getValue()) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "Thời gian kết thúc tích lũy nhiệm vụ quý: áp dụng ngay");
            }
            if (DateTimeUtils.getDateTime(
                    time,
                    "HH:mm") == null) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, key + ": " + time);
            }
            return ResponseConstants.success;
        } catch (Exception e) {
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, e.getMessage());
        }
    }

    public ClientResponse validateUuDaiTuan() {
        try {
            if (ConvertUtils.toInt(
                    data) <= 0) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, key + ": " + data);
            }
            return ResponseConstants.success;
        } catch (Exception e) {
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, e.getMessage());
        }
    }

    public ClientResponse validateUuDaiThang() {
        try {
            if (ConvertUtils.toInt(
                    data) <= 0) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, key + ": " + data);
            }
            return ResponseConstants.success;
        } catch (Exception e) {
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, e.getMessage());
        }
    }

    public ClientResponse validateUuDaiQuy() {
        try {
            if (ConvertUtils.toInt(
                    data) <= 0) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, key + ": " + data);
            }
            return ResponseConstants.success;
        } catch (Exception e) {
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, e.getMessage());
        }
    }

    public ClientResponse validateSoHuyHieuDoiTuan() {
        try {
            if (ConvertUtils.toInt(
                    data) <= 0) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, key + ": " + data);
            }
            return ResponseConstants.success;
        } catch (Exception e) {
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, e.getMessage());
        }
    }

    public ClientResponse validateSoHuyHieuDoiThang() {
        try {
            if (ConvertUtils.toInt(
                    data) <= 0) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, key + ": " + data);
            }
            return ResponseConstants.success;
        } catch (Exception e) {
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, e.getMessage());
        }
    }

    public ClientResponse validateSoHuyHieuDoiQuy() {
        try {
            if (ConvertUtils.toInt(
                    data) <= 0) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, key + ": " + data);
            }
            return ResponseConstants.success;
        } catch (Exception e) {
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, e.getMessage());
        }
    }

    public ClientResponse validateKyNhiemVu() {
        try {
            if (data == null || data.isEmpty()) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, key + ": " + data);
            }
            List<Integer> js = JsonUtils.DeSerialize(data,
                    new TypeToken<List<Integer>>() {
                    }.getType());
            if (js.size() != 2) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, key + ": " + data);
            }
            return ResponseConstants.success;
        } catch (Exception e) {
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, e.getMessage());
        }
    }
}