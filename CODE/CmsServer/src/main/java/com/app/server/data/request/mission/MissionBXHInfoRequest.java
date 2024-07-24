package com.app.server.data.request.mission;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.*;
import com.app.server.response.ClientResponse;
import com.ygame.framework.utils.DateTimeUtils;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class MissionBXHInfoRequest {
    @ApiModelProperty(value = "Tên", required = true)
    protected String name;
    @ApiModelProperty(value = "Hình ảnh", required = false)
    protected String image;
    @ApiModelProperty(value = "Mô tả thông tin", required = true)
    protected String description;
    @ApiModelProperty(value = "Thời gian bắt đầu", required = false)
    protected long start_date_millisecond;
    @ApiModelProperty(value = "Thời gian kết thúc", required = false)
    protected long end_date_millisecond;
    @ApiModelProperty(value = "Kỳ nhiệm vụ", required = true)
    protected int mission_period_id;
    @ApiModelProperty(value = "Loại bảng thành tích", required = true)
    protected int type;
    @ApiModelProperty(value = "Tên hiển thị trên App", required = true)
    protected String name_app;
    @ApiModelProperty("Yêu cầu giá trị tích lũy tối thiểu")
    protected int require_accumulate_value = 0;
    @ApiModelProperty("Số lượng đại lý được xếp hạng: 0-không giới hạn")
    protected int agency_position_rank_limit = 0;
    @ApiModelProperty("Tổng ưu đãi")
    protected long total_offer_value = 0;
    @ApiModelProperty("Hiển thị tổng ưu đãi trên App")
    private int show_total_offer_value_in_app;

    public ClientResponse validate() {
        if (StringUtils.isBlank(name)) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.NAME_INVALID);
        }
        if (mission_period_id <= 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.INFO_INVALID);
        }
        if (type <= 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.INFO_INVALID);
        }
        return ClientResponse.success(null);
    }
}