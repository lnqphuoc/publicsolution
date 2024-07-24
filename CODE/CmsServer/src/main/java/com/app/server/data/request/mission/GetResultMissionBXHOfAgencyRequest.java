package com.app.server.data.request.mission;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetResultMissionBXHOfAgencyRequest {
    private int agency_id;
    private int btt_id;
    private int period;
    private int year;
}