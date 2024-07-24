package com.app.server.data.request.agency;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditAddressExportBillingRequest extends AddAddressExportBillingRequest {
    @JsonProperty(value = "id", defaultValue = "0")
    private Integer id;
}