package com.app.server.data.request.cttl;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetResultMissionBXHInfoRequest {
    private int id;
    private int period;
    private int year;
}