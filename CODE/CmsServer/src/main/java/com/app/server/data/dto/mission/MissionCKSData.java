package com.app.server.data.dto.mission;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MissionCKSData {
    @ApiModelProperty("Mã đơn hàng con")
    private String order_code;
    @ApiModelProperty("Số lượng sản phẩm = 0")
    private int product_quantity = 0;
    @ApiModelProperty("Giá trị")
    private long product_dtt;
    @ApiModelProperty("ID sản phẩm = 0")
    private int product_id = 0;
}