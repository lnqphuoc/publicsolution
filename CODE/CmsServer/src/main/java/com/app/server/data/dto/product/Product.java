package com.app.server.data.dto.product;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Product {
    private int id;
    private String name;
    private String image;
    private int price;
}