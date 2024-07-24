package com.app.server.data.request.promo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PromoTimeRequest {
    protected long start_date_millisecond;
    protected long end_date_millisecond;
    protected String start_date;
    protected String end_date;
}