package com.app.server.data.response.order;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderSummaryInfoResponse {
    /**
     * tổng tiền trước khi áp dụng ưu đãi, giảm giá
     */
    private double total_begin_price = 0;

    /**
     * tổng tiền hoàn lại
     */
    private double total_refund_price = 0;

    /**
     * tổng tiền ưu đãi, giảm giá theo sản phẩm
     */
    private double total_promotion_price_of_product = 0;

    /**
     * tổng tiền ưu đãi, giảm giá theo đơn hàng
     */
    private double total_promotion_price_of_order = 0;

    /**
     * tổng tiền phí dịch vụ
     */
    private double total_service_fee = 0;

    /**
     * tổng tiền sau khi áp dụng ưu đãi, giảm giá
     */
    private double total_end_price = 0;

    /**
     * tổng số lượng sản phẩm
     */
    private int total_product_quantity = 0;

    /**
     * tổng tiền của tặng hàng
     */
    private double total_goods_offer_price = 0;

    /**
     * tổng tiền đổi quà
     */
    private double total_goods_offer_claimed_price = 0;

    /**
     * tổng tiền đổi quà còn lại
     */
    private double total_goods_offer_remain_price = 0;

    /**
     * tổng giảm giá trên sản phẩm của ctkm
     */
    private double total_promotion_price = 0;

    /**
     * tổng giảm giá trên đơn hàng của ctkm
     */
    private double total_promotion_order_price_ctkm = 0;
    private double total_promotion_product_price_ctkm = 0;

    /**
     * tổng giảm của săn sale
     */
    private double total_sansale_promotion_price = 0;

    /**
     * tổng số lượng hàng tặng
     */
    private int total_goods_quantity = 0;

    /**
     * tổng số lượng hàng tặng kèm theo
     */
    private int total_bonus_goods_quantity = 0;

    /**
     * tổng số lượng quà tặng kèm theo
     */
    private int total_bonus_gift_quantity = 0;

    /**
     * Tổng ưu đãi đam mê
     */
    private double tong_uu_dai_dam_me;

    /**
     * Tổng ưu đãi voucher
     */
    private double total_voucher_price;
}