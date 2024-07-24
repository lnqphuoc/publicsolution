package com.app.server.data.dto.program;

import lombok.Data;

@Data
public class DeptInfo {
    private int deptCycle; // kỳ hạn nợ
    private long deposit; // tiền đặt cọc
    private long ngdLimit; // hạn mức nợ gối đầu
    private long deptLimit; // hạn mức nợ
    private long currentDept; // công nợ hiện tại
    private long availabilityDeptLimit; // hạn mức khả dụng
    private long deptCycleStart; // công nợ đầu kỳ
    private long deptCycleEnd; // công nợ cuối kỳ
    private long totalPriceOrder; // tổng tiền mua hàng trong kỳ
    private long totalPricePayment; // tổng tiền thanh toán trong kỳ
    private long totalPriceSales; // doanh số
    private long ngd; // nợ gối đầu
    private long nqh; // nợ quá hạn
    private long nx; // nợ xấu
    private long nth; // nợ trong hạn
    private long ndh; // nợ đến hạn
    private long dtt; // doanh thu thuần
    private long tt; // tiền thu
    private long totalDttCycle; // tổng doanh thu thuần trong kỳ
    private long totalTtCycle; // tổng tiền thu trong kỳ
}