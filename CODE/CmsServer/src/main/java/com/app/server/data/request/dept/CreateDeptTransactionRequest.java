package com.app.server.data.request.dept;

import com.app.server.data.dto.dept.DeptTransactionData;
import com.app.server.response.ClientResponse;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class CreateDeptTransactionRequest {
    @ApiModelProperty(value = "Loại giao dịch")
    private Integer dept_transaction_main_type_id;
    @ApiModelProperty(value = "Hạn mục công việc")
    private Integer dept_transaction_sub_type_id;
    @ApiModelProperty(value = "Ảnh hưởng công nợ")
    private String cn_effect_type;
    @ApiModelProperty(value = "Ảnh hưởng doanh thu thuần")
    private String dtt_effect_type;
    @ApiModelProperty(value = "Ảnh hưởng tiền thu")
    private String tt_effect_type;
    @ApiModelProperty(value = "Ảnh hưởng A-coin")
    private String acoin_effect_type;
    @ApiModelProperty(value = "Giá trị giao dịch")
    private long transaction_value;
    @ApiModelProperty(value = "Đại lý")
    private Integer agency_id;
    @ApiModelProperty(value = "Nội dung")
    private String description;
    @ApiModelProperty(value = "Mã đơn hàng nếu có")
    private String dept_type_data;

    public ClientResponse validate() {
        return ClientResponse.success(null);
    }
}