package com.app.server.data.entity;

import com.app.server.utils.AppUtils;
import com.app.server.utils.JsonUtils;
import lombok.Data;
import org.json.simple.JSONObject;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

@Data
public class AgencyOrderDetailEntity {
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
     * tag của product
     */
    private String product_tag;

    /**
     * mã phiên bản
     */
    private String product_code;

    /**
     * tên phiên bản đầy đủ
     */
    private String product_full_name;

    /**
     * thời gian bảo hành
     */
    private String product_warranty_time;

    /**
     * hình ảnh của sản phẩm
     */
    private String product_images;

    /**
     * quy cách
     */
    private String product_specification;

    /**
     * id màu sắc
     */
    private Integer product_color_id;

    /**
     * tên màu sắc
     */
    private String product_color_name;

    /**
     * đặc điểm
     */
    private String product_characteristic;

    /**
     * mô tả
     */
    private String product_description;

    /**
     * hướng dẫn sử dụng
     */
    private String product_user_manual;

    /**
     * thông số kỹ thuật
     */
    private String product_technical_data;

    /**
     * giá sản phẩm
     */
    private Double product_price;

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
    private Integer product_minimum_purchase = 1;

    /**
     * bước nhảy
     */
    private Integer product_step = 0;

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
    private Double product_total_begin_price = 0.0;

    /**
     * tổng giá ưu đãi / giảm giá csbh
     */
    private Double product_total_promotion_price = 0.0;

    /**
     * tổng giá ưu đãi / giảm giá ctkm
     */
    private Double product_total_promotion_price_ctkm = 0.0;

    /**
     * tổng giá sau khi áp dụng ưu đãi / giảm giá
     */
    private Double product_total_end_price = 0.0;

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

    private int combo_id;
    private int promo_id;
    private int combo_quantity;
    private String combo_data;
    private String promo_description;

    private double product_total_dm_price = 0;
    private Integer dm_id;
    private Integer dm_percent;

    public static AgencyOrderDetailEntity from(JSONObject js) {
        AgencyOrderDetailEntity entity = JsonUtils.DeSerialize(
                JsonUtils.Serialize(js), AgencyOrderDetailEntity.class
        );
        entity.setCreated_date(AppUtils.convertJsonToDate(js.get("created_date")));
        return entity;
    }
}