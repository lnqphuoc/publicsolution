package com.app.server.data.dto.promo;

import com.app.server.data.dto.program.Program;
import com.app.server.data.dto.program.product.OfferProduct;
import com.app.server.enums.PromoOfferType;
import com.app.server.utils.JsonUtils;
import com.ygame.framework.common.LogUtil;
import lombok.Data;

import java.util.*;

@Data
public class EstimatePromoData {
    /**
     * tổng tạm tính
     */
    private double total_begin_price = 0;
    private double total_refund_price = 0;
    /**
     * Danh sach san pham duoc giam gia CSBH
     * Map:product_id-so tien duoc giam
     */
    private Map<Integer, Long> mpResponseProductPriceCSBH = new LinkedHashMap<>();
    /**
     * Danh sach san pham duoc giam gia CTKM
     * Map:product_id-so tien duoc giam
     */
    private Map<Integer, Long> mpResponseProductPriceCTKM = new LinkedHashMap<>();
    private List<OfferProduct> ltResponseGoods = new LinkedList<>();
    private List<OfferProduct> ltResponseBonusGoods = new ArrayList<>();
    private List<OfferProduct> ltResponseBonusGift = new LinkedList<>();
    Map<Integer, List<Program>> ltMatchedSalePolicyForProduct = new LinkedHashMap<>();
    Map<Integer, List<Program>> ltMatchedCTKMForProduct = new LinkedHashMap<>();
    List<Program> ltCSBHMatchedForOrder = new ArrayList<>();
    List<Program> ltCTKMMatchedForOrder = new ArrayList<>();
    private double totalMoneyRemainClaimGift;
    private double totalMoneyOfferClaimGift;
    private double totalOrderPriceByCSBH;
    private double totalOrderPriceByCTKM;

    /**
     * CSBH tặng hàng
     */
    private List<Program> csbhGoodsOfferList = new ArrayList<>();
    /**
     * CTKM tặng hàng
     */
    private List<Program> ctkmGoodsOfferList = new ArrayList<>();

    /**
     * all program
     */
    private List<Program> allProgram = new ArrayList<>();

    private Map<Integer, Long> mpResponseProductPriceCSDM = new LinkedHashMap<>();
    Map<Integer, List<Program>> ltMatchedCSDMForProduct = new LinkedHashMap<>();

    public void log() {
        LogUtil.printDebug("totalMoneyRemainClaimGift - " + totalMoneyRemainClaimGift);
        for (OfferProduct offerProduct : ltResponseGoods) {
            LogUtil.printDebug("ltResponseGoodsForProduct-" + offerProduct.getProduct().getFullName() + " " + offerProduct.getQuantity());
        }
        for (OfferProduct offerProduct : ltResponseBonusGoods) {
            LogUtil.printDebug("ltResponseBonusGoodsForProduct-" + offerProduct.getProduct().getFullName() + " " + offerProduct.getQuantity());
        }
        for (OfferProduct offerProduct : ltResponseBonusGift) {
            LogUtil.printDebug("ltResponseBonusGiftForProduct-" + offerProduct.getProduct().getFullName() + " " + offerProduct.getQuantity());
        }
        for (Map.Entry<Integer, Long> pair : mpResponseProductPriceCSBH.entrySet()) {
            LogUtil.printDebug("mpResponseProductPrice-" + pair.getKey() + "-" + pair.getValue());
        }
        for (Map.Entry<Integer, Long> pair : mpResponseProductPriceCTKM.entrySet()) {
            LogUtil.printDebug("mpResponseProductPrice-" + pair.getKey() + "-" + pair.getValue());
        }

        for (List<Program> programs : ltMatchedSalePolicyForProduct.values()) {
            for (Program prgram :
                    programs) {
                LogUtil.printDebug("ltMatchedSalePolicyForProduct: " + prgram.getCode());
            }
        }

        for (List<Program> programs : ltMatchedCTKMForProduct.values()) {
            for (Program prgram :
                    programs) {
                LogUtil.printDebug("ltMatchedCTKMForProduct: " + prgram.getCode());
            }
        }

        for (Map.Entry<Integer, Long> pair : mpResponseProductPriceCSDM.entrySet()) {
            LogUtil.printDebug("mpResponseProductPriceCSDM-" + pair.getKey() + "-" + pair.getValue());
        }
    }
}