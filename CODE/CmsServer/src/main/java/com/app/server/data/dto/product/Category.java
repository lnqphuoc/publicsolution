package com.app.server.data.dto.product;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Category {
    private int id;
    private String name;
    private String image;
    private String image_url;
    private int is_branch;
    private int parent_id;
    private int category_level;
    private int priority;
    private int parent_priority;
    private int status;
    private List<Integer> ltSub;
    private List<Product> ltProduct;
    private String parent_name;
    private Integer business_department_id;

    public Category() {
        ltSub = new ArrayList<>();
        ltProduct = new ArrayList<>();
    }
}