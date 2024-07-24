package com.app.server.data.request.staff;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MenuRequest {
    private int id;
    private int allow;
    private List<MenuRequest> children = new ArrayList<>();
}