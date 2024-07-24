package com.app.server.data.request;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import io.swagger.models.auth.In;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class BasicRequest {
    @ApiParam(required = true, example = "0")
    private int id;

    public ClientResponse validate() {
        if (id == 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
        }

        return ClientResponse.success(null);
    }
}