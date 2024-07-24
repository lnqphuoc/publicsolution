package com.app.server.data.request.promo;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.*;
import com.app.server.response.ClientResponse;
import com.ygame.framework.utils.DateTimeUtils;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

@Getter
@Setter
public class PromoInfoRequest {
    protected int id;
    protected String code;
    protected String name;
    protected String image;
    protected String description;
    protected String promo_type = PromoType.CTSS.getKey();
    protected String condition_type = PromoConditionType.PRODUCT_QUANTITY.getKey();
    protected String promo_end_value_type = PromoEndValueType.IS_NOT_NULL.getKey();
    protected int use_limit = 0;
    protected long start_date_millisecond;
    protected long end_date_millisecond;
    protected String start_date;
    protected String end_date;
    protected int is_automatic_allocation;
    protected int status;
    protected int use_limit_per_agency = 0;
    protected int priority = 0;
    protected long promo_max_value = 0;
    @ApiModelProperty("Thánh toán trong vòng n ngày")
    protected int payment_duration = -1;
    @ApiModelProperty("Áp dụng cho giá riêng")
    protected int apply_for_private_price = 0;
    @ApiModelProperty("Hiển thị ở khu vực săn sale")
    protected int show_at_hunt_sale = 0;
    @ApiModelProperty("Loại xếp hạng: DATE / YEAR")
    protected String circle_type;
    @ApiModelProperty("Yêu cầu giá trị tích lũy tối thiểu")
    protected int require_accumulate_value = 0;
    @ApiModelProperty("Số lượng đại lý được xếp hạng: 0-không giới hạn")
    protected int agency_position_rank_limit = 0;

    /**
     * Hình thức trả thưởng
     */
    protected String form_of_reward = PromoFormOfRewardType.MUC_CAO_NHAT.getKey();
    /**
     * Hiển thị ở tab CTKM/CTTL
     */
    protected String show_on_tab = "";
    @ApiModelProperty("Yêu cầu xác nhận tham gia")
    protected int require_confirm_join = 0;
    @ApiModelProperty("Thanh toán toàn bộ giá trị tham gia")
    protected int is_full_payment = 0;
    @ApiModelProperty("Thanh toán toàn bộ giá trị tham gia")
    protected String hot_label;

    public ClientResponse validate() {
        if (StringUtils.isBlank(name)) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.NAME_INVALID);
        }
        if (StringUtils.isBlank(promo_type) || PromoType.from(promo_type) == null) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_TYPE_INVALID);
        }
        if (PromoConditionType.from(condition_type) == null) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_CONDITION_TYPE_INVALID);
        }
        if (start_date_millisecond == 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.TIME_BEGIN_INVALID);
        }
        if (end_date_millisecond != 0 && !(start_date_millisecond < end_date_millisecond)) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.TIME_BETWEEN_INVALID);
        }
        if (end_date_millisecond != 0
                && end_date_millisecond < DateTimeUtils.getMilisecondsNow()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.TIME_END_INVALID);
        }
        if (PromoConditionType.ORDER_PRICE.getKey().equals(condition_type) && is_automatic_allocation == 1) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_ORDER_PRICE_NOT_AUTOMATIC_ALLOCATION);
        }
        return ClientResponse.success(null);
    }
}