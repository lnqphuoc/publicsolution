package com.app.server.data.request.product;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.*;
import com.app.server.response.ClientResponse;
import lombok.Data;

@Data
public class ProductPriceSettingDetailRequest {
    private int id;
    private String price_setting_type;
    private String price_data_type;
    private double price_setting_value;
    private Integer product_id;
    private int is_auto = 1;
    private int minimum_purchase = 0;
    private Long start_date;
    private Long end_date;
    private int status = SettingStatus.RUNNING.getId();

    public ClientResponse validate() {
        if (product_id == null || product_id == 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SETTING_DATA_TYPE_INVALID);
        }

        if (start_date != null && end_date != null && end_date < start_date) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.TIME_BETWEEN_INVALID);
        }

        if (PriceSettingType.from(price_setting_type) == null) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SETTING_VALUE_TYPE_INVALID);
        }

        if (PriceDataType.from(price_data_type) == null) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SETTING_VALUE_TYPE_INVALID);
        }

        if (PriceSettingType.CONTACT.getCode().equals(price_setting_type) &&
                price_setting_value != -1) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SETTING_VALUE_TYPE_INVALID);
        }

        if ((PriceSettingType.CONSTANT.getCode().equals(price_setting_type) ||
                PriceSettingType.CONTACT.getCode().equals(price_setting_type)) &&
                is_auto == 1) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SETTING_VALUE_TYPE_INVALID);
        }

        if ((PriceSettingType.INCREASE.getCode().equals(price_setting_type) ||
                PriceSettingType.DECREASE.getCode().equals(price_setting_type)) &&
                is_auto == 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SETTING_VALUE_TYPE_INVALID);
        }

        if (minimum_purchase < 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.MINIMUM_PURCHASE_INVALID);
        }

        if (start_date == null
                && end_date != null) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.TIME_BEGIN_INVALID);
        }
        return ClientResponse.success(null);
    }
}