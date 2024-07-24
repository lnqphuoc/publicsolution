package com.app.server.data.request.cttl;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetListCTTLByTransactionRequest {
    private int agency_id;
    private int promo_id;
    private int transaction_id;
    private int type;
}