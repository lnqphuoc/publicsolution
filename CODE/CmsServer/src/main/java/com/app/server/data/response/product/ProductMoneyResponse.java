package com.app.server.data.response.product;

import com.ygame.framework.utils.ConvertUtils;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductMoneyResponse {
    protected int id;
    protected String code;
    protected String full_name;
    protected int product_small_unit_id;
    protected String product_small_unit_name;
    protected int step;
    protected int minimum_purchase;
    protected String images;
    protected double price;
    protected int quantity;
    protected double total_begin_price;
    protected double total_promo_price;
    protected double total_csbh_price;
    protected double total_csdm_price;
    protected double total_end_price;
    protected double uu_dai;
    protected String offer_type;
    protected String note = "";
    protected int is_error = 0;
    protected int quantity_select = 0;
    protected int item_type;
    protected int offer_value;
    protected int real_value;
    private int quantity_delivery = 0;
    private int agency_order_id;
    private String agency_order_code;
    protected double uu_dai_dam_me;

    public void tinhTongUuDai() {
        uu_dai = ConvertUtils.toLong(total_csbh_price +
                total_promo_price +
                total_csdm_price);
    }

    public long tinhDTT() {
        return ConvertUtils.toLong(
                total_begin_price -
                        total_csbh_price -
                        total_promo_price -
                        total_csdm_price);
    }
}