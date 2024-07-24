package com.app.server.data.request.agency;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddNewAgencyAccountRequest {
    @JsonProperty(value = "agency_id", defaultValue = "0")
    private Integer agencyId;
    @JsonProperty("full_name")
    private String fullName;
    private String username;
    private String password;
    @JsonProperty("is_primary")
    private int isPrimary;
    private int status;
}