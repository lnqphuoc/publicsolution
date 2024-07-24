package com.app.server.data.request.agency;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class AddAddressExportBillingRequest {
    protected int agency_id;
    protected String address;
    protected String billing_label;
    protected String billing_name;
    protected String email = "";
    protected String tax_number = "";
    protected int is_default;
}