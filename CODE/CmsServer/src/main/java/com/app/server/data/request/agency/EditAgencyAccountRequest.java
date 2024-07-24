package com.app.server.data.request.agency;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

@Getter
@Setter
public class EditAgencyAccountRequest {
    private int id;
    @JsonProperty("full_name")
    private String fullName;
    private String username;
    private String password;
    @JsonProperty("is_primary")
    private int isPrimary;
    private int status;

    public ClientResponse validate() {
        if (StringUtils.isBlank(fullName)) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FULL_NAME_INVALID);
        }

        if (StringUtils.isBlank(username)) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PHONE_INVALID);
        }

        return ClientResponse.success(null);
    }
}