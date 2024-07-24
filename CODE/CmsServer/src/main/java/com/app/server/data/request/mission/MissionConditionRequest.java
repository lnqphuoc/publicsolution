package com.app.server.data.request.mission;

import com.app.server.response.ClientResponse;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MissionConditionRequest {
    @ApiModelProperty("Số lần thực hiện")
    private int action_number;
    @ApiModelProperty("Giá trị yêu cầu")
    private int required_value;

    public ClientResponse validate() {
        if (action_number == 0) {

        }
        return ClientResponse.success(null);
    }
}