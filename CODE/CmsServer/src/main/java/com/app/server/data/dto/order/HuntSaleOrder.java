package com.app.server.data.dto.order;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class HuntSaleOrder {
    @ApiModelProperty("CTSS - ID")
    private int promo_id;
    @ApiModelProperty("Kỳ hạn nợ")
    private int dept_cycle;
    @ApiModelProperty("CTSS - Tên")
    private String promo_name;
    @ApiModelProperty("CTSS - Mã")
    private String promo_code;
    @ApiModelProperty("CTSS - Mô tả")
    private String promo_description;
    @ApiModelProperty("Mã ghi nhận công nợ")
    private String dept_code;
    private List<HuntSaleOrderDetail> products = new ArrayList<>();
    private List<HuntSaleOrderDetail> gifts = new ArrayList<>();
    private long total_begin_price = 0;
    private long total_promo_price = 0;
    private long total_end_price = 0;
    private int priority;

    public Integer sumProductQuantity() {
        return this.getProducts().stream().reduce(
                0, (totalProduct, objectProduct) -> totalProduct + objectProduct.sumProductQuantity(), Integer::sum)
                + this.getGifts().stream().reduce(
                0, (totalGift, objectGift) -> totalGift + objectGift.sumProductQuantity(), Integer::sum);
    }

    public Integer sumProductTotalQuantity() {
        return this.getProducts().stream().reduce(
                0, (totalProduct, objectProduct) -> totalProduct + objectProduct.sumProductQuantity(), Integer::sum);
    }

    public Integer sumGiftTotalQuantity() {
        return this.getGifts().stream().reduce(
                0, (totalGift, objectGift) -> totalGift + objectGift.sumProductQuantity(), Integer::sum);
    }
}