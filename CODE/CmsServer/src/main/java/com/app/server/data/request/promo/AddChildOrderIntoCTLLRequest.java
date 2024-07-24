package com.app.server.data.request.promo;

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
public class AddChildOrderIntoCTLLRequest {
    private int promo_id;
    private int agency_id;
    private List<TransactionCTTLRequest> transactions = new ArrayList<>();

    public ClientResponse validate() {
        if (promo_id == 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
        }

        if (agency_id == 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
        }

        if (transactions.isEmpty()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
        }

        return ClientResponse.success(null);
    }
}