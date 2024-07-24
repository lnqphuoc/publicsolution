package com.app.server.data.entity;

import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import static javax.persistence.GenerationType.IDENTITY;

@Data
public class ConfigEntity {
    private Integer id;
    private String code;
    private String name;
    private String data;
}