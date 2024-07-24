package com.app.server.data.response.order;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderDeptInfoResponse {
    private long current_dept;
    private long dept_cycle;
    private long dept_limit;
    private long overlapping_dept;
    private long overlapping_dept_limit;
    private long dept_over_at_time_book_order;
    private long dept_over_at_current;
}