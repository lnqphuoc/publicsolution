package com.app.server.data.request.mission;

import com.app.server.constants.ResponseConstants;
import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EditMissionGroupRequest {
    @ApiModelProperty(value = "ID", required = true)
    protected int id;
    @ApiModelProperty(value = "Tên", required = true)
    protected String name;

    public ClientResponse validate() {
        if (id <= 0) {
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, id);
        }

        if (name == null || name.isEmpty()) {
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, name);
        }
        
        return ResponseConstants.success;
    }
}