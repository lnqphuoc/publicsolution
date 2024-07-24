package com.app.server.data.dto.ctxh;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderAccumulateCTXHData {
    private int agency_id;
    private int agency_order_id;
    private String agency_order_code;
    private long total_end_price;
}