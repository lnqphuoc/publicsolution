package com.app.server.data.dto.mission;

import com.app.server.constants.ResponseMessage;
import com.app.server.data.entity.BannerEntity;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import com.app.server.utils.JsonUtils;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;

import java.util.Date;

@Getter
@Setter
public class MissionBXHInfo {
    private int id;
    @ApiModelProperty("Mã")
    protected String code;
    @ApiModelProperty("Tên")
    protected String name;
    @ApiModelProperty("Hình ảnh")
    protected String image;
    @ApiModelProperty("Mô tả thông tin")
    protected String description;
    @ApiModelProperty("Thời gian bắt đầu")
    protected Date start_date;
    @ApiModelProperty("Thời gian kết thúc")
    protected Date end_date;
    private int status;
    protected Date created_date;
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
    @ApiModelProperty("Lặp lại: 0-Không lặp, 1-Có lặp")
    private int is_repeat;

    public static MissionBXHInfo from(JSONObject js) {
        MissionBXHInfo entity = JsonUtils.DeSerialize(
                JsonUtils.Serialize(js), MissionBXHInfo.class
        );

        if (entity == null) {
            return null;
        }

        entity.setCreated_date(js.get("created_date") == null ? null :
                DateTimeUtils.getDateTime(
                        ConvertUtils.toString(
                                js.get("created_date")))
        );
        entity.setStart_date(js.get("start_date") == null ? null :
                DateTimeUtils.getDateTime(
                        ConvertUtils.toString(
                                js.get("start_date")))
        );
        entity.setEnd_date(js.get("end_date") == null ? null :
                DateTimeUtils.getDateTime(
                        ConvertUtils.toString(
                                js.get("end_date")))
        );
        return entity;
    }
}