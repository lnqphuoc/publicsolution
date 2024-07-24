package com.app.server.data.dto.staff;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class MenuData {
    private int id;
    private String code;
    private String name;
    private int parent_id;
    private int level;
    private String type;
    private int priority;
    private Map<Integer, MenuData> children = new HashMap<>();
}