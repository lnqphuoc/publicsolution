package com.app.server.data.dto.program.filter;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class ProgramFilter {
    private List<Integer> ltAgencyMembershipId;
    private List<Integer> ltAgencyBusinessDepartmentId;
    private List<Integer> ltAgencyCityId;
    private boolean isBirthday;
    private List<Integer> ltGender;
    private int diffOrderDay;
    private List<Integer> ltSource;
    private boolean isNx; // không áp dụng cho đại lý có nợ xấu
    private long fromNqh; // giá trị nợ quá hạn - từ
    private long endNqh; // giá trị nợ quá hạn - đến
    private boolean isNqh; // không áp dụng cho đại lý có nợ quá hạn
    private int ngdLimitStatus; // Cho phép hoặc không cho phép đại lý có hạn mức gối đầu
    private int fromDeptCycle; // kỳ hạn nợ - từ
    private int endDeptCycle; // kỳ hạn nợ - đến
    private long fromDeptLimit; // hạn mức nợ - từ
    private long endDeptLimit; // hạn mức nợ - đến
    private long fromDtt; // doanh thu thuần - từ
    private long endDtt; // doanh thu thuần - đến
    private long fromTotalPriceSales; // doanh số - từ
    private long endTotalPriceSales; // doanh số - đến
    private long fromDeptCycleEnd; // công nợ cuối kỳ - từ
    private long endDeptCycleEnd; // công nợ cuối kỳ - đến
    private long fromTt; // tiền thu - từ
    private long endTt; // tiền thu - đến
    private int saiCamKet; // số lần cam kết sai
    private long fromDttLastYear; // doanh thu thuần năm trước - từ
    private long endDttLastYear; // doanh thu thuần năm trước - đến

    public ProgramFilter() {
        ltAgencyMembershipId = new ArrayList<>();
        ltAgencyBusinessDepartmentId = new ArrayList<>();
        ltAgencyCityId = new ArrayList<>();
        ltGender = new ArrayList<>();
        ltSource = new ArrayList<>();
        ngdLimitStatus = -1;
    }
}