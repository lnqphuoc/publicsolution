package com.app.server.data.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

@Data
public class DeptAgencyHistoryEntity {
    private Integer id;

    /**
     * giá trị
     */
    private Long data;

    /**
     * loại giá trị
     */
    private String type;

    private Integer status;

    private Date created_date;

    private Integer modifier_id;

    private Integer creator_id;

    private Date modified_date;

    /**
     * đại lý
     */
    private Integer agency_id;
    private Integer dept_cycle = 0;
    private Long dept_cycle_end = 0L;
    private Long dept_limit = 0L;
    private Long ngd_limit = 0L;
    private String note;
}