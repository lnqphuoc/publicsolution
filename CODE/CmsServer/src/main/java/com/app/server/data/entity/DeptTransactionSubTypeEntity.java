package com.app.server.data.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import static javax.persistence.GenerationType.IDENTITY;

@Data
public class DeptTransactionSubTypeEntity {
    private Integer id;
    private String name;
    private String cn_effect_type;
    private String dtt_effect_type;
    private String tt_effect_type;
    private String acoin_effect_type;
    private int dept_type_id;
    private int dept_transaction_main_type_id;
    private int can_edit_effect;
    private String code;
    private String function_type;
}