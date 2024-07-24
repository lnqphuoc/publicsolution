package com.app.server.data.entity;

import com.app.server.utils.AppUtils;
import com.app.server.utils.JsonUtils;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import lombok.Data;
import org.json.simple.JSONObject;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

@Data
public class AgencyOrderEntity {
    private Integer id;

    /**
     * mã đơn hàng
     */
    private String code = "";

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
    private String address_delivery = "";

    /**
     * thông tin xuất hóa đơn
     */
    private String address_billing = "";

    /**
     * ngày yêu cầu được giao
     */
    private Date request_delivery_date;

    /**
     * ghi chú
     */
    private String note = "";

    /**
     * tổng tiền trước khi áp dụng ưu đãi, giảm giá
     */
    private double total_begin_price;

    /**
     * tổng tiền ưu đãi, giảm giá
     */
    private double total_promotion_price;

    /**
     * tổng tiền sau khi áp dụng ưu đãi, giảm giá
     */
    private double total_end_price;

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
     * người chỉnh sửa
     */
    private Integer modifier_id;

    /**
     * ghi chú nội bộ
     */
    private String note_internal;

    /**
     * ghi chú hủy
     */
    private String note_cancel = "";

    /**
     * thông tin đại lý
     */
    private String agency_info = "";

    /**
     * tổng tiền ưu đãi, giảm giá trên sản phẩm
     */
    private double total_promotion_product_price;

    /**
     * tổng tiền ưu đãi, giảm giá trên đơn hàng csbh
     */
    private double total_promotion_order_price;

    /**
     * tổng tiền ưu đãi, giảm giá trên đơn hàng ctkm
     */
    private double total_promotion_order_price_ctkm;

    /**
     * tổng giảm của săn sale
     */
    private double total_sansale_promotion_price = 0;

    /**
     * hoàn tiền
     */
    private double total_refund_price;

    /**
     * thông tin csbh trên đơn
     */
    private String promo_order_info = "[]";

    /**
     * thông tin ctkm trên đơn
     */
    private String promo_order_info_ctkm = "[]";

    /**
     * thông tin csbh trên sản phẩm
     */
    private String promo_product_info = "[]";

    /**
     * thông tin ctkm trên sản phẩm
     */
    private String promo_product_info_ctkm = "[]";

    /**
     * thông tin csbh tang qua
     */
    private String promo_good_offer_info = "[]";

    /**
     * thông tin ctkm tang qua
     */
    private String promo_good_offer_info_ctkm = "[]";

    /**
     * tất cả ưu đãi
     */
    private String promo_all_id_info = "[]";

    /**
     * Trường hợp
     */
    private int stuck_type = 0;

    /**
     * Trường hợp cụ thể
     */
    private String stuck_info = "[]";

    /**
     * trạng thái ghi nhận công nợ
     */
    private int increase_dept = 0;

    /**
     * nợ quá hạn lúc đặt đơn
     */
    private Double nqh_order = 0.0;

    /**
     * Vượt hạn mức lúc đặt đơn
     */
    private Double hmkd_over_order = 0.0;

    private int commit_approve_status;

    private Integer dept_cycle;

    private Date confirm_prepare_date;
    private int total;
    private int sync_status;
    private String sync_note = "";

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
     * tổng ưu đãi đam mê
     */
    private double total_dm_price = 0;

    /**
     * thông tin csdm sản phẩm
     */
    private String dm_product_info = "[]";

    /**
     * tổng ưu đãi voucher
     */
    private double total_voucher_price = 0;

    /**
     * Voucher sử dụng
     */
    private String voucher_info = "[]";


    public static AgencyOrderEntity from(JSONObject js) {
        AgencyOrderEntity entity = JsonUtils.DeSerialize(
                JsonUtils.Serialize(js), AgencyOrderEntity.class
        );

        entity.setCreated_date(AppUtils.convertJsonToDate(js.get("created_date")));
        entity.setRequest_delivery_date(AppUtils.convertJsonToDate(js.get("request_delivery_date")));
        entity.setConfirm_delivery_date(AppUtils.convertJsonToDate(js.get("confirm_delivery_date")));
        entity.setUpdate_status_date(AppUtils.convertJsonToDate(js.get("update_status_date")));
        entity.setConfirm_delivery_date(AppUtils.convertJsonToDate(js.get("confirm_delivery_date")));
        entity.setConfirm_prepare_date(AppUtils.convertJsonToDate(js.get("confirm_prepare_date")));
        return entity;
    }

}