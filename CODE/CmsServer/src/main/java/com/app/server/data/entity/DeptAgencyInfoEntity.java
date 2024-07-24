package com.app.server.data.entity;

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
public class DeptAgencyInfoEntity {
    private Integer id;

    private Date created_date;

    private Integer status;

    private Integer agency_id;

    /**
     * công nợ hiện tại
     */
    private Long current_dept = 0L;

    /**
     * kỳ hạn nợ
     */
    private Integer dept_cycle = 0;

    /**
     * hạn mức công nợ
     */
    private Long dept_limit = 0L;

    /**
     * nợ gối đầu
     */
    private Long ngd = 0L;

    /**
     * hạn mức gối đầu
     */
    private Long ngd_limit = 0L;

    /**
     * doanh thu
     */
    private Long dt = 0L;

    /**
     * giới hạn cam kết
     */
    private Integer commit_limit = 0;

    /**
     * công nợ cuối kỳ
     */
    private Long dept_cycle_end = 0L;

    /**
     * giá trị công nợ đang thực hiện
     */
    private Long dept_order_waiting = 0L;

    /**
     * doanh số
     */
    private Long total_price_sales = 0L;

    /**
     * doanh thu thuần
     */
    private Long dtt = 0L;

    /**
     * tiền thu
     */
    private Long tt = 0L;

    /**
     * nợ xấu
     */
    private Long nx = 0L;

    /**
     * nợ trong hạn
     */
    private Long nth = 0L;

    /**
     * nợ đến hạn
     */
    private Long ndh = 0L;

    /**
     * nợ quá hạn
     */
    private Long nqh = 0L;

    /**
     * công nợ đầu kỳ
     */
    private Long dept_cycle_start = 0L;

    /**
     * tổng tiền mua hàng
     */
    private Long total_price_order = 0L;

    /**
     * tổng tiền thanh toán
     */
    private Long total_price_payment = 0L;

    /**
     * tổng doanh thu thuần trong kỳ
     */
    private Long total_dtt_cycle = 0L;

    /**
     * tổng tiền thu trong kỳ
     */
    private Long total_tt_cycle = 0L;

    /**
     * số lần sai cam kết
     */
    private Integer miss_commit = 0;

    public static DeptAgencyInfoEntity from(JSONObject js) {
        DeptAgencyInfoEntity entity = JsonUtils.DeSerialize(
                JsonUtils.Serialize(js), DeptAgencyInfoEntity.class
        );

        if (entity == null) {
            return null;
        }

        entity.setCreated_date(js.get("created_date") == null ? null :
                DateTimeUtils.getDateTime(
                        ConvertUtils.toString(
                                js.get("created_date")))
        );
        return entity;
    }
}