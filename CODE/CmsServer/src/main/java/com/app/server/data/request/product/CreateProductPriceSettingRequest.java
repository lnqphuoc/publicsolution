package com.app.server.data.request.product;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.enums.SettingObjectType;
import com.app.server.enums.SettingStatus;
import com.app.server.response.ClientResponse;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class CreateProductPriceSettingRequest {
    private int id;
    private String price_object_type;
    private int price_object_id;
    private Long start_date;
    private Long end_date;
    private String name;
    private String note;

    private List<ProductPriceSettingDetailRequest> records = new ArrayList<>();

    public ClientResponse validate() {
        if (SettingObjectType.from(price_object_type) == null) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SETTING_OBJECT_TYPE_INVALID);
        }

        if (SettingObjectType.from(price_object_type).getId() != SettingObjectType.AGENCY.getId()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SETTING_AGENCY_PRICE_ONLY);
        }

        if (price_object_id <= 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SETTING_OBJECT_TYPE_INVALID);
        }

        if (start_date == null || start_date == 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.TIME_BEGIN_INVALID);
        }

        if (end_date != null && end_date <= DateTimeUtils.getMilisecondsNow()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.TIME_END_INVALID);
        }

        if (start_date != null && end_date != null && end_date < start_date) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.TIME_BETWEEN_INVALID);
        }

        /**
         * Verify dữ liệu danh mục/sản phẩm thiết lập
         */
        Map<String, String> mpRecord = new HashMap<>();
        for (int i = 0; i < records.size(); i++) {
            ClientResponse cr = records.get(i).validate();
            if (cr.failed()) {
                cr.setMessage("[Thứ " + (i + 1) + "]" + cr.getMessage());
                return cr;
            }

            if (records.get(i).getStart_date() != null && records.get(i).getStart_date() < start_date) {
                cr = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.TIME_BEGIN_INVALID);
                cr.setMessage("[Thứ " + (i + 1) + "]" + cr.getMessage());
                return cr;
            }

            if (mpRecord.get(ConvertUtils.toString(records.get(i).getProduct_id())) == null) {
                mpRecord.put(
                        mpRecord.get(ConvertUtils.toString(records.get(i).getProduct_id())),
                        ConvertUtils.toString(records.get(i).getProduct_id()));
            } else {
                cr = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.DUPLICATE);
                cr.setMessage("[Thứ " + (i + 1) + "]" + cr.getMessage());
                return cr;
            }

            if (records.get(i).getStatus() != SettingStatus.PENDING.getId()
                    && records.get(i).getEnd_date() != null
                    && records.get(i).getEnd_date() < start_date) {
                cr = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.TIME_END_INVALID);
                cr.setMessage("[Thứ " + (i + 1) + "]" + cr.getMessage());
                return cr;
            }

            if (records.get(i).getStatus() == SettingStatus.RUNNING.getId()
                    && records.get(i).getStart_date() != null
                    && records.get(i).getStart_date() > DateTimeUtils.getMilisecondsNow()) {
                cr = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.TIME_BEGIN_INVALID);
                cr.setMessage("[Thứ " + (i + 1) + "]" + cr.getMessage());
                return cr;
            }
        }
        return ClientResponse.success(null);
    }
}