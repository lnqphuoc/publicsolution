package com.app.server.data.request.product;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.enums.SettingObjectType;
import com.app.server.response.ClientResponse;
import com.ygame.framework.utils.DateTimeUtils;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class EditProductVisibilitySettingRequest {
    private int id;
    private String visibility_object_type;
    private int visibility_object_id;
    private Long start_date;
    private Long end_date;
    private String name;

    private List<ProductVisibilitySettingDetailRequest> records = new ArrayList<>();

    public ClientResponse validate() {
        if (SettingObjectType.from(visibility_object_type) == null) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SETTING_OBJECT_TYPE_INVALID);
        }
        if (visibility_object_id <= 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SETTING_OBJECT_TYPE_INVALID);
        }
        if (end_date != null && end_date <= DateTimeUtils.getMilisecondsNow()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.TIME_END_INVALID);
        }

        if (records.isEmpty()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SETTING_DATA_TYPE_INVALID);
        }

        Map<String, String> mpRecord = new HashMap<>();
        for (int i = 0; i < records.size(); i++) {
            ClientResponse cr = records.get(i).validate();
            if (cr.failed()) {
                cr.setMessage("[Thứ " + (i + 1) + "]" + cr.getMessage());
                return cr;
            }

            if (mpRecord.get(records.get(i).getVisibility_data_type() + "-" + records.get(i).getVisibility_data_id()) == null) {
                mpRecord.put(mpRecord.get(records.get(i).getVisibility_data_type() + "-" + records.get(i).getVisibility_data_id()),
                        records.get(i).getVisibility_data_type() + "-" + records.get(i).getVisibility_data_id());
            } else {
                cr = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.DUPLICATE);
                cr.setMessage("[Thứ " + (i + 1) + "]" + cr.getMessage());
                return cr;
            }
        }


        return ClientResponse.success(null);
    }
}