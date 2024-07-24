package com.app.server.data.dto.promo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EstimatePromo {
    private Double total_begin_price = 0.0;
    private List<PromoProductBasic> promoProductInputList = new ArrayList<>();
    private List<PromoProductBasic> promoGoodsSelectList = new ArrayList<>();
    private Double total_refund_price = 0.0;
}