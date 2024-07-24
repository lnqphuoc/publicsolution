package com.app.server.data.dto.dept;

import com.app.server.data.request.dept.DeptSettingApplyObjectRequest;
import lombok.Data;

import java.util.Date;

@Data
public class DeptSettingData {
    private Integer id;
    private DeptSettingApplyObjectRequest dept_apply_object;
    private Long dept_limit;
    private Long ngd_limit;
    private Integer dept_cycle;
    private Long start_date;
    private Long end_date;
    private Date created_date;
}