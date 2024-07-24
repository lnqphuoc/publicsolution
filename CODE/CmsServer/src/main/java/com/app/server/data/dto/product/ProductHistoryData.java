package com.app.server.data.dto.product;

import com.app.server.utils.JsonUtils;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;

import java.util.Date;


@Getter
@Setter
public class ProductHistoryData {
    private Integer id;
    /**
     * mã phiên bản
     */
    private String code;

    /**
     * tên phiên bản rút gọn
     */
    private String short_name;

    /**
     * tên phiên bản đầy đủ
     */
    private String full_name;

    /**
     * thời gian bảo hành
     */
    private String warranty_time;

    /**
     * hình ảnh của sản phẩm
     */
    private String images;

    /**
     * quy cách
     */
    private String specification;

    /**
     * id màu sắc
     */
    private Integer product_color_id;

    /**
     * đặc điểm
     */
    private String characteristic;

    /**
     * mô tả
     */
    private String description;

    /**
     * hướng dẫn sử dụng
     */
    private String user_manual;

    /**
     * thông số kỹ thuật
     */
    private String technical_data;

    /**
     * 0: ẩn, 1: hiển thị
     */
    private int status;

    /**
     * giá, -1: giá liên hệ, >=0 giá
     */
    private int price;

    /**
     * id của đơn vị nhỏ
     */
    private int product_small_unit_id;

    /**
     * id của đơn vị lớn
     */
    private Integer product_big_unit_id;

    /**
     * tỷ lệ quy đổi ra đơn vị nhỏ
     */
    private int convert_small_unit_ratio;

    /**
     * mua tối thiểu
     */
    private int minimum_purchase;

    /**
     * bước nhảy
     */
    private int step;

    /**
     * tổng số lượng bán
     */
    private int total_sell_quantity;

    /**
     * tổng số lượt bán
     */
    private int total_sell_turn;

    /**
     * vị trí xếp hạng hot
     */
    private int hot_priority;

    /**
     * id của nhóm sản phẩm
     */
    private int product_group_id;

    /**
     * số lượng tồn trong kho
     */
    private int warehouse_quantity;

    /**
     * ngày tạo
     */
    private Date created_date;

    /**
     * loại hàng hóa: 1-máy móc, 2-phụ tùng
     */
    private int item_type;

    /**
     * giá trị chuyển đổi từ mã, dùng để sort
     */
    private String sort_data;

    /**
     * id của thương hiệu
     */
    private Integer brand_id;

    /**
     * id của danh mục
     */
    private int category_id;

    /**
     * tên khác
     */
    private String other_name;

    /**
     * trạng thái hiển thị trên app
     */
    private int app_active = 0;

    /**
     * nhãn/tag
     */
    private String hot_label;

    private Integer mat_hang_id;

    private Integer plsp_id;
    
    private Integer pltth_id;
}