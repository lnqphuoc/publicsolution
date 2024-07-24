package com.app.server.data.dto.staff;

import lombok.Data;

@Data
public class StaffProfile {
    private int id;
    private String full_name;
    private String name;
    private String email;
}