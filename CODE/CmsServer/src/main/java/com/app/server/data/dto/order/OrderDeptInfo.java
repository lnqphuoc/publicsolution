package com.app.server.data.dto.order;

import lombok.Data;

@Data
public class OrderDeptInfo {
    private long hmkd;
    private long cno;
    private long nqh_order;
    private long nqh_current;
    private long ngd_limit;
    private long hmkd_over_order;
    private long hmkd_over_current;
    private long committed_money;
    private String committed_date;
    private int dept_cycle;
}