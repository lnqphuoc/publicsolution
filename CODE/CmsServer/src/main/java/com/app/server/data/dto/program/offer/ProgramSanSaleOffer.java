package com.app.server.data.dto.program.offer;

import com.app.server.data.dto.program.product.OfferProduct;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ProgramSanSaleOffer {
    private Map<Integer, Integer> mpProductQuantity = new LinkedHashMap<>(); // danh sách sản phẩm và số lượng ăn chương trình
    private Map<Integer, Integer> mpComboQuantity = new LinkedHashMap<>(); // danh sách combo và số lượng ăn chương trình
    private Map<Integer, Integer> mpProductPercentDiscount; // danh sách chiết khấu theo từng sản phẩm
    private Map<Integer, Double> mpProductMoneyDiscount; // danh sách giảm tiền theo từng sản phẩm
    private List<OfferProduct> ltBonusGift; // danh sách quà tặng kèm theo
    private Map<Integer, Double> mpProductFixedPrice; // danh sách giá cố định cho từng sản phẩm

    public ProgramSanSaleOffer() {
        this.mpProductQuantity = new LinkedHashMap<>();
        this.mpComboQuantity = new LinkedHashMap<>();
        this.mpProductPercentDiscount = new LinkedHashMap<>();
        this.mpProductMoneyDiscount = new LinkedHashMap<>();
        this.ltBonusGift = new ArrayList<>();
        this.mpProductFixedPrice = new LinkedHashMap<>();
    }
}