package com.app.server.data.request.dept;

import lombok.Data;

@Data
public class GetListDeptOrderByAgencyRequest {
    private int agency_id;
    private Long from_date;
    private Long to_date;
    private int status;
    private int page = 1;
    private int isLimit = 1;
}