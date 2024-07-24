package com.app.server.data.dto.ctxh;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoucherData {
    private int id;
    private int voucher_release_period_id;
    private String offer_type;
    private String items;
    private long total_value;
}