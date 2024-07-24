package com.app.server.data.request.agency;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAgencyContractInfoRequest {
    private int agency_id;
    @ApiModelProperty(value = "Số hợp đồng")
    private String contract_number;
    @ApiModelProperty(value = "Tên công ty")
    private String company_name;
    @ApiModelProperty(value = "Người đại diện")
    private String representative;
    @ApiModelProperty(value = "Mã số thuế")
    private String tax_number;
    @ApiModelProperty(value = "Số CMND/CCCD")
    private String identity_number;
    @ApiModelProperty(value = "Ngày cấp")
    private String identity_date;

    public ClientResponse validate() {
        if (agency_id == 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
        }
        if (contract_number.isEmpty()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CONTRACT_NUMBER_EMPTY);
        }
        if (company_name.isEmpty()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.COMPANY_NAME_EMPTY);
        }
        if (representative.isEmpty()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.REPRESENTATIVE_EMPTY);
        }
        if (tax_number.isEmpty()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.TAX_NUMBER_INVALID);
        }
        if (identity_number.isEmpty()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.IDENTITY_NUMBER_EMPTY);
        }
        if (identity_date.isEmpty()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.IDENTITY_DATE);
        }

        return ClientResponse.success(null);
    }
}