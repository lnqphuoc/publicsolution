package com.app.server.data.dto.ctxh;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderVoucherGiftData {
    private int id;
    private int product_id;
    private int product_total_quantity;
    private int voucher_id;
}