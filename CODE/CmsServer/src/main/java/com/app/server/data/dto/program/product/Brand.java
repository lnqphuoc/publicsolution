package com.app.server.data.dto.program.product;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class Brand {
    private int id;
    private String name;
    private String image;
    private int isHighlight;
    private int highlightPriority;
    private int status;
    private Date createdDate;
    private List<Product> ltProduct;

    public Brand() {
        ltProduct = new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Brand brand = (Brand) o;
        return id == brand.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}