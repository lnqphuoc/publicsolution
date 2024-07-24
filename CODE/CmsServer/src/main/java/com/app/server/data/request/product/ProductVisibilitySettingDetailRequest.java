package com.app.server.data.request.product;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.SettingStatus;
import com.app.server.enums.ResponseStatus;
import com.app.server.enums.VisibilityDataType;
import com.app.server.enums.VisibilityType;
import com.app.server.response.ClientResponse;
import lombok.Data;

@Data
public class ProductVisibilitySettingDetailRequest {
    private int id;
    private String visibility_data_type;
    private Integer visibility_data_id;
    private int visibility;
    private Long start_date;
    private Long end_date;
    private int status = SettingStatus.RUNNING.getId();

    public ClientResponse validate() {
        if (VisibilityDataType.from(visibility_data_type) == null) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SETTING_DATA_TYPE_INVALID);
        }

        if (visibility_data_id == null || visibility_data_id == 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SETTING_DATA_TYPE_INVALID);
        }

        if (VisibilityType.from(visibility) == null) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
        }
        return ClientResponse.success(null);
    }
}