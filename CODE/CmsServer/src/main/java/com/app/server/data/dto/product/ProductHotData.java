package com.app.server.data.dto.product;

import com.app.server.data.dto.staff.Staff;
import com.app.server.data.dto.staff.StaffProfile;
import lombok.Data;

@Data
public class ProductHotData {
    private int id;
    private String full_name;
    private String code;
    private String images;
    private String hot_label;
    private Integer hot_priority;
    private String hot_date;
    private String hot_modifier_id;
    private StaffProfile creator_info;
}