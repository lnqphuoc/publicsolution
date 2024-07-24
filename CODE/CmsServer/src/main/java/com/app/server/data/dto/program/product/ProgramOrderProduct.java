package com.app.server.data.dto.program.product;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProgramOrderProduct {
    private Product product;
    private int productQuantity;
    private int productId;
    private double beginPrice;
    private double productPrice;
    private double commonPrice;

    public double getProductPrice() {
        return product.getPrice();
    }
}