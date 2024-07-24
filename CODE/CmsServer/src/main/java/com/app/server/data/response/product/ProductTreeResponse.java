package com.app.server.data.response.product;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProductTreeResponse {
    private int id;
    private String name;
    private String code;
    private String images;
    private Long price;
    private int item_type;
    private List<ProductTreeResponse> childs = new ArrayList<>();
}