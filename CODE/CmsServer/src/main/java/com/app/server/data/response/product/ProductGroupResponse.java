package com.app.server.data.response.product;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductGroupResponse {
    private int id;
    private String code;
    private String name;
    private String similar_name;
    private int status;
    private int item_type;
    private int category_id;
}