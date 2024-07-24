package com.app.server.data.request.mission;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.MissionType;
import com.app.server.enums.MissionUnitType;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import com.ygame.framework.utils.StringUtils;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.jetty.util.StringUtil;

import java.util.List;

@Getter
@Setter
public class MissionRequest {
    @ApiModelProperty("Id")
    private Integer id;
    @ApiModelProperty("Đơn vị")
    private Integer mission_unit_id;
    @ApiModelProperty(value = "Cú pháp", required = true)
    private String name;
    @ApiModelProperty(value = "Loại nhiệm vụ", required = true)
    private int mission_type_id;
    @ApiModelProperty(value = "Nhóm nhiệm vụ", required = false)
    private int mission_group_id;
    @ApiModelProperty("Kỳ nhiệm vụ")
    private int mission_period_id;
    @ApiModelProperty(value = "Tổng các lần", required = false)
    private int action_status;
    @ApiModelProperty(value = "Danh sách sản phẩm: ", required = false)
    private MissionItemDataRequest item_data;
    @ApiModelProperty(value = "Tuần", required = false)
    private MissionConditionRequest tuan;
    @ApiModelProperty(value = "Tháng", required = false)
    private MissionConditionRequest thang;
    @ApiModelProperty(value = "Quý", required = false)
    private MissionConditionRequest quy;

    public ClientResponse validate() {
        if (StringUtil.isBlank(name)) {
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.NAME_INVALID, name);
        }

        MissionType missionType = MissionType.from(mission_type_id);
        if (missionType == null) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.TYPE_INVALID);
        }

        if (mission_unit_id != null) {
            MissionUnitType missionUnitType = MissionUnitType.from(mission_unit_id);
            if (missionUnitType == null) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.TYPE_INVALID, MissionUnitType.class);
            }
        }

        if (tuan == null &&
                thang == null &&
                quy == null) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.INFO_INVALID);
        }

        if (missionType.getId() == MissionType.THANH_TOAN.getId()) {
            if (tuan != null) {
                ClientResponse crValidateTuan = tuan.validate();
                if (crValidateTuan.failed()) {
                    return crValidateTuan;
                }
            }

            if (thang != null) {
                ClientResponse crValidateThang = thang.validate();
                if (crValidateThang.failed()) {
                    return crValidateThang;
                }
            }

            if (quy != null) {
                ClientResponse crValidateQuy = quy.validate();
                if (crValidateQuy.failed()) {
                    return crValidateQuy;
                }
            }
        } else if (missionType.getId() == MissionType.MUA_HANG.getId()) {
            if (mission_unit_id == null) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "mission_unit_id: " + mission_unit_id);
            }
            if (tuan != null) {
                ClientResponse crValidateTuan = tuan.validate();
                if (crValidateTuan.failed()) {
                    return crValidateTuan;
                }
            }

            if (thang != null) {
                ClientResponse crValidateThang = thang.validate();
                if (crValidateThang.failed()) {
                    return crValidateThang;
                }
            }

            if (quy != null) {
                ClientResponse crValidateQuy = quy.validate();
                if (crValidateQuy.failed()) {
                    return crValidateQuy;
                }
            }

            if (item_data == null) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, MissionItemDataRequest.class);
            }

            ClientResponse crItemData = item_data.validate();
            if (crItemData.failed()) {
                return crItemData;
            }
        } else if (missionType.getId() == MissionType.NQH.getId()) {
            if (tuan != null) {
                ClientResponse crValidateTuan = tuan.validate();
                if (crValidateTuan.failed()) {
                    return crValidateTuan;
                }
            }

            if (thang != null) {
                ClientResponse crValidateThang = thang.validate();
                if (crValidateThang.failed()) {
                    return crValidateThang;
                }
            }

            if (quy != null) {
                ClientResponse crValidateQuy = quy.validate();
                if (crValidateQuy.failed()) {
                    return crValidateQuy;
                }
            }

            if (item_data != null) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, MissionItemDataRequest.class);
            }
        }
        return ClientResponse.success(null);
    }
}