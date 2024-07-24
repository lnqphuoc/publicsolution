package com.app.server.data.request.damme;

import com.app.server.constants.ResponseMessage;
import com.app.server.data.request.cttl.TransactionCTTLRequest;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CreatePhieuDieuChinhCSDMRequest {
    private int promo_id;
    private int agency_id;
    private long value;
    private int type;
    private String note = "";

    public ClientResponse validate() {
        if (promo_id == 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
        }

        if (agency_id == 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
        }

        if (value == 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
        }

        return ClientResponse.success(null);
    }
}