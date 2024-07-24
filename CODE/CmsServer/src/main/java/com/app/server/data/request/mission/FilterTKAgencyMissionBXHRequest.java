
package com.app.server.data.request.mission;

import com.app.server.data.request.FilterListRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FilterTKAgencyMissionBXHRequest extends FilterListRequest {
    private int period;
    private int year;
}