package com.app.server.data.dto.agency;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgencySession {
    private int agencyId;
    private int agencyAccountId;
    private String agencyAccountPhone;
    private String agencyAccountDeviceId;
}