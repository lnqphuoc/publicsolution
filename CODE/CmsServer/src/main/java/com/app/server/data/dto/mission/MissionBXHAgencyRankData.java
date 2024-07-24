package com.app.server.data.dto.mission;

import com.app.server.data.dto.agency.AgencyBasicData;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class MissionBXHAgencyRankData {
    private int agency_id;
    private int point;
    private Date time;
    private int row_id;
}