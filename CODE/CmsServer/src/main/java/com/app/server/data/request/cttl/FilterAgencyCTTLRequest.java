package com.app.server.data.request.cttl;

import com.app.server.data.request.FilterListRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FilterAgencyCTTLRequest extends FilterListRequest {
    private int promo_id;
    private int type;
}