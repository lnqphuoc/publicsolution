package com.app.server.data.dto.program.agency;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgencyAccount {
    private int id;
    @SerializedName("full_name")
    private String fullName;
    private String username;
    private String password;
    @SerializedName("agency_id")
    private int agencyId;
    @SerializedName("agency_phone")
    private String agencyPhone;
    @SerializedName("is_primary")
    private int isPrimary;
    private int status;
    private String otpAuthCode;
    private int otpAuthStatus;
    private String token;
    @SerializedName("force_update_status")
    private int forceUpdateStatus;

    private Agency agency;
}