package com.app.server.data.request.mission;

import com.app.server.constants.ResponseConstants;
import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

@Getter
@Setter
public class GenerateRateData {
    private GenerateRatePeriodData tuan;
    private GenerateRatePeriodData thang;
    private GenerateRatePeriodData quy;

    public ClientResponse validate() {
        if (tuan == null) {
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "tuan: " + tuan);
        }

        if (thang == null) {
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "thang: " + thang);
        }

        if (quy == null) {
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "quy: " + quy);
        }


        ClientResponse crTuan = tuan.validate();
        if (crTuan.failed()) {
            return crTuan;
        }

        ClientResponse crThang = thang.validate();
        if (crThang.failed()) {
            return crThang;
        }

        ClientResponse crQuy = quy.validate();
        if (crQuy.failed()) {
            return crQuy;
        }

        return ResponseConstants.success;
    }
}