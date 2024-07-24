package com.app.server.data.dto.order;

import com.app.server.enums.StuckType;
import lombok.Data;

@Data
public class StuckData {
    private StuckType stuck_type;
    private String stuck_info;
    private String data;

    public StuckData(StuckType stuck_type, String stuck_info) {
        this.stuck_type = stuck_type;
        this.stuck_info = stuck_info;
    }

    public StuckData(StuckType stuck_type, String stuck_info, String data) {
        this.stuck_type = stuck_type;
        this.stuck_info = stuck_info;
        this.data = data;
    }
}