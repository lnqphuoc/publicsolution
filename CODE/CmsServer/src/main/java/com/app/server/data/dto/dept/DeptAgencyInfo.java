package com.app.server.data.dto.dept;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeptAgencyInfo {
    private Long current_dept;
    private Long dept_limit;
    private Long dept_available;
    private Long overlapping_dept_limit;
    private Long ngd;
    private Integer dept_cycle;
    private Long dept_cycle_end;
    private Long dept_cycle_start;
    private Long nth;
    private Long ndh;
    private Long nqh;
    private Integer commit_limit;
}