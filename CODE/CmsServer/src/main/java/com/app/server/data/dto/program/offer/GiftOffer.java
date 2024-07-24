package com.app.server.data.dto.program.offer;

import com.app.server.data.dto.program.product.OfferProduct;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class GiftOffer extends ProgramOffer {
    private List<OfferProduct> ltBonusGift;

    public GiftOffer() {
        ltBonusGift = new ArrayList<>();
    }
}