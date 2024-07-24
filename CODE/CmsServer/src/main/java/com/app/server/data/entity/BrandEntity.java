package com.app.server.data.entity;

import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import static javax.persistence.GenerationType.IDENTITY;

@Data
public class BrandEntity {
    private Integer id;
    private String name;
    private String image;
    private int is_highlight;
    private int highlight_priority;
    private int status;
    private String created_date;
}