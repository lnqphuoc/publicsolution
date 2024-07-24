package com.app.server.data.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

@Data
public class DeptOrderEntity {
    private Integer id;

    private Integer agency_id;

    private Integer dept_transaction_sub_type_id;

    private Integer dept_transaction_main_type_id;

    private Integer dept_type_id;

    private String cn_effect_type;

    private String dtt_effect_type;

    private String tt_effect_type;
    private String acoin_effect_type;

    private Long transaction_value;

    private Integer dept_cycle;
    private Long dept_cycle_end;

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

    private Long payment_value = 0L;

    private Date payment_date;
    private Integer dept_transaction_id;
    private Long acoin = 0L;
    private int order_data_index;
    private String code;
    private String doc_no;
    private int is_nqh;
}