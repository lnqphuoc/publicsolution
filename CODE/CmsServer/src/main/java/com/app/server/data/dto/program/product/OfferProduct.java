package com.app.server.data.dto.program.product;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OfferProduct {
    private Product product;
    private int quantity;
    private double price;

    public OfferProduct() {
    }

    public OfferProduct(OfferProduct offerProduct) {
        this.product = offerProduct.getProduct();
        this.quantity = offerProduct.getQuantity();
        this.price = offerProduct.getPrice();
    }

    // get category's parent priority
    public int getProductCategoryParentPriority() {
        return product.getProductGroup().getCategory().getParentPriority();
    }

    // get category's priority
    public int getProductCategoryPriority() {
        return product.getProductGroup().getCategory().getPriority();
    }

    // get product group's sort data
    public String getProductGroupSortData() {
        return product.getProductGroup().getSortData();
    }

    // get product's sort data
    public String getProductSortData() {
        return product.getSortData();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass())
            return false;
        OfferProduct p = (OfferProduct) o;
        return product.getId() == p.getProduct().getId();
    }

    @Override
    public int hashCode() {
        return product.getId();
    }
}