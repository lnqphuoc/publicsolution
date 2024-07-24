package com.app.server.data.dto.dept;

import com.app.server.data.entity.DeptOrderEntity;
import lombok.Data;

import java.util.Date;

@Data
public class DeptOrderClearingData {
    private int id;
    private Integer agency_id;
    private Integer dept_cycle;
    private Long transaction_value;
    private Long payment_value;
    private Date dept_time;
    private Date payment_date;
    private int payment_doing;
    private Date payment_deadline;
    private Integer status;
    private Integer dept_type_id;
    private String dept_type_data;
    private Integer dept_transaction_sub_type_id;
    private String note = "";
    private Integer dept_transaction_id;
    private String code;
}