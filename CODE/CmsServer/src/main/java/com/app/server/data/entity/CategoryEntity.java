package com.app.server.data.entity;

import io.swagger.models.auth.In;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

@Data
public class CategoryEntity {
    private Integer id;
    private String name;
    private int parent_id;
    private String image;
    private int is_branch;
    private int priority;
    private int parent_priority;
    private int category_level;
    private int status;
    private String created_date;
}