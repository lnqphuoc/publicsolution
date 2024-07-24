package com.app.server.data.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import static javax.persistence.GenerationType.IDENTITY;

@Data
public class ProductGroupEntity {
    private Integer id;
    private String code;
    private String name;
    private String similar_name;
    private int status;
    private int category_id;
    private String created_date;
    private String sort_data;
    private Integer business_department_id;
}