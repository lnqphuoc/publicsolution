package com.app.server.data.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

@Data
public class AgencyOrderHistoryEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    /**
     * mã đơn hàng
     */
    private String code;

    /**
     * id của đại lý
     */
    private Integer agency_id;

    /**
     * id của tài khoản đại lý
     */
    private Integer agency_account_id;

    /**
     * id của cấp bật thành viên
     */
    private Integer membership_id;

    /**
     * thông tin địa chỉ giao hàng
     */
    private String address_delivery;

    /**
     * thông tin xuất hóa đơn
     */
    private String address_billing;

    /**
     * ngày yêu cầu được giao
     */
    private Date request_delivery_date;

    /**
     * ghi chú
     */
    private String note;

    /**
     * tổng tiền trước khi áp dụng ưu đãi, giảm giá
     */
    private long total_begin_price;

    /**
     * tổng tiền ưu đãi, giảm giá
     */
    private long total_promotion_price;

    /**
     * tổng tiền sau khi áp dụng ưu đãi, giảm giá
     */
    private long total_end_price;

    /**
     * tổng số lượng sản phẩm trong đơn hàng
     */
    private long total_product_quantity;

    /**
     * tạo từ app-1, admin-2
     */
    private Integer source;

    /**
     * loại đơn hàng
     */
    private Integer type;

    /**
     * 0: chờ xác nhận, 1: soạn hàng, 2: đã giao, 3: trả hàng
     */
    private Integer status;

    /**
     * ngày xác nhận đã giao
     */
    private Date confirm_delivery_date;

    /**
     * ngày tạo
     */
    private Date created_date = new Date();

    /**
     * ngày cập nhật trạng thái đơn hàng
     */
    private Date update_status_date = new Date();

    /**
     * người tạo
     */
    private Integer creator_id;


    /**
     * Thông tin đại lý
     */
    private String agency_info;

}