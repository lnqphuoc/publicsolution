package com.app.server.data.dto.program.product;

import com.app.server.data.dto.program.Program;
import com.app.server.enums.SanSaleItemType;
import com.app.server.enums.SanSaleOfferType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SanSaleItem {
    private Program program;
    private SanSaleItemType type;
    private ProgramProduct product;
    private ProgramProductGroup combo;
    private double moneyDiscount;
    private int percentDiscount;
    private long oldPrice;
    private double currentPrice;
    private SanSaleOfferType offerType;

    public SanSaleItem() {
        offerType = SanSaleOfferType.NONE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (type == SanSaleItemType.PRODUCT) {
            ProgramProduct product = (ProgramProduct) ((SanSaleItem) o).getProduct();
            return this.product.getProduct().getId() == product.getProduct().getId();
        }
        if (type == SanSaleItemType.COMBO) {
            ProgramProductGroup combo = (ProgramProductGroup) ((SanSaleItem) o).getCombo();
            return this.combo.getId() == combo.getId();
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (type == SanSaleItemType.PRODUCT)
            return this.product.getProduct().getId();
        else if (type == SanSaleItemType.COMBO)
            return this.combo.getId();
        return 0;
    }
}