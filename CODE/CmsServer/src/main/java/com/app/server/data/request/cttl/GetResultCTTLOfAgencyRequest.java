package com.app.server.data.request.cttl;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetResultCTTLOfAgencyRequest {
    private int agency_id;
    private int promo_id;
}