package com.app.server.data.dto.promo;

import com.app.server.data.dto.program.Program;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class ResultEstimatePromo {
    private double total_begin_price = 0;
    private double total_refund_price = 0;
    private List<PromoProductBasic> promoProductInputList = new ArrayList<>();
    private List<PromoProductBasic> promoGoodsSelectedList = new ArrayList<>();

    private List<PromoProductBasic> promoGoodsOfferList = new ArrayList<>();
    private List<PromoBasicData> csbhClaimList = new ArrayList<>();


    private Double totalMoneyDiscountProductOfferByCSBH = 0.0;
    private Double totalMoneyDiscountProductOfferByCTKM = 0.0;
    private Double totalMoneyDiscountOrderOfferByCSBH = 0.0;
    private Double totalMoneyDiscountOrderOfferByCTKM = 0.0;
    private Double totalMoneyRefundOffer = 0.0;
    private List<PromoGiftClaim> promoGiftClaimList = new ArrayList<>();
    private List<PromoBasicData> promoClaims = new ArrayList<>();
    private Map<Integer, Double> mpPromoProductPriceCSBH = new LinkedHashMap<>();
    private Map<Integer, Double> mpPromoProductPriceCTKM = new LinkedHashMap<>();
    private Map<Integer, List<Program>> mpPromoProductCSBH = new LinkedHashMap<>();
    private Map<Integer, List<Program>> mpPromoProductCTKM = new LinkedHashMap<>();
    private List<Program> csbhOrderList = new ArrayList<>();
    private List<Program> ctkmOrderList = new ArrayList<>();
    private List<Program> csbhGoodList = new ArrayList<>();
    private List<Program> ctkmGoodList = new ArrayList<>();

    private List<Program> allProgram = new ArrayList<>();

    private Double totalMoneyGoodsOffer = 0.0;

    private Map<Integer, Double> mpPromoProductPriceCSDM = new LinkedHashMap<>();
    private Map<Integer, List<Program>> mpPromoProductCSDM = new LinkedHashMap<>();
}