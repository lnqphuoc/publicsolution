package com.app.server.data.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import static javax.persistence.GenerationType.IDENTITY;

@Data
@Entity
@Table(name = "product_small_unit")
public class ProductSmallUnitEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;
    private String name;
}