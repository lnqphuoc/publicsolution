package com.app.server.data.request.ctxh;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddAgencyToCTXHRequest {
    private int agency_id;
    private int promo_id;
}