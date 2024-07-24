package com.app.server.data.entity;

import com.app.server.utils.AppUtils;
import com.app.server.utils.JsonUtils;
import lombok.Data;
import org.json.simple.JSONObject;

import java.util.Date;

@Data
public class AgencyOrderHuntSaleEntity {
    private Integer id;

    /**
     * id của đơn hàng
     */
    private Integer agency_order_id;

    /**
     * id của sản phẩm
     */
    private Integer product_id;

    /**
     * mã phiên bản
     */
    private String product_code;

    /**
     * tên phiên bản đầy đủ
     */
    private String product_full_name;

    /**
     * hình ảnh của sản phẩm
     */
    private String product_images;

    /**
     * quy cách
     */
    private String product_specification;

    /**
     * mô tả
     */
    private String product_description;


    /**
     * giá sản phẩm
     */
    private Long product_price;

    /**
     * id của đơn vị nhỏ
     */
    private Integer product_small_unit_id;

    /**
     * tên của đơn vị nhỏ
     */
    private String product_small_unit_name;

    /**
     * id của đơn vị lớn
     */
    private Integer product_big_unit_id;

    /**
     * tên của đơn vị lớn
     */
    private String product_big_unit_name;

    /**
     * tỷ lệ quy đổi ra đơn vị nhỏ
     */
    private Integer product_convert_small_unit_ratio;

    /**
     * mua tối thiểu
     */
    private Integer product_minimum_purchase;

    /**
     * bước nhảy
     */
    private Integer product_step;

    /**
     * loại hàng hóa: 1-máy móc, 2-phụ tùng
     */
    private Integer product_item_type;

    /**
     * giá trị chuyển đổi từ mã, dùng để sort
     */
    private String product_sort_data;

    /**
     * số lượng sản phẩm
     */
    private Integer product_total_quantity = 0;

    /**
     * tổng giá trước khi áp dụng ưu đãi / giảm giá
     */
    private Long product_total_begin_price = 0L;

    /**
     * tổng giá ưu đãi / giảm giá csbh
     */
    private Long product_total_promotion_price = 0L;

    /**
     * tổng giá ưu đãi / giảm giá ctkm
     */
    private Long product_total_promotion_price_ctkm = 0L;

    /**
     * tổng giá sau khi áp dụng ưu đãi / giảm giá
     */
    private Long product_total_end_price = 0L;

    /**
     * ngày tạo
     */
    private Date created_date = new Date();

    /**
     * id thương hiệu
     */
    private Integer product_brand_id;

    /**
     * tên thương hiệu
     */
    private String product_brand_name;

    private Integer promo_id;
    private Integer type;
    private String promo_info;
    private int is_combo;

    public static AgencyOrderHuntSaleEntity from(JSONObject js) {
        AgencyOrderHuntSaleEntity entity = JsonUtils.DeSerialize(
                JsonUtils.Serialize(js), AgencyOrderHuntSaleEntity.class
        );
        entity.setCreated_date(AppUtils.convertJsonToDate(js.get("created_date")));
        return entity;
    }
}