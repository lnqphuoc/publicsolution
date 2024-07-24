package com.app.server.data.entity;

import com.app.server.utils.JsonUtils;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import lombok.Data;
import org.json.simple.JSONObject;
import springfox.documentation.spring.web.json.Json;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

@Data
public class DeptAgencyDateEntity {
    private Integer id;

    /**
     * công nợ đầu kỳ
     */
    private Long dept_cycle_start = 0L;

    /**
     * công nợ cuối kỳ
     */
    private Long dept_cycle_end = 0L;

    private Integer status;

    private Date created_date;

    private Integer modifier_id;

    private Integer creator_id;

    private Date modified_date;

    /**
     * đại lý
     */
    private Integer agency_id;

    /**
     * nợ gối đầu
     */
    private Long ngd = 0L;

    /**
     * nợ quá hạn
     */
    private Long nqh = 0L;

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
     * tổng tiền mua hàng
     */
    private Long total_price_order = 0L;

    /**
     * tổng tiền thanh toán
     */
    private Long total_price_payment = 0L;

    /**
     * ngày công nợ
     */
    private Date dept_date;

    /**
     * tổng doanh thu thuần trong kỳ
     */
    private Long total_dtt_cycle = 0L;

    /**
     * tổng tiền thu trong kỳ
     */
    private Long total_tt_cycle = 0L;

    /**
     * tổng doanh thu thuần trong kỳ
     */
    private Long dtt = 0L;

    /**
     * tổng tiền thu trong kỳ
     */
    private Long tt = 0L;

    public static DeptAgencyDateEntity from(JSONObject js) {
        DeptAgencyDateEntity entity = JsonUtils.DeSerialize(
                JsonUtils.Serialize(js), DeptAgencyDateEntity.class
        );

        entity.setCreated_date(js.get("created_date") == null ? null :
                DateTimeUtils.getDateTime(
                        ConvertUtils.toString(
                                js.get("created_date")))
        );
        entity.setModified_date(js.get("modified_date") == null ? null :
                DateTimeUtils.getDateTime(
                        ConvertUtils.toString(
                                js.get("modified_date")))
        );
        entity.setDept_date(js.get("dept_date") == null ? null :
                DateTimeUtils.getDateTime(
                        ConvertUtils.toString(
                                js.get("dept_date")), "yyyy-MM-dd")
        );
        return entity;
    }
}