package com.app.server.data.dto.program.offer;

import com.app.server.data.dto.program.product.OfferProduct;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class GoodsOffer extends ProgramOffer {
    private int quantity;
    private List<OfferProduct> ltGoods;
    private List<OfferProduct> ltBonusGoods;
    private List<OfferProduct> ltBonusGift;

    public GoodsOffer() {
        ltGoods = new ArrayList<>();
        ltBonusGoods = new ArrayList<>();
        ltBonusGift = new ArrayList<>();
    }
}