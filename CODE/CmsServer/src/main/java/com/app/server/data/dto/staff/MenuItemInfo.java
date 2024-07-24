package com.app.server.data.dto.staff;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class MenuItemInfo {
    private int id;
    private String name;
    private String code;
    private int level;
    private int parent_id;
    private int priority;
    private List<MenuItemInfo> children = new ArrayList<>();
}