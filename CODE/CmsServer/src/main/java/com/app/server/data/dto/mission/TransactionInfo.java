package com.app.server.data.dto.mission;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionInfo {
    private int id;
    private String code;
    private long transaction_value;
    private long total_end_price;
    private String created_date;
    private String note;
    private int dept_transaction_sub_type_id;
    private String description;
}