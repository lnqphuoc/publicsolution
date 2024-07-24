package com.app.server.data.dto.program.product;

import com.app.server.data.dto.program.ProgramItemType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProgramProduct {
    private Product product;
    private int netPrice;
    private String description;
    private int quantity;
    private int maxOfferPerPromo;
    private int maxOfferPerAgency;
    private double productPrice;
    private int itemId;
    private ProgramItemType itemType;
    private int categoryLevel;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ProgramProduct p = (ProgramProduct) o;
        return product.getId() == p.getProduct().getId();
    }

    @Override
    public int hashCode() {
        return product.getId();
    }
}