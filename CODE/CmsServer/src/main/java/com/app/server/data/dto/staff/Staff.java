package com.app.server.data.dto.staff;

import lombok.Data;

@Data
public class Staff {
    private int id;
    private String full_name;
    private String name;
    private String email;
    private String password;
    private int status;
    private int staff_group_permission_id;
    private int department_id;
}