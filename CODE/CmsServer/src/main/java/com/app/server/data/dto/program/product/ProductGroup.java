package com.app.server.data.dto.program.product;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class ProductGroup {
    private int id;
    private String code;
    private String name;
    private int status;
    private Date createdDate;
    private int itemType;
    private String similarName;
    private int productNumber;
    private String sortData;
    private Category category;
    private List<Product> ltProduct;

    public ProductGroup() {
        ltProduct = new ArrayList<>();
    }

    // set data
    public void setData(ProductGroup productGroup) {
        this.code = productGroup.getCode();
        this.name = productGroup.getName();
        this.status = productGroup.getStatus();
        this.createdDate = productGroup.getCreatedDate();
        this.itemType = productGroup.getItemType();
        this.similarName = productGroup.getSimilarName();
        this.productNumber = productGroup.getProductNumber();
        this.sortData = productGroup.getSortData();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ProductGroup productGroup = (ProductGroup) o;
        return id == productGroup.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}