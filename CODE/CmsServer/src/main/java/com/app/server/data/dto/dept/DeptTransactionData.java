package com.app.server.data.dto.dept;

import lombok.Data;

import java.util.Date;

@Data
public class DeptTransactionData {
    private Integer id;

    private Integer dept_transaction_sub_type_id;

    private Integer dept_transaction_main_type_id;

    private Integer dept_type_id;

    private String cn_effect_type;

    private String dtt_effect_type;

    private String tt_effect_type;

    private Integer transaction_value;

    private Integer agency_id;

    private Integer dept_cycle_end;

    private String dept_type_data;

    private String note;

    private Integer status;

    private Date created_date;

    private Integer modifier_id;

    private Integer creator_id;

    private Date modified_date;

    /**
     * Ngày phát sinh công nợ
     */
    private Date dept_time;
}