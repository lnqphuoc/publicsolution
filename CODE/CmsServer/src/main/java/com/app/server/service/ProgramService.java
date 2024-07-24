package com.app.server.service;

import com.app.server.data.dto.product.ProductCache;
import com.app.server.data.dto.program.*;
import com.app.server.data.dto.program.agency.Agency;
import com.app.server.data.dto.program.filter.ProgramFilter;
import com.app.server.data.dto.program.filter.ProgramFilterType;
import com.app.server.data.dto.program.limit.ProgramLimit;
import com.app.server.data.dto.program.offer.*;
import com.app.server.data.dto.program.product.*;
import com.app.server.data.request.promo.CreatePromoRequest;
import com.app.server.data.request.promo.PromoLimitRequest;
import com.app.server.enums.*;
import com.app.server.utils.JsonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ProgramService extends BaseService {
    /**
     * Tính chính sách bán hàng cho sản phẩm
     */
    public long checkSalePolicyForProduct(Agency agency,
                                          Map<Integer, ProgramOrderProduct> mpProgramOrderProduct, // danh sách sản phẩm dùng đểm kiểm tra csbh
                                          Map<Integer, Long> mpResponsePromotionProductPrice, // danh sách id sản phẩm và giá giảm ( bao gồm giảm giá + chiết khấu )
                                          List<OfferProduct> ltResponseGoods, // danh sách hàng tặng
                                          List<OfferProduct> ltResponseBonusGoods, // danh sách hàng tặng kèm theo
                                          List<OfferProduct> ltResponseBonusGift, // danh sách quà tặng kèm theo
                                          Map<Integer, List<Program>> mpResponseProgramMatchedProduct, // sản phẩm và danh sách csbh tương ứng
                                          Source source, // nguồn từ app hay cms
                                          List<Program> ltResponseGoodsOfferMatchedProgram, // danh sách csbh có hàng tặng
                                          List<Program> ltResponseAllMatchedProgram) {  // danh sách tất cả csbh được ăn
        long totalRestGoodsOfferPrice = 0;
        if (mpProgramOrderProduct.isEmpty())
            return totalRestGoodsOfferPrice;
        if (agency.isBlockCsbh())
            return totalRestGoodsOfferPrice;
        try {
            Collection<Program> ltSalePolicy = this.dataManager.getProgramManager().getMpSalePolicy().values();
            // Lấy thông tin công nợ hiện tại của đại lý
            DeptInfo deptInfo = this.dataManager.getProgramManager().getDeptInfo(agency.getId());
            for (Program program : ltSalePolicy) {
                if (program.isRunning() && (program.getConditionType() == ProgramConditionType.PRODUCT_QUANTITY || program.getConditionType() == ProgramConditionType.PRODUCT_PRICE)) {
                    if (checkProgramFilter(agency, program, source, deptInfo)) {
                        List<ProgramOffer> ltProgramOffer = handleProgramForProduct(program, mpProgramOrderProduct);
                        if (!ltProgramOffer.isEmpty())
                            ltResponseAllMatchedProgram.add(program);
                        totalRestGoodsOfferPrice += handleProgramOfferForProduct(agency, ltProgramOffer, mpProgramOrderProduct, mpResponsePromotionProductPrice, ltResponseGoods,
                                ltResponseBonusGoods, ltResponseBonusGift, mpResponseProgramMatchedProduct, ltResponseGoodsOfferMatchedProgram, new ArrayList<>());
                    }
                }
            }
            // Sắp xếp hàng tặng, quà tặng...
            Comparator<OfferProduct> compareByOfferProduct = this.sortUtil.getOfferProductComparator();
            if (!ltResponseGoods.isEmpty())
                ltResponseGoods.sort(compareByOfferProduct);
            if (!ltResponseBonusGoods.isEmpty())
                ltResponseBonusGoods.sort(compareByOfferProduct);
            if (!ltResponseBonusGift.isEmpty())
                ltResponseBonusGift.sort(compareByOfferProduct);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return totalRestGoodsOfferPrice;
    }

    public long checkSalePolicyForProductHuntSale(Agency agency,
                                                  Map<Integer, ProgramOrderProduct> mpProgramOrderProduct, // danh sách sản phẩm dùng đểm kiểm tra csbh
                                                  Map<Integer, Long> mpResponsePromotionProductPrice, // danh sách id sản phẩm và giá giảm ( bao gồm giảm giá + chiết khấu )
                                                  List<OfferProduct> ltResponseGoods, // danh sách hàng tặng
                                                  List<OfferProduct> ltResponseBonusGoods, // danh sách hàng tặng kèm theo
                                                  List<OfferProduct> ltResponseBonusGift, // danh sách quà tặng kèm theo
                                                  Map<Integer, List<Program>> mpResponseProgramMatchedProduct, // sản phẩm và danh sách csbh tương ứng
                                                  Source source, // nguồn từ app hay cms
                                                  List<Program> ltResponseGoodsOfferMatchedProgram, // danh sách csbh có hàng tặng
                                                  List<Program> ltResponseAllMatchedProgram) {  // danh sách tất cả csbh được ăn
        long totalRestGoodsOfferPrice = 0;
        if (mpProgramOrderProduct.isEmpty())
            return totalRestGoodsOfferPrice;
        if (agency.isBlockCsbh())
            return totalRestGoodsOfferPrice;
        try {
            Collection<Program> ltSalePolicy = this.dataManager.getProgramManager().getMpHuntSale().values();
            // Lấy thông tin công nợ hiện tại của đại lý
            DeptInfo deptInfo = this.dataManager.getProgramManager().getDeptInfo(agency.getId());
            for (Program program : ltSalePolicy) {
                if (program.isRunning() && (program.getConditionType() == ProgramConditionType.PRODUCT_QUANTITY || program.getConditionType() == ProgramConditionType.PRODUCT_PRICE)) {
                    if (checkProgramFilter(agency, program, source, deptInfo)) {
                        List<ProgramOffer> ltProgramOffer = handleProgramForProduct(program, mpProgramOrderProduct);
                        if (!ltProgramOffer.isEmpty())
                            ltResponseAllMatchedProgram.add(program);
                        totalRestGoodsOfferPrice += handleProgramOfferForProduct(agency, ltProgramOffer, mpProgramOrderProduct, mpResponsePromotionProductPrice, ltResponseGoods,
                                ltResponseBonusGoods, ltResponseBonusGift, mpResponseProgramMatchedProduct, ltResponseGoodsOfferMatchedProgram, new ArrayList<>());
                    }
                }
            }
            // Sắp xếp hàng tặng, quà tặng...
            Comparator<OfferProduct> compareByOfferProduct = this.sortUtil.getOfferProductComparator();
            if (!ltResponseGoods.isEmpty())
                ltResponseGoods.sort(compareByOfferProduct);
            if (!ltResponseBonusGoods.isEmpty())
                ltResponseBonusGoods.sort(compareByOfferProduct);
            if (!ltResponseBonusGift.isEmpty())
                ltResponseBonusGift.sort(compareByOfferProduct);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return totalRestGoodsOfferPrice;
    }

    /**
     * Tính chính sách bán hàng cho đơn hàng
     */
    public long checkSalePolicyForOrder(Agency agency,
                                        long totalOrderPriceForCheck, // tổng giá trị đơn hàng, dùng để kiểm tra csbh
                                        long totalOrderPriceForCal, // tổng giá trị đơn hàng, dùng để tính toán csbh
                                        Map<Integer, ProgramOrderProduct> mpProgramOrderProduct, // danh sách sản phẩm dùng đểm kiểm tra csbh
                                        Map<Integer, Long> mpPromotionProductPrice, // danh sách id sản phẩm và giá giảm ( bao gồm giảm giá + chiết khấu ) từ csbh cho sp
                                        List<OfferProduct> ltResponseBonusGoods, // danh sách hàng tặng kèm theo
                                        List<OfferProduct> ltResponseBonusGift, // danh sách quà tặng kèm theo
                                        List<Program> ltResponseDiscountOfferMatchedProgram, // danh sách csbh có giảm giá và chiết khấu
                                        Source source, // nguồn từ app hay cms
                                        List<Program> ltResponseGoodsOfferMatchedProgram, // danh sách csbh có hàng tặng kèm theo
                                        List<Program> ltResponseAllMatchedProgram) {  // danh sách tất cả csbh được ăn
        long totalMoney = 0;
        if (agency.isBlockCsbh())
            return totalMoney;
        try {
            Collection<Program> ltSalePolicy = this.dataManager.getProgramManager().getMpSalePolicy().values();
            // Lấy thông tin công nợ hiện tại của đại lý
            DeptInfo deptInfo = this.dataManager.getProgramManager().getDeptInfo(agency.getId());
            for (Program program : ltSalePolicy) {
                long totalInitPriceForCheck = totalOrderPriceForCheck;
                long totalInitPriceForCal = totalOrderPriceForCal;
                if (program.isRunning() && program.getConditionType() == ProgramConditionType.ORDER_PRICE) {
                    if (checkProgramFilter(agency, program, source, deptInfo)) {
                        for (Integer productId : program.getLtIgnoreProductId()) {
                            // Nếu sản phẩm nằm trong loại trừ, trừ đi giá của sản phẩm đó trước khi xét csbh
                            if (mpProgramOrderProduct.containsKey(productId)) {
                                long promotionPrice = 0;
                                if (mpPromotionProductPrice.containsKey(productId))
                                    promotionPrice = mpPromotionProductPrice.get(productId);
                                totalInitPriceForCheck -= (mpProgramOrderProduct.get(productId).getBeginPrice() - promotionPrice);
                                totalInitPriceForCal -= (mpProgramOrderProduct.get(productId).getBeginPrice() - promotionPrice);
                            }
                        }
                        List<ProgramOffer> ltProgramOffer = handleProgramForOrder(program, totalInitPriceForCheck);
                        if (!ltProgramOffer.isEmpty())
                            ltResponseAllMatchedProgram.add(program);
                        long money = handleProgramOfferForOrder(ltProgramOffer, totalInitPriceForCal, ltResponseBonusGoods, ltResponseBonusGift, ltResponseGoodsOfferMatchedProgram);
                        totalMoney += money;
                        if (money != 0)
                            ltResponseDiscountOfferMatchedProgram.add(program);
                    }
                }
            }
            // Sắp xếp hàng tặng, quà tặng...
            Comparator<OfferProduct> compareByOfferProduct = sortUtil.getOfferProductComparator();
            if (!ltResponseBonusGoods.isEmpty())
                ltResponseBonusGoods.sort(compareByOfferProduct);
            if (!ltResponseBonusGift.isEmpty())
                ltResponseBonusGift.sort(compareByOfferProduct);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return totalMoney;
    }

    /**
     * Tính chương trình khuyến mãi cho sản phẩm
     */
    public long checkPromotionForProduct(Agency agency,
                                         Map<Integer, ProgramOrderProduct> mpProgramOrderProduct, // danh sách sản phẩm dùng đểm kiểm tra ctkm
                                         Map<Integer, Long> mpResponsePromotionProductPrice,  // danh sách id sản phẩm và giá giảm ( bao gồm giảm giá + chiết khấu )
                                         List<OfferProduct> ltResponseGoods, // danh sách hàng tặng
                                         List<OfferProduct> ltResponseBonusGoods, // danh sách hàng tặng kèm theo
                                         List<OfferProduct> ltResponseBonusGift, // danh sách quà tặng kèm theo
                                         Map<Integer, List<Program>> mpResponseProgramMatchedProduct, // danh sách sản phẩm kèm theo chương trình chứa nó
                                         Source source,
                                         List<Program> ltResponseGoodsOfferMatchedProgram, // danh sách chương trình có hàng tặng
                                         List<Program> ltResponseDiscountOfferMatchedProgram, // danh sách chương trình có giảm giá và chiết khấu
                                         List<Program> ltResponsePriorityProgram, // danh sách chương trình có độ ưu tiên cao nhất và đã được ăn
                                         List<Program> ltResponseAllMatchedProgram) {  // danh sách tất cả ctkm được ăn
        long totalRestGoodsOfferPrice = 0;
        if (mpProgramOrderProduct.isEmpty())
            return totalRestGoodsOfferPrice;
        try {
            Collection<Program> clPromotion = this.dataManager.getProgramManager().getMpPromotion().values();
            List<Program> ltPromotion = new ArrayList<>(clPromotion);
            sortUtil.sortProgram(ltPromotion);
            // Lấy thông tin công nợ hiện tại của đại lý
            DeptInfo deptInfo = this.dataManager.getProgramManager().getDeptInfo(agency.getId());
            for (Program program : ltPromotion) {
                if (program.isRunning() && checkProgramFilter(agency, program, source, deptInfo)) {
                    boolean valid = program.getConditionType() == ProgramConditionType.PRODUCT_QUANTITY || program.getConditionType() == ProgramConditionType.PRODUCT_PRICE;
                    if (valid) {
                        List<ProgramOffer> ltProgramOffer = handleProgramForProduct(program, mpProgramOrderProduct);
                        if (!ltProgramOffer.isEmpty())
                            ltResponseAllMatchedProgram.add(program);
                        totalRestGoodsOfferPrice += handleProgramOfferForProduct(agency, ltProgramOffer, mpProgramOrderProduct, mpResponsePromotionProductPrice, ltResponseGoods,
                                ltResponseBonusGoods, ltResponseBonusGift, mpResponseProgramMatchedProduct, ltResponseGoodsOfferMatchedProgram, ltResponseDiscountOfferMatchedProgram);
                    }
                    if (program.getPriority() != 0) { // Nếu gặp chương trình khuyến mãi có độ ưu tiên khác 0 đầu tiên thì dừng
                        if (valid)
                            ltResponsePriorityProgram.add(program);
                        break;
                    }
                }
            }
            // Sắp xếp hàng tặng, quà tặng...
            Comparator<OfferProduct> compareByOfferProduct = sortUtil.getOfferProductComparator();
            if (!ltResponseGoods.isEmpty())
                ltResponseGoods.sort(compareByOfferProduct);
            if (!ltResponseBonusGoods.isEmpty())
                ltResponseBonusGoods.sort(compareByOfferProduct);
            if (!ltResponseBonusGift.isEmpty())
                ltResponseBonusGift.sort(compareByOfferProduct);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return totalRestGoodsOfferPrice;
    }

    /**
     * Tính chương trình khuyến mãi cho đơn hàng
     */
    public long checkPromotionForOrder(Agency agency,
                                       long totalOrderPriceForCheck, // tổng giá trị đơn hàng, dùng để kiểm tra ctkm
                                       long totalOrderPriceForCal, // tổng giá trị đơn hàng, dùng để tính toán ctkm
                                       Map<Integer, ProgramOrderProduct> mpProgramOrderProduct, // danh sách sản phẩm dùng đểm kiểm tra ctkm
                                       Map<Integer, Long> mpPromotionProductPrice, // danh sách id sản phẩm và giá giảm ( bao gồm giảm giá + chiết khấu ) từ csbh cho sp
                                       Map<Integer, Long> mpPromotionProductPriceCTKM, // danh sách id sản phẩm và giá giảm ( bao gồm giảm giá + chiết khấu ) từ ctkm cho sp
                                       List<OfferProduct> ltResponseBonusGoods, // danh sách hàng tặng kèm theo
                                       List<OfferProduct> ltResponseBonusGift, // danh sách quà tặng kèm theo
                                       List<Program> ltResponseDiscountOfferMatchedProgram, // danh sách chương trình có giảm giá và chiết khấu
                                       Source source,
                                       List<Program> ltResponseGoodsOfferMatchedProgram, // danh sách chương trình có hàng tặng
                                       List<Program> ltResponsePriorityProgram, // danh sách chương trình có độ ưu tiên cao nhất và đã được ăn
                                       List<Program> ltResponseAllMatchedProgram) {  // danh sách tất cả chương trình được ăn
        long totalMoney = 0;
        try {
            Collection<Program> clPromotion = this.dataManager.getProgramManager().getMpPromotion().values();
            List<Program> ltPromotion = new ArrayList<>(clPromotion);
            sortUtil.sortProgram(ltPromotion);
            // Lấy thông tin công nợ hiện tại của đại lý
            DeptInfo deptInfo = this.dataManager.getProgramManager().getDeptInfo(agency.getId());
            for (Program program : ltPromotion) {
                long totalInitPriceForCheck = totalOrderPriceForCheck;
                long totalInitPriceForCal = totalOrderPriceForCal;
                if (program.isRunning() && checkProgramFilter(agency, program, source, deptInfo)) {
                    boolean valid = (program.getConditionType() == ProgramConditionType.ORDER_PRICE);
                    if (valid && (program.getPriority() == 0 || ltResponsePriorityProgram.isEmpty())) {
                        for (Integer productId : program.getLtIgnoreProductId()) {
                            // nếu sản phẩm nằm trong loại trừ, trừ đi giá của sản phẩm đó trước khi xét ctkm
                            if (mpProgramOrderProduct.containsKey(productId)) {
                                long promotionPrice = 0;
                                if (mpPromotionProductPrice.containsKey(productId)) // lấy giá ăn csbh
                                    promotionPrice += mpPromotionProductPrice.get(productId);
                                if (mpPromotionProductPriceCTKM.containsKey(productId)) // lấy giá ăn ctkm
                                    promotionPrice += mpPromotionProductPriceCTKM.get(productId);
                                totalInitPriceForCheck -= (mpProgramOrderProduct.get(productId).getBeginPrice() - promotionPrice);
                                totalInitPriceForCal -= (mpProgramOrderProduct.get(productId).getBeginPrice() - promotionPrice);
                            }
                        }
                        List<ProgramOffer> ltProgramOffer = handleProgramForOrder(program, totalInitPriceForCheck);
                        if (!ltProgramOffer.isEmpty())
                            ltResponseAllMatchedProgram.add(program);
                        long money = handleProgramOfferForOrder(ltProgramOffer, totalInitPriceForCal, ltResponseBonusGoods, ltResponseBonusGift, ltResponseGoodsOfferMatchedProgram);
                        // kiểm tra giá trị ưu đãi tối đa cho giảm giá và chiết khấu trong phần thiết lập
                        if (program.getPromoMaxValue() > 0 && money > program.getPromoMaxValue())
                            money = program.getPromoMaxValue();
                        totalMoney += money;
                        if (money != 0)
                            ltResponseDiscountOfferMatchedProgram.add(program);
                        if (program.getPriority() != 0)
                            break;
                    }
                }
            }
            Comparator<OfferProduct> compareByOfferProduct = sortUtil.getOfferProductComparator();
            if (!ltResponseBonusGoods.isEmpty())
                ltResponseBonusGoods.sort(compareByOfferProduct);
            if (!ltResponseBonusGift.isEmpty())
                ltResponseBonusGift.sort(compareByOfferProduct);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return totalMoney;
    }

    /**
     * Xử lý phần dữ liệu cho sản phẩm trong chương trình
     */
    private List<ProgramOffer> handleProgramForProduct(Program program, Map<Integer, ProgramOrderProduct> mpProgramOrderProduct) {
        List<ProgramOffer> ltProgramOffer = new ArrayList<>();
        try {
            if (program.getConditionType() == ProgramConditionType.PRODUCT_QUANTITY) {
                Map<Integer, Long> mpProductGroupQuantity = new LinkedHashMap<>();
                // Kiểm tra hạn mức cao nhất
                for (ProgramLimit programLimit : program.getLtProgramLimit()) {
                    boolean isExit = false;
                    for (ProgramProductGroup programProductGroup : programLimit.getLtProgramProductGroup()) {
                        long totalProductQuantity = programProductGroup.getLtProgramProduct().stream()
                                .filter(object -> mpProgramOrderProduct.containsKey(object.getProduct().getId()))
                                .map(object -> mpProgramOrderProduct.get(object.getProduct().getId()).getProductQuantity())
                                .reduce(0, Integer::sum);
                        if (totalProductQuantity >= programProductGroup.getFromValue() && !program.isEndLimit()) {
                            if (programProductGroup.getOffer().getOfferType() == ProgramOfferType.MONEY_DISCOUNT ||
                                    programProductGroup.getOffer().getOfferType() == ProgramOfferType.PERCENT_DISCOUNT)
                                isExit = true;
                            else if (programProductGroup.getOffer().getOfferType() == ProgramOfferType.GOODS_OFFER &&
                                    program.getGoodsType() == ProgramGoodsType.CONVERSION)
                                isExit = true;
                            ltProgramOffer.add(programProductGroup.getOffer());
                            totalProductQuantity -= programProductGroup.getFromValue();
                        } else if (totalProductQuantity >= programProductGroup.getFromValue() && programProductGroup.getEndValue() > 0) {
                            if (programProductGroup.getOffer().getOfferType() == ProgramOfferType.MONEY_DISCOUNT ||
                                    programProductGroup.getOffer().getOfferType() == ProgramOfferType.PERCENT_DISCOUNT)
                                isExit = true;
                            else if (programProductGroup.getOffer().getOfferType() == ProgramOfferType.GOODS_OFFER &&
                                    program.getGoodsType() == ProgramGoodsType.CONVERSION)
                                isExit = true;
                            ltProgramOffer.add(programProductGroup.getOffer());
                            totalProductQuantity -= programProductGroup.getUnderEndValue();
                            if (totalProductQuantity < 0)
                                totalProductQuantity = 0;
                        } else if (totalProductQuantity >= programProductGroup.getFromValue()) {
                            isExit = true;
                            ltProgramOffer.add(programProductGroup.getOffer());
                            totalProductQuantity = 0;
                        }
                        mpProductGroupQuantity.put(programProductGroup.getDataIndex(), totalProductQuantity);
                    }
                    if (isExit)
                        return ltProgramOffer;
                    break;
                }
                // Kiểm tra bước nhảy
                if (!ltProgramOffer.isEmpty() && program.getLtStepLimit() != null) {
                    for (ProgramProductGroup programProductGroup : program.getLtStepLimit().getLtProgramProductGroup()) {
                        long totalProductQuantity = 0;
                        if (mpProductGroupQuantity.containsKey(programProductGroup.getDataIndex()))
                            totalProductQuantity = mpProductGroupQuantity.get(programProductGroup.getDataIndex());
                        while (totalProductQuantity >= programProductGroup.getFromValue()) {
                            ltProgramOffer.add(programProductGroup.getOffer());
                            totalProductQuantity -= programProductGroup.getFromValue();
                        }
                        mpProductGroupQuantity.put(programProductGroup.getDataIndex(), totalProductQuantity);
                    }
                }
                // Kiểm tra tất cả hạn mức
                for (ProgramLimit programLimit : program.getLtProgramLimit()) {
                    boolean isExit = false;
                    for (ProgramProductGroup programProductGroup : programLimit.getLtProgramProductGroup()) {
                        long totalProductQuantity = 0;
                        if (mpProductGroupQuantity.containsKey(programProductGroup.getDataIndex()))
                            totalProductQuantity = mpProductGroupQuantity.get(programProductGroup.getDataIndex());
                        if (totalProductQuantity >= programProductGroup.getFromValue() && !program.isEndLimit()) {
                            long offerNumber = totalProductQuantity / programProductGroup.getFromValue();
                            if (programProductGroup.getOffer().getOfferType() == ProgramOfferType.MONEY_DISCOUNT ||
                                    programProductGroup.getOffer().getOfferType() == ProgramOfferType.PERCENT_DISCOUNT) {
                                offerNumber = 1;
                                isExit = true;
                            } else if (programProductGroup.getOffer().getOfferType() == ProgramOfferType.GOODS_OFFER &&
                                    program.getGoodsType() == ProgramGoodsType.CONVERSION) {
                                offerNumber = 1;
                                isExit = true;
                            }
                            for (int i = 1; i <= offerNumber; i++) {
                                ltProgramOffer.add(programProductGroup.getOffer());
                                totalProductQuantity -= programProductGroup.getFromValue();
                            }
                        } else if (totalProductQuantity >= programProductGroup.getFromValue() && programProductGroup.getEndValue() > 0) {
                            long offerNumber = 1;
                            long endQuantity = programProductGroup.getUnderEndValue();
                            if (totalProductQuantity > endQuantity)
                                offerNumber = totalProductQuantity / endQuantity;
                            if (programProductGroup.getOffer().getOfferType() == ProgramOfferType.MONEY_DISCOUNT ||
                                    programProductGroup.getOffer().getOfferType() == ProgramOfferType.PERCENT_DISCOUNT) {
                                offerNumber = 1;
                                isExit = true;
                            } else if (programProductGroup.getOffer().getOfferType() == ProgramOfferType.GOODS_OFFER &&
                                    program.getGoodsType() == ProgramGoodsType.CONVERSION) {
                                offerNumber = 1;
                                isExit = true;
                            }
                            for (int i = 1; i <= offerNumber; i++) {
                                ltProgramOffer.add(programProductGroup.getOffer());
                                totalProductQuantity -= endQuantity;
                                if (totalProductQuantity < 0)
                                    totalProductQuantity = 0;
                            }
                        } else if (totalProductQuantity >= programProductGroup.getFromValue()) {
                            isExit = true;
                            ltProgramOffer.add(programProductGroup.getOffer());
                            totalProductQuantity = 0;
                        }
                        mpProductGroupQuantity.put(programProductGroup.getDataIndex(), totalProductQuantity);
                    }
                    if (isExit)
                        return ltProgramOffer;
                }
            } else if (program.getConditionType() == ProgramConditionType.PRODUCT_PRICE) {
                Map<Integer, Double> mpProductGroupPrice = new LinkedHashMap<>();
                // Kiểm tra hạn mức cao nhất
                for (ProgramLimit programLimit : program.getLtProgramLimit()) {
                    boolean isExit = false;
                    for (ProgramProductGroup programProductGroup : programLimit.getLtProgramProductGroup()) {
                        double totalProductPrice = programProductGroup.getLtProgramProduct().stream()
                                .filter(object -> mpProgramOrderProduct.containsKey(object.getProduct().getId()))
                                .map(object -> mpProgramOrderProduct.get(object.getProduct().getId()).getBeginPrice())
                                .reduce(0.0, Double::sum);
                        if (totalProductPrice >= programProductGroup.getFromValue() && !program.isEndLimit()) {
                            if (programProductGroup.getOffer().getOfferType() == ProgramOfferType.MONEY_DISCOUNT ||
                                    programProductGroup.getOffer().getOfferType() == ProgramOfferType.PERCENT_DISCOUNT)
                                isExit = true;
                            else if (programProductGroup.getOffer().getOfferType() == ProgramOfferType.GOODS_OFFER &&
                                    program.getGoodsType() == ProgramGoodsType.CONVERSION)
                                isExit = true;
                            ltProgramOffer.add(programProductGroup.getOffer());
                            totalProductPrice -= programProductGroup.getFromValue();
                        } else if (totalProductPrice >= programProductGroup.getFromValue() && programProductGroup.getEndValue() > 0) {
                            if (programProductGroup.getOffer().getOfferType() == ProgramOfferType.MONEY_DISCOUNT ||
                                    programProductGroup.getOffer().getOfferType() == ProgramOfferType.PERCENT_DISCOUNT)
                                isExit = true;
                            else if (programProductGroup.getOffer().getOfferType() == ProgramOfferType.GOODS_OFFER &&
                                    program.getGoodsType() == ProgramGoodsType.CONVERSION)
                                isExit = true;
                            ltProgramOffer.add(programProductGroup.getOffer());
                            totalProductPrice -= programProductGroup.getUnderEndValue();
                            if (totalProductPrice < 0)
                                totalProductPrice = 0;
                        } else if (totalProductPrice >= programProductGroup.getFromValue()) {
                            isExit = true;
                            ltProgramOffer.add(programProductGroup.getOffer());
                            totalProductPrice = 0;
                        }
                        mpProductGroupPrice.put(programProductGroup.getDataIndex(), totalProductPrice);
                    }
                    if (isExit)
                        return ltProgramOffer;
                    break;
                }
                // Kiểm tra tất cả hạn mức
                for (ProgramLimit programLimit : program.getLtProgramLimit()) {
                    boolean isExit = false;
                    for (ProgramProductGroup programProductGroup : programLimit.getLtProgramProductGroup()) {
                        double totalProductPrice = 0;
                        if (mpProductGroupPrice.containsKey(programProductGroup.getDataIndex()))
                            totalProductPrice = mpProductGroupPrice.get(programProductGroup.getDataIndex());
                        if (totalProductPrice >= programProductGroup.getFromValue() && !program.isEndLimit()) {
                            int offerNumber = (int) (totalProductPrice / programProductGroup.getFromValue());
                            if (programProductGroup.getOffer().getOfferType() == ProgramOfferType.MONEY_DISCOUNT ||
                                    programProductGroup.getOffer().getOfferType() == ProgramOfferType.PERCENT_DISCOUNT) {
                                offerNumber = 1;
                                isExit = true;
                            } else if (programProductGroup.getOffer().getOfferType() == ProgramOfferType.GOODS_OFFER &&
                                    program.getGoodsType() == ProgramGoodsType.CONVERSION) {
                                offerNumber = 1;
                                isExit = true;
                            }
                            for (int i = 1; i <= offerNumber; i++) {
                                ltProgramOffer.add(programProductGroup.getOffer());
                                totalProductPrice -= programProductGroup.getFromValue();
                            }
                        } else if (totalProductPrice >= programProductGroup.getFromValue() && programProductGroup.getEndValue() > 0) {
                            int offerNumber = 1;
                            long endPrice = programProductGroup.getUnderEndValue();
                            if (totalProductPrice > endPrice)
                                offerNumber = (int) (totalProductPrice / endPrice);
                            if (programProductGroup.getOffer().getOfferType() == ProgramOfferType.MONEY_DISCOUNT ||
                                    programProductGroup.getOffer().getOfferType() == ProgramOfferType.PERCENT_DISCOUNT) {
                                offerNumber = 1;
                                isExit = true;
                            } else if (programProductGroup.getOffer().getOfferType() == ProgramOfferType.GOODS_OFFER &&
                                    program.getGoodsType() == ProgramGoodsType.CONVERSION) {
                                offerNumber = 1;
                                isExit = true;
                            }
                            for (int i = 1; i <= offerNumber; i++) {
                                ltProgramOffer.add(programProductGroup.getOffer());
                                totalProductPrice -= endPrice;
                                if (totalProductPrice < 0)
                                    totalProductPrice = 0;
                            }
                        } else if (totalProductPrice >= programProductGroup.getFromValue()) {
                            isExit = true;
                            ltProgramOffer.add(programProductGroup.getOffer());
                            totalProductPrice = 0;
                        }
                        mpProductGroupPrice.put(programProductGroup.getDataIndex(), totalProductPrice);
                    }
                    if (isExit)
                        return ltProgramOffer;
                }
            }
        } catch (Exception ex) {
            ltProgramOffer = new ArrayList<>();
            LogUtil.printDebug("", ex);
        }
        return ltProgramOffer;
    }

    /**
     * Xử lý phần dữ liệu cho đơn hàng trong chương trình
     */
    private List<ProgramOffer> handleProgramForOrder(Program salePolicy, long totalOrderPrice) {
        List<ProgramOffer> ltProgramOffer = new ArrayList<>();
        try {
            for (ProgramLimit programLimit : salePolicy.getLtProgramLimit()) {
                boolean isExit = false;
                for (ProgramProductGroup programProductGroup : programLimit.getLtProgramProductGroup()) {
                    if (totalOrderPrice >= programProductGroup.getFromValue() && !salePolicy.isEndLimit()) {
                        int offerNumber = (int) (totalOrderPrice / programProductGroup.getFromValue());
                        if (programProductGroup.getOffer().getOfferType() == ProgramOfferType.PERCENT_DISCOUNT) {
                            offerNumber = 1;
                            isExit = true;
                        }
                        for (int i = 1; i <= offerNumber; i++) {
                            ltProgramOffer.add(programProductGroup.getOffer());
                            totalOrderPrice -= programProductGroup.getFromValue();
                        }
                    } else if (totalOrderPrice >= programProductGroup.getFromValue() && programProductGroup.getEndValue() > 0) {
                        int offerNumber = 1;
                        long endPrice = programProductGroup.getUnderEndValue();
                        if (totalOrderPrice > endPrice) {
                            offerNumber = (int) (totalOrderPrice / endPrice);
                            long mod = totalOrderPrice % endPrice;
                            if (mod >= programProductGroup.getFromValue())
                                offerNumber++;
                        }
                        for (int i = 1; i <= offerNumber; i++) {
                            ltProgramOffer.add(programProductGroup.getOffer());
                            totalOrderPrice -= endPrice;
                            if (totalOrderPrice < 0)
                                totalOrderPrice = 0;
                        }
                    } else if (totalOrderPrice >= programProductGroup.getFromValue()) {
                        ltProgramOffer.add(programProductGroup.getOffer());
                        totalOrderPrice = 0;
                        isExit = true;
                    }
                }
                if (isExit)
                    return ltProgramOffer;
            }
        } catch (Exception ex) {
            ltProgramOffer = new ArrayList<>();
            LogUtil.printDebug("", ex);
        }
        return ltProgramOffer;
    }

    private long handleProgramOfferForProduct(Agency agency, List<ProgramOffer> ltProgramOffer,
                                              Map<Integer, ProgramOrderProduct> mpProgramOrderProduct,
                                              Map<Integer, Long> mpResponsePromotionProductPrice,
                                              List<OfferProduct> ltResponseGoods,
                                              List<OfferProduct> ltResponseBonusGoods,
                                              List<OfferProduct> ltResponseBonusGift,
                                              Map<Integer, List<Program>> mpResponseProgramMatchedProduct,
                                              List<Program> ltResponseGoodsOfferMatchedProgram,
                                              List<Program> ltResponseDiscountOfferMatchedProgram) {
        long totalRestGoodsOfferPrice = 0;
        try {
            for (ProgramOffer programOffer : ltProgramOffer) {

            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return totalRestGoodsOfferPrice;
    }

    private long handleProgramOfferForOrder(List<ProgramOffer> ltProgramOffer,
                                            long totalOrderPrice,
                                            List<OfferProduct> ltResponseBonusGoods,
                                            List<OfferProduct> ltResponseBonusGift,
                                            List<Program> ltResponseGoodsOfferMatchedProgram) {
        long money = 0;
        try {
            for (ProgramOffer programOffer : ltProgramOffer) {
                if (programOffer.getOfferType() == ProgramOfferType.MONEY_DISCOUNT) {
                    MoneyDiscountOffer moneyDiscountOffer = (MoneyDiscountOffer) programOffer;
                    money += moneyDiscountOffer.getMoney();
                } else if (programOffer.getOfferType() == ProgramOfferType.PERCENT_DISCOUNT) {
                    PercentDiscountOffer percentDiscountOffer = (PercentDiscountOffer) programOffer;
                    double tmp = (double) (percentDiscountOffer.getPercent() * totalOrderPrice) / 100;
                    money += new BigDecimal(tmp).setScale(0, RoundingMode.HALF_UP).longValue();
                } else if (programOffer.getOfferType() == ProgramOfferType.GIFT_OFFER) {
                    GiftOffer giftOffer = (GiftOffer) programOffer;
                    for (OfferProduct offerProduct : giftOffer.getLtBonusGift()) {
                        OfferProduct object = new OfferProduct(offerProduct);
                        if (!ltResponseBonusGift.contains(object))
                            ltResponseBonusGift.add(object);
                        else {
                            int index = ltResponseBonusGift.indexOf(object);
                            OfferProduct bonusGift = ltResponseBonusGift.get(index);
                            bonusGift.setQuantity(bonusGift.getQuantity() + object.getQuantity());
                        }
                    }
                } else if (programOffer.getOfferType() == ProgramOfferType.GOODS_OFFER) {
                    GoodsOffer goodsOffer = (GoodsOffer) programOffer;
                    boolean isAllowRefund = false;
                    for (OfferProduct offerProduct : goodsOffer.getLtBonusGift()) {
                        OfferProduct object = new OfferProduct(offerProduct);
                        if (!ltResponseBonusGift.contains(object))
                            ltResponseBonusGift.add(object);
                        else {
                            int index = ltResponseBonusGift.indexOf(object);
                            OfferProduct bonusGift = ltResponseBonusGift.get(index);
                            bonusGift.setQuantity(bonusGift.getQuantity() + object.getQuantity());
                        }
                    }
                    for (OfferProduct offerProduct : goodsOffer.getLtBonusGoods()) {
                        OfferProduct object = new OfferProduct(offerProduct);
                        if (!ltResponseBonusGoods.contains(object))
                            ltResponseBonusGoods.add(object);
                        else {
                            int index = ltResponseBonusGoods.indexOf(object);
                            OfferProduct bonusGoods = ltResponseBonusGoods.get(index);
                            bonusGoods.setQuantity(bonusGoods.getQuantity() + object.getQuantity());
                        }
                        isAllowRefund = true;
                    }
                    // Lưu danh sách chương trình có tặng hàng
                    if (isAllowRefund && !ltResponseGoodsOfferMatchedProgram.contains(programOffer.getProgramProductGroup().getProgramLimit().getProgram()))
                        ltResponseGoodsOfferMatchedProgram.add(programOffer.getProgramProductGroup().getProgramLimit().getProgram());
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return money;
    }

    /**
     * Lấy danh sách chương trình đang chạy theo danh mục sản phẩm
     */
    public List<Program> getProgramByProductCategoryId(int productCategoryId, Agency agency, Source source) {
        List<Program> ltProgram = new ArrayList<>();
        try {
            // Lấy thông tin công nợ hiện tại của đại lý
            DeptInfo deptInfo = this.dataManager.getProgramManager()
                    .getDeptInfo(agency.getId());
            List<Program> ltSalePolicy = this.dataManager.getProgramManager().getMpSalePolicy().values().stream()
                    .filter(program -> program.isRunning()
                            && program.containProductCategoryId(productCategoryId)
                            && checkProgramFilter(agency, program, source, deptInfo))
                    .collect(Collectors.toList());
            List<Program> ltPromotion = this.dataManager.getProgramManager().getMpPromotion().values().stream()
                    .filter(program -> program.isRunning()
                            && program.containProductCategoryId(productCategoryId)
                            && checkProgramFilter(agency, program, source, deptInfo))
                    .collect(Collectors.toList());
            ltProgram.addAll(ltSalePolicy);
            ltProgram.addAll(ltPromotion);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ltProgram;
    }

    /**
     * Lấy thông tin chương trình
     */
    public Program getProgramById(int programId) {
        Program program = this.dataManager.getProgramManager().getMpSalePolicy().get(programId);
        if (program == null)
            program = this.dataManager.getProgramManager().getMpPromotion().get(programId);
        if (program == null) {
            program = this.dataManager.getProgramManager().getMpHuntSale().get(programId);
        }
        if (program == null) {
            program = this.dataManager.getProgramManager().getMpCTTL().get(programId);
        }
        if (program == null) {
            program = this.dataManager.getProgramManager().getMpDamMe().get(programId);
        }
        if (program == null) {
            program = this.dataManager.getProgramManager().getMpCTXH().get(programId);
        }
        return program;
    }

    /**
     * Kiểm tra xem sản phẩm có được phép tham gia chương trình không
     */
    public boolean allowProgram(String productTag) {
        return StringUtils.isBlank(productTag);
    }

    /**
     * Tối ưu danh sách hàng tặng, quà tặng để không trùng nhau
     */
    public List<OfferProduct> optimizeOfferProducts(List<OfferProduct> ltOfferProduct) {
        if (ltOfferProduct.isEmpty())
            return ltOfferProduct;
        List<OfferProduct> ltData = new ArrayList<>();
        try {
            for (OfferProduct offerProduct : ltOfferProduct) {
                if (!ltData.contains(offerProduct))
                    ltData.add(offerProduct);
                else {
                    int index = ltData.indexOf(offerProduct);
                    OfferProduct data = ltData.get(index);
                    data.setQuantity(data.getQuantity() + offerProduct.getQuantity());
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ltData;
    }

    /**
     * Lấy mô tả cho chương trình theo sản phẩm, hiển thị trên app
     */
    public void getAppProgramDescriptionForProduct(int productId, String productTag, Agency agency, Source source,
                                                   List<String> ltSalePolicyDescription,
                                                   List<String> ltPromotionDescription) {
        try {
            if (allowProgram(productTag)) {
                Product product = this.dataManager.getProgramManager().getProductById(productId, productTag);
                Collection<Program> ltSalePolicy = this.dataManager.getProgramManager().getMpSalePolicy().values();
                Collection<Program> ltPromotion = this.dataManager.getProgramManager().getMpPromotion().values();
                // Lấy thông tin công nợ hiện tại của đại lý
                DeptInfo deptInfo = this.dataManager.getProgramManager().getDeptInfo(agency.getId());
                // Lấy danh sách mô tả csbh
                for (Program salePolicy : ltSalePolicy) {
                    if (agency.isBlockCsbh())
                        continue;
                    if (salePolicy.isRunning() && salePolicy.getMpProduct().get(product.getId()) != null && checkProgramFilter(agency, salePolicy, source, deptInfo)) {
                        String description = salePolicy.getMpProduct().get(product.getId()).getDescription();
                        if (StringUtils.isNotBlank(description)) {
                            for (ProgramLimit programLimit : salePolicy.getLtProgramLimit())
                                description = getNetPriceForProduct(agency, description, programLimit, product);
                            if (salePolicy.getLtStepLimit() != null)
                                description = getNetPriceForProduct(agency, description, salePolicy.getLtStepLimit(), product);
                            ltSalePolicyDescription.add(description);
                        }
                    }
                }
                // Lấy danh sách mô tả ctkm
                for (Program promotion : ltPromotion) {
                    if (promotion.isRunning() && promotion.getMpProduct().get(product.getId()) != null && checkProgramFilter(agency, promotion, source, deptInfo)) {
                        String description = promotion.getMpProduct().get(product.getId()).getDescription();
                        if (StringUtils.isNotBlank(description)) {
                            for (ProgramLimit programLimit : promotion.getLtProgramLimit())
                                description = getNetPriceForProduct(agency, description, programLimit, product);
                            if (promotion.getLtStepLimit() != null)
                                description = getNetPriceForProduct(agency, description, promotion.getLtStepLimit(), product);
                            ltPromotionDescription.add(description);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
    }

    /**
     * Lấy mô tả cho chính sách bán hàng theo sản phẩm, hiển thị trên cms
     */
    public String getCmsProgramDescriptionForProduct(Agency agency, int productId, Program program) {
        try {
            Product product = this.dataManager.getProgramManager().getProductById(productId, "");
            if ((program.getConditionType() == ProgramConditionType.PRODUCT_QUANTITY || program.getConditionType() == ProgramConditionType.PRODUCT_PRICE)
                    && program.getMpProduct().get(product.getId()) != null) {
                String description = program.getMpProductWithCombo().get(product.getId()).getDescription();
                if (StringUtils.isNotBlank(description)) {
                    for (ProgramLimit programLimit : program.getLtProgramLimit())
                        description = getNetPriceForProduct(agency, description, programLimit, product);
                    if (program.getLtStepLimit() != null)
                        description = getNetPriceForProduct(agency, description, program.getLtStepLimit(), product);
                }

                /**
                 * Nếu không có mô tả riêng thì trả về mô tả chung
                 */
                if (StringUtils.isBlank(description)) {
                    description = program.getDescription();
                }
                return description;
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return program.getDescription();
    }

    // Lấy giá net của sản phẩm
    public String getNetPriceForProduct(Agency agency, String description, ProgramLimit programLimit, Product product) {
        String data = description;
        try {

        } catch (Exception ex) {
            data = description;
            LogUtil.printDebug("", ex);
        }
        return data;
    }

    /**
     * Tạo bộ lọc
     */
    public boolean checkProgramFilter(Agency agency, Program program, Source source, DeptInfo deptInfo) {
        try {
            //Kiểm tra đại lý xem có bị chặn csbh
            if (program.getType() == ProgramType.CTTL && agency.isBlockCttl()) {
                return false;
            }
            if (program.getType() == ProgramType.SALE_POLICY && agency.isBlockCsbh()) {
                return false;
            }
            // kiểm tra đại lý xem có bị chặn ctsn
            if (program.getType() == ProgramType.PROMOTION && agency.isBlockCtsn() && program.isBirthdayFilter())
                return false;
            // kiểm tra đại lý xem có bị chặn ctkm ngoại trừ ctsn
            if (program.getType() == ProgramType.PROMOTION && agency.isBlockCtkm() && !program.isBirthdayFilter())
                return false;
            // kiểm tra đại lý xem có bị chặn ctss
            if (program.getType() == ProgramType.CTSS && agency.isBlockCtss())
                return false;
            if (program.getType() == ProgramType.DAMME && agency.isBlockCsdm())
                return false;
            // kiểm tra giới hạn số lượng đơn hàng theo tất cả
            if (program.getUserLimit() > 0) {
                int countOrderByProgram = this.dataManager.getProgramManager().countOrderByProgram(0, program.getId());
                if (countOrderByProgram >= program.getUserLimit())
                    return false;
            }
            // kiểm tra giới hạn số lượng đơn hàng theo đại lý
            if (program.getUseLimitPerAgency() > 0) {
                int countOrderByProgram = this.dataManager.getProgramManager().countOrderByProgram(agency.getId(), program.getId());
                if (countOrderByProgram >= program.getUseLimitPerAgency())
                    return false;
            }
            // Loại trừ đại lý
            if (program.getLtIgnoreAgencyId().contains(agency.getId()))
                return false;
            // Bao gồm đại lý
            if (program.getLtIncludeAgencyId().contains(agency.getId()))
                return true;
            if (program.getLtIncludeAgencyId().isEmpty() && program.getLtProgramFilter().isEmpty())
                return true;
            if (!program.getLtIncludeAgencyId().isEmpty() && program.getLtProgramFilter().isEmpty())
                return false;
            // Bộ lọc
            for (ProgramFilter programFilter : program.getLtProgramFilter()) {
                // Kiểm tra cấp bậc
                boolean isMatchedMembership = true;
                if (!programFilter.getLtAgencyMembershipId().isEmpty())
                    isMatchedMembership = programFilter.getLtAgencyMembershipId().contains(agency.getMembershipId());
                if (!isMatchedMembership)
                    continue;
                // Kiểm tra phòng kinh doanh
                boolean isMatchedAgencyBusinessDepartment = true;
                if (!programFilter.getLtAgencyBusinessDepartmentId().isEmpty())
                    isMatchedAgencyBusinessDepartment = programFilter.getLtAgencyBusinessDepartmentId().contains(agency.getBusinessDepartmentId());
                if (!isMatchedAgencyBusinessDepartment)
                    continue;
                // Kiểm tra tỉnh - tp
                boolean isMatchedAgencyCity = true;
                if (!programFilter.getLtAgencyCityId().isEmpty())
                    isMatchedAgencyCity = programFilter.getLtAgencyCityId().contains(agency.getCityId());
                if (!isMatchedAgencyCity)
                    continue;
                // Kiểm tra ngày sinh nhật
                boolean isMatchedBirthday = true;
                if (programFilter.isBirthday()) {
                    if (StringUtils.isBlank(agency.getBirthday()))
                        isMatchedBirthday = false;
                    else {
                        String pattern = "dd/MM";
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                        String now = simpleDateFormat.format(new Date());
                        isMatchedBirthday = now.equals(agency.getBirthday().substring(0, 5));
                    }
                }
                if (!isMatchedBirthday)
                    continue;
                // Kiểm tra giới tính
                boolean isMatchedGender = true;
                if (!programFilter.getLtGender().isEmpty())
                    isMatchedGender = programFilter.getLtGender().contains(agency.getGender());
                if (!isMatchedGender)
                    continue;
                // Kiểm tra khoảng cách đơn hàng cuối
                boolean isMatchedDiffOrderDay = true;
                if (programFilter.getDiffOrderDay() > 0) {
                    Order order = this.getLatestCompletedOrder(agency.getId(), "", "");
                    if (order == null)
                        isMatchedDiffOrderDay = false;
                    else {
                        long time = System.currentTimeMillis() - order.getConfirmDeliveryDate().getTime();
                        long day = TimeUnit.DAYS.convert(time, TimeUnit.MILLISECONDS);
                        isMatchedDiffOrderDay = (day <= programFilter.getDiffOrderDay());
                    }
                }
                if (!isMatchedDiffOrderDay)
                    continue;
                boolean isSource = true;
                if (!programFilter.getLtSource().isEmpty())
                    isSource = (programFilter.getLtSource().contains(source.getValue()));
                if (!isSource)
                    continue;
                // Kiểm tra số lần cam kết sai
                boolean isFailedCommit = true;
                if (programFilter.getSaiCamKet() > 0) {
                    int countFailedCommit = this.countFailedCommit(agency.getId());
                    isFailedCommit = !(countFailedCommit > programFilter.getSaiCamKet());
                }
                if (!isFailedCommit)
                    continue;
                // kiểm tra nợ xấu
                boolean isNX = true;
                if (programFilter.isNx())
                    isNX = !(deptInfo.getNx() > 0);
                if (!isNX)
                    continue;
                // kiểm tra nợ quá hạn trong khoảng
                boolean isNqhRange = true;
                if (programFilter.getFromNqh() > 0 && programFilter.getEndNqh() > 0)
                    isNqhRange = (deptInfo.getNqh() >= programFilter.getFromNqh() && deptInfo.getNqh() <= programFilter.getEndNqh());
                else if (programFilter.getFromNqh() > 0)
                    isNqhRange = (deptInfo.getNqh() >= programFilter.getFromNqh());
                else if (programFilter.getEndNqh() > 0)
                    isNqhRange = (deptInfo.getNqh() <= programFilter.getEndNqh());
                if (!isNqhRange)
                    continue;
                // kiểm tra có nợ quá hạn
                boolean isNqh = true;
                if (programFilter.isNqh())
                    isNqh = !(deptInfo.getNqh() > 0);
                if (!isNqh)
                    continue;
                // kiểm tra cho phép hạn mức gối đầu
                boolean isNgdLimit = true;
                if (programFilter.getNgdLimitStatus() == 0)
                    isNgdLimit = (deptInfo.getNgdLimit() <= 0);
                else if (programFilter.getNgdLimitStatus() == 1)
                    isNgdLimit = (deptInfo.getNgdLimit() > 0);
                if (!isNgdLimit)
                    continue;
                // kiểm tra kỳ hạn nợ trong khoảng
                boolean isDeptCycleRange = true;
                if (programFilter.getFromDeptCycle() > 0 && programFilter.getEndDeptCycle() > 0)
                    isDeptCycleRange = (deptInfo.getDeptCycle() >= programFilter.getFromDeptCycle() && deptInfo.getDeptCycle() <= programFilter.getEndDeptCycle());
                else if (programFilter.getFromDeptCycle() > 0)
                    isDeptCycleRange = (deptInfo.getDeptCycle() >= programFilter.getFromDeptCycle());
                else if (programFilter.getEndDeptCycle() > 0)
                    isDeptCycleRange = (deptInfo.getDeptCycle() <= programFilter.getEndDeptCycle());
                if (!isDeptCycleRange)
                    continue;
                // kiểm tra hạn mức nợ trong khoảng
                boolean isDeptLimitRange = true;
                if (programFilter.getFromDeptLimit() > 0 && programFilter.getEndDeptLimit() > 0)
                    isDeptLimitRange = (deptInfo.getDeptLimit() >= programFilter.getFromDeptLimit() && deptInfo.getDeptLimit() <= programFilter.getEndDeptLimit());
                else if (programFilter.getFromDeptLimit() > 0)
                    isDeptLimitRange = (deptInfo.getDeptLimit() >= programFilter.getFromDeptLimit());
                else if (programFilter.getEndDeptLimit() > 0)
                    isDeptLimitRange = (deptInfo.getDeptLimit() <= programFilter.getEndDeptLimit());
                if (!isDeptLimitRange)
                    continue;
                // kiểm tra doanh thu thuần trong khoảng
                boolean isDttRange = true;
                if (programFilter.getFromDtt() > 0 && programFilter.getEndDtt() > 0)
                    isDttRange = (deptInfo.getTotalDttCycle() >= programFilter.getFromDtt() && deptInfo.getTotalDttCycle() <= programFilter.getEndDtt());
                else if (programFilter.getFromDtt() > 0)
                    isDttRange = (deptInfo.getTotalDttCycle() >= programFilter.getFromDtt());
                else if (programFilter.getEndDtt() > 0)
                    isDttRange = (deptInfo.getTotalDttCycle() <= programFilter.getEndDtt());
                if (!isDttRange)
                    continue;
                // kiểm tra doanh số trong khoảng
                boolean isTotalPriceSaleRange = true;
                if (programFilter.getFromTotalPriceSales() > 0 && programFilter.getEndTotalPriceSales() > 0)
                    isTotalPriceSaleRange = (deptInfo.getTotalPriceSales() >= programFilter.getFromTotalPriceSales() && deptInfo.getTotalPriceSales() <= programFilter.getEndTotalPriceSales());
                else if (programFilter.getFromTotalPriceSales() > 0)
                    isTotalPriceSaleRange = (deptInfo.getTotalPriceSales() >= programFilter.getFromTotalPriceSales());
                else if (programFilter.getEndTotalPriceSales() > 0)
                    isTotalPriceSaleRange = (deptInfo.getTotalPriceSales() <= programFilter.getEndTotalPriceSales());
                if (!isTotalPriceSaleRange)
                    continue;
                // kiểm tra công nợ cuối kỳ trong khoảng
                boolean isDeptCycleEndRange = true;
                if (programFilter.getFromDeptCycleEnd() > 0 && programFilter.getEndDeptCycleEnd() > 0)
                    isDeptCycleEndRange = (deptInfo.getDeptCycleEnd() >= programFilter.getFromDeptCycleEnd() && deptInfo.getDeptCycleEnd() <= programFilter.getEndDeptCycleEnd());
                else if (programFilter.getFromDeptCycleEnd() > 0)
                    isDeptCycleEndRange = (deptInfo.getDeptCycleEnd() >= programFilter.getFromDeptCycleEnd());
                else if (programFilter.getEndDeptCycleEnd() > 0)
                    isDeptCycleEndRange = (deptInfo.getDeptCycleEnd() <= programFilter.getEndDeptCycleEnd());
                if (!isDeptCycleEndRange)
                    continue;
                // kiểm tra tiền thu trong khoảng
                boolean isTtRange = true;
                if (programFilter.getFromTt() > 0 && programFilter.getEndTt() > 0)
                    isTtRange = (deptInfo.getTotalTtCycle() >= programFilter.getFromTt() && deptInfo.getTotalTtCycle() <= programFilter.getEndTt());
                else if (programFilter.getFromTt() > 0)
                    isTtRange = (deptInfo.getTotalTtCycle() >= programFilter.getFromTt() && deptInfo.getTotalTtCycle() <= programFilter.getEndTt());
                else if (programFilter.getEndTt() > 0)
                    isTtRange = (deptInfo.getTotalTtCycle() <= programFilter.getEndTt());
                if (!isTtRange)
                    continue;
                return true;
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return false;
    }

    public boolean checkProgramVisibility(Agency agency, Program program) {
        try {
            //Kiểm tra đại lý xem có bị chặn csbh
            if (program.getType() == ProgramType.SALE_POLICY && agency.isBlockCsbh()) {
                return false;
            }
            // kiểm tra đại lý xem có bị chặn ctsn
            if (program.getType() == ProgramType.PROMOTION && agency.isBlockCtsn() && program.isBirthdayFilter())
                return false;
            // kiểm tra đại lý xem có bị chặn ctkm ngoại trừ ctsn
            if (program.getType() == ProgramType.PROMOTION && agency.isBlockCtkm() && !program.isBirthdayFilter())
                return false;
            if (program.getType() == ProgramType.CTSS && agency.isBlockCtss())
                return false;
            if (program.getType() == ProgramType.CTTL && agency.isBlockCttl())
                return false;
            if (program.getType() == ProgramType.DAMME && agency.isBlockCsdm())
                return false;
            // Loại trừ đại lý
            if (program.getLtIgnoreAgencyId().contains(agency.getId()))
                return false;
            // Bao gồm đại lý
            if (program.getLtIncludeAgencyId().contains(agency.getId()))
                return true;
            if (program.getLtIncludeAgencyId().isEmpty() && program.getLtProgramFilter().isEmpty())
                return true;
            if (!program.getLtIncludeAgencyId().isEmpty() && program.getLtProgramFilter().isEmpty())
                return false;
            // Bộ lọc
            for (ProgramFilter programFilter : program.getLtProgramFilter()) {
                // Kiểm tra cấp bậc
                boolean isMatchedMembership = true;
                if (!programFilter.getLtAgencyMembershipId().isEmpty())
                    isMatchedMembership = programFilter.getLtAgencyMembershipId().contains(agency.getMembershipId());
                if (!isMatchedMembership)
                    continue;
                // Kiểm tra phòng kinh doanh
                boolean isMatchedAgencyBusinessDepartment = true;
                if (!programFilter.getLtAgencyBusinessDepartmentId().isEmpty())
                    isMatchedAgencyBusinessDepartment = programFilter.getLtAgencyBusinessDepartmentId().contains(agency.getBusinessDepartmentId());
                if (!isMatchedAgencyBusinessDepartment)
                    continue;
                // Kiểm tra tỉnh - tp
                boolean isMatchedAgencyCity = true;
                if (!programFilter.getLtAgencyCityId().isEmpty())
                    isMatchedAgencyCity = programFilter.getLtAgencyCityId().contains(agency.getCityId());
                if (!isMatchedAgencyCity)
                    continue;
                return true;
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return false;
    }


    /**
     * Import chương trình
     */
    public Program importProgram(String data) {
        Program program;
        try {
            JsonObject object = (JsonObject) JsonParser.parseString(data);
            JsonObject info = object.get("promo_info").getAsJsonObject();
            ProgramType programType = ProgramType.valueOf(info.get("promo_type").getAsString());
            program = new Program();
            program.setId(info.get("id").getAsInt());
            program.setType(programType);
            program.setCode(info.get("code").getAsString());
            program.setName(info.get("name").getAsString());
            program.setDescription(info.get("description").getAsString());
            if (info.has("priority"))
                program.setPriority(info.get("priority").getAsInt());
            if (info.has("use_limit"))
                program.setUserLimit(info.get("use_limit").getAsInt());
            if (info.has("use_limit_per_agency"))
                program.setUseLimitPerAgency(info.get("use_limit_per_agency").getAsInt());
            if (info.has("promo_max_value"))
                program.setPromoMaxValue(info.get("promo_max_value").getAsLong());
            String image = info.get("image").getAsString();
            if (StringUtils.isNotBlank(image))
                image = ImagePath.PROMO.getImageUrl() + info.get("image").getAsString();
            program.setImage(image);
            program.setConditionType(ProgramConditionType.valueOf(info.get("condition_type").getAsString()));
            program.setStartDate(new Date(info.get("start_date_millisecond").getAsLong()));
            long endDateMillisecond = info.get("end_date_millisecond").getAsLong();
            if (endDateMillisecond > 0)
                program.setEndDate(new Date(endDateMillisecond));
            int isAutomaticAllocation = info.get("is_automatic_allocation").getAsInt();
            ProgramGoodsType programGoodsType = ProgramGoodsType.AUTO;
            if (isAutomaticAllocation == 0)
                programGoodsType = ProgramGoodsType.CONVERSION;
            program.setGoodsType(programGoodsType);
            String promoEndValueType = info.get("promo_end_value_type").getAsString();
            program.setEndLimit(promoEndValueType.trim().equals("IS_NOT_NULL"));
            Map<Integer, List<ProgramProduct>> mpProgramProduct = new LinkedHashMap<>();
            JsonArray arrPromoItemGroups = object.get("promo_item_groups").getAsJsonArray();
            for (JsonElement elPromoItemGroup : arrPromoItemGroups) {
                JsonObject obPromoItemGroup = elPromoItemGroup.getAsJsonObject();
                int dataIndex = obPromoItemGroup.get("data_index").getAsInt();
                List<ProgramProduct> ltProgramProduct = new ArrayList<>();
                JsonArray arrProduct = obPromoItemGroup.get("products").getAsJsonArray();
                for (JsonElement elProduct : arrProduct) {
                    JsonObject obProduct = elProduct.getAsJsonObject();
                    Product product = this.dataManager.getProgramManager().getProductById(obProduct.get("item_id").getAsInt(), "");
                    String description = obProduct.get("note").getAsString();
                    ProgramProduct programProduct = new ProgramProduct();
                    programProduct.setProduct(product);
                    programProduct.setDescription(description);
                    ltProgramProduct.add(programProduct);
                    if (product.getProductGroup() != null && product.getProductGroup().getCategory() != null && product.getProductGroup().getCategory().getParent() != null)
                        program.getMpProductCategory().put(product.getProductGroup().getCategory().getParent().getId(), 1);
                    program.getMpProduct().put(programProduct.getProduct().getId(), programProduct);
                }
                mpProgramProduct.put(dataIndex, ltProgramProduct);
            }
            if (object.has("promo_item_ignores")) {
                JsonArray arrPromoItemIgnores = object.get("promo_item_ignores").getAsJsonArray();
                for (JsonElement elPromoItemIgnore : arrPromoItemIgnores) {
                    JsonObject obPromoItemIgnore = elPromoItemIgnore.getAsJsonObject();
                    int productId = obPromoItemIgnore.get("item_id").getAsInt();
                    program.getLtIgnoreProductId().add(productId);
                }
            }
            JsonArray arrProgramLimit = object.get("promo_limits").getAsJsonArray();
            for (JsonElement elProgramLimit : arrProgramLimit) {
                JsonObject obProgramLimit = elProgramLimit.getAsJsonObject();
                ProgramLimit programLimit = new ProgramLimit();
                programLimit.setId(obProgramLimit.get("id").getAsInt());
                programLimit.setProgramConditionType(ProgramConditionType.valueOf(obProgramLimit.get("condition_type").getAsString()));
                programLimit.setLevel(obProgramLimit.get("level").getAsInt());
                JsonArray arrProgramProductGroup = obProgramLimit.get("promo_limit_groups").getAsJsonArray();
                for (JsonElement elProgramProductGroup : arrProgramProductGroup) {
                    JsonObject obProgramProductGroup = elProgramProductGroup.getAsJsonObject();
                    ProgramProductGroup programProductGroup = new ProgramProductGroup();
                    programProductGroup.setId(obProgramProductGroup.get("id").getAsInt());
                    programProductGroup.setFromValue(obProgramProductGroup.get("from_value").getAsLong());
                    programProductGroup.setEndValue(obProgramProductGroup.get("end_value").getAsLong());
                    int dataIndex = obProgramProductGroup.get("data_index").getAsInt();
                    programProductGroup.setDataIndex(dataIndex);
                    List<ProgramProduct> ltProgramProduct = new ArrayList<>();
                    if (program.getConditionType() != ProgramConditionType.ORDER_PRICE)
                        ltProgramProduct = mpProgramProduct.get(dataIndex);
                    programProductGroup.setLtProgramProduct(ltProgramProduct);
                    JsonObject obOffer = obProgramProductGroup.get("offer").getAsJsonObject();
                    ProgramOfferType programOfferType = ProgramOfferType.valueOf(obOffer.get("offer_type").getAsString());
                    switch (programOfferType) {
                        case GOODS_OFFER:
                            GoodsOffer goodsOffer = new GoodsOffer();
                            goodsOffer.setOfferType(programOfferType);
                            goodsOffer.setGoodsType(program.getGoodsType());
                            goodsOffer.setConversionRatio(obOffer.get("conversion_ratio").getAsDouble());
                            goodsOffer.setQuantity(obOffer.get("offer_value").getAsInt());
                            JsonArray arrGoodsOfferBonus = obOffer.get("offer_bonus").getAsJsonArray();
                            for (JsonElement elOfferBonus : arrGoodsOfferBonus) {
                                JsonObject obOfferBonus = elOfferBonus.getAsJsonObject();
                                int productId = obOfferBonus.get("product_id").getAsInt();
                                Product product = this.dataManager.getProgramManager().getProductById(productId, "");
                                int quantity = obOfferBonus.get("offer_value").getAsInt();
                                OfferProduct offerProduct = new OfferProduct();
                                offerProduct.setProduct(product);
                                offerProduct.setQuantity(quantity);
                                ProgramOfferType bonusOfferType = ProgramOfferType.valueOf(obOfferBonus.get("offer_type").getAsString());
                                if (bonusOfferType == ProgramOfferType.GOODS_OFFER)
                                    goodsOffer.getLtBonusGoods().add(offerProduct);
                                else if (bonusOfferType == ProgramOfferType.GIFT_OFFER)
                                    goodsOffer.getLtBonusGift().add(offerProduct);
                            }
                            goodsOffer.setProgramProductGroup(programProductGroup);
                            programProductGroup.setOffer(goodsOffer);
                            break;
                        case GIFT_OFFER:
                            GiftOffer giftOffer = new GiftOffer();
                            giftOffer.setOfferType(programOfferType);
                            giftOffer.setGoodsType(program.getGoodsType());
                            JsonArray arrGiftOfferBonus = obOffer.get("offer_bonus").getAsJsonArray();
                            for (JsonElement elOfferBonus : arrGiftOfferBonus) {
                                JsonObject obOfferBonus = elOfferBonus.getAsJsonObject();
                                int productId = obOfferBonus.get("product_id").getAsInt();
                                Product product = this.dataManager.getProgramManager().getProductById(productId, "");
                                int quantity = obOfferBonus.get("offer_value").getAsInt();
                                OfferProduct offerProduct = new OfferProduct();
                                offerProduct.setProduct(product);
                                offerProduct.setQuantity(quantity);
                                giftOffer.getLtBonusGift().add(offerProduct);
                            }
                            giftOffer.setProgramProductGroup(programProductGroup);
                            programProductGroup.setOffer(giftOffer);
                            break;
                        case MONEY_DISCOUNT:
                            MoneyDiscountOffer moneyDiscountOffer = new MoneyDiscountOffer();
                            moneyDiscountOffer.setOfferType(programOfferType);
                            moneyDiscountOffer.setGoodsType(program.getGoodsType());
                            moneyDiscountOffer.setMoney(obOffer.get("offer_value").getAsInt());
                            JsonArray arrMoneyDiscountOfferProduct = obOffer.get("offer_products").getAsJsonArray();
                            for (JsonElement elOfferProduct : arrMoneyDiscountOfferProduct) {
                                JsonObject obOfferProduct = elOfferProduct.getAsJsonObject();
                                int productId = obOfferProduct.get("product_id").getAsInt();
                                double money = obOfferProduct.get("offer_value").getAsDouble();
                                moneyDiscountOffer.getMpProductMoney().put(productId, money);
                            }
                            moneyDiscountOffer.setProgramProductGroup(programProductGroup);
                            programProductGroup.setOffer(moneyDiscountOffer);
                            break;
                        case PERCENT_DISCOUNT:
                            PercentDiscountOffer percentDiscountOffer = new PercentDiscountOffer();
                            percentDiscountOffer.setOfferType(programOfferType);
                            percentDiscountOffer.setGoodsType(program.getGoodsType());
                            percentDiscountOffer.setPercent(obOffer.get("offer_value").getAsInt());
                            JsonArray arrPercentDiscountOfferProduct = obOffer.get("offer_products").getAsJsonArray();
                            for (JsonElement elOfferProduct : arrPercentDiscountOfferProduct) {
                                JsonObject obOfferProduct = elOfferProduct.getAsJsonObject();
                                int productId = obOfferProduct.get("product_id").getAsInt();
                                int percent = obOfferProduct.get("offer_value").getAsInt();
                                percentDiscountOffer.getMpProductPercent().put(productId, percent);
                            }
                            percentDiscountOffer.setProgramProductGroup(programProductGroup);
                            programProductGroup.setOffer(percentDiscountOffer);
                            break;
                    }
                    programProductGroup.setProgramLimit(programLimit);
                    programLimit.getLtProgramProductGroup().add(programProductGroup);
                }
                programLimit.setProgram(program);
                if (programLimit.getProgramConditionType() == ProgramConditionType.STEP)
                    program.setLtStepLimit(programLimit);
                else
                    program.getLtProgramLimit().add(programLimit);
            }
            sortUtil.sortProgramLimit(program.getLtProgramLimit());
            if (object.has("promo_apply_object")) {
                JsonObject obPromoFilterData = object.get("promo_apply_object").getAsJsonObject();
                if (obPromoFilterData.has("promo_agency_ignores")) {
                    JsonArray arrPromoAgencyIgnores = obPromoFilterData.get("promo_agency_ignores").getAsJsonArray();
                    for (JsonElement elPromoAgencyIgnore : arrPromoAgencyIgnores) {
                        JsonObject obPromoAgencyIgnore = elPromoAgencyIgnore.getAsJsonObject();
                        int agencyId = obPromoAgencyIgnore.get("id").getAsInt();
                        program.getLtIgnoreAgencyId().add(agencyId);
                    }
                }
                if (obPromoFilterData.has("promo_agency_includes")) {
                    JsonArray arrPromoAgencyIncludes = obPromoFilterData.get("promo_agency_includes").getAsJsonArray();
                    for (JsonElement elPromoAgencyInclude : arrPromoAgencyIncludes) {
                        JsonObject obPromoAgencyInclude = elPromoAgencyInclude.getAsJsonObject();
                        int agencyId = obPromoAgencyInclude.get("id").getAsInt();
                        program.getLtIncludeAgencyId().add(agencyId);
                    }
                }
                if (obPromoFilterData.has("promo_filters")) {
                    JsonArray arrPromoFilter = obPromoFilterData.get("promo_filters").getAsJsonArray();
                    for (JsonElement elPromoFilter : arrPromoFilter) {
                        JsonObject obPromoFilter = elPromoFilter.getAsJsonObject();
                        if (obPromoFilter.has("filter_types")) {
                            JsonArray arrFilterType = obPromoFilter.get("filter_types").getAsJsonArray();
                            ProgramFilter programFilter = new ProgramFilter();
                            for (JsonElement elFilterType : arrFilterType) {
                                JsonObject obFilterType = elFilterType.getAsJsonObject();
                                ProgramFilterType programFilterType = ProgramFilterType.valueOf(obFilterType.get("filter_type").getAsString());
                                switch (programFilterType) {
                                    case CAP_BAC:
                                        String filterData = obFilterType.get("filter_data").getAsString();
                                        JsonArray arrAgencyMembership = JsonParser.parseString(filterData).getAsJsonArray();
                                        for (JsonElement elAgencyMembership : arrAgencyMembership) {
                                            int membershipId = elAgencyMembership.getAsInt();
                                            programFilter.getLtAgencyMembershipId().add(membershipId);
                                        }
                                        break;
                                    case PKD:
                                        filterData = obFilterType.get("filter_data").getAsString();
                                        JsonArray arrAgencyDepartment = JsonParser.parseString(filterData).getAsJsonArray();
                                        for (JsonElement elAgencyDepartment : arrAgencyDepartment) {
                                            int departmentId = elAgencyDepartment.getAsInt();
                                            programFilter.getLtAgencyBusinessDepartmentId().add(departmentId);
                                        }
                                        break;
                                    case TINH_THANH:
                                        filterData = obFilterType.get("filter_data").getAsString();
                                        JsonArray arrAgencyCity = JsonParser.parseString(filterData).getAsJsonArray();
                                        for (JsonElement elAgencyCity : arrAgencyCity) {
                                            int cityId = elAgencyCity.getAsInt();
                                            programFilter.getLtAgencyCityId().add(cityId);
                                        }
                                        break;
                                    case NGAY_SINH_NHAT:
                                        filterData = obFilterType.get("filter_data").getAsString();
                                        programFilter.setBirthday(JsonParser.parseString(filterData).getAsBoolean());
                                        break;
                                    case GIOI_TINH:
                                        filterData = obFilterType.get("filter_data").getAsString();
                                        JsonArray arrGender = JsonParser.parseString(filterData).getAsJsonArray();
                                        for (JsonElement elGender : arrGender) {
                                            int gender = elGender.getAsInt();
                                            programFilter.getLtGender().add(gender);
                                        }
                                        break;
                                    case KHOANG_CACH_DON_HANG_CUOI:
                                        filterData = obFilterType.get("filter_data").getAsString();
                                        programFilter.setDiffOrderDay(JsonParser.parseString(filterData).getAsInt());
                                        break;
                                    case NGUON_DON_HANG:
                                        filterData = obFilterType.get("filter_data").getAsString();
                                        JsonArray arrSource = JsonParser.parseString(filterData).getAsJsonArray();
                                        for (JsonElement elSource : arrSource) {
                                            int source = elSource.getAsInt();
                                            programFilter.getLtSource().add(source);
                                        }
                                        break;
                                    case CO_NO_XAU:
                                        filterData = obFilterType.get("filter_data").getAsString();
                                        programFilter.setNx(JsonParser.parseString(filterData).getAsBoolean());
                                        break;
                                    case GIA_TRI_NO_QUA_HAN:
                                        filterData = obFilterType.get("filter_data").getAsString();
                                        JsonObject obFilterData = JsonParser.parseString(filterData).getAsJsonObject();
                                        if (obFilterData.has("from_value"))
                                            programFilter.setFromNqh(obFilterData.get("from_value").getAsLong());
                                        if (obFilterData.has("end_value"))
                                            programFilter.setEndNqh(obFilterData.get("end_value").getAsLong());
                                        break;
                                    case CO_NO_QUA_HAN:
                                        filterData = obFilterType.get("filter_data").getAsString();
                                        programFilter.setNqh(JsonParser.parseString(filterData).getAsBoolean());
                                        break;
                                    case SO_LAN_SAI_CAM_KET:
                                        filterData = obFilterType.get("filter_data").getAsString();
                                        programFilter.setSaiCamKet(JsonParser.parseString(filterData).getAsInt());
                                        break;
                                    case CO_HAN_MUC_GOI_DAU:
                                        filterData = obFilterType.get("filter_data").getAsString();
                                        boolean status = JsonParser.parseString(filterData).getAsBoolean();
                                        if (status)
                                            programFilter.setNgdLimitStatus(1);
                                        else
                                            programFilter.setNgdLimitStatus(0);
                                        break;
                                    case KY_HAN_NO:
                                        filterData = obFilterType.get("filter_data").getAsString();
                                        obFilterData = JsonParser.parseString(filterData).getAsJsonObject();
                                        if (obFilterData.has("from_value"))
                                            programFilter.setFromDeptCycle(obFilterData.get("from_value").getAsInt());
                                        if (obFilterData.has("end_value"))
                                            programFilter.setEndDeptCycle(obFilterData.get("end_value").getAsInt());
                                        break;
                                    case HAN_MUC_NO:
                                        filterData = obFilterType.get("filter_data").getAsString();
                                        obFilterData = JsonParser.parseString(filterData).getAsJsonObject();
                                        if (obFilterData.has("from_value"))
                                            programFilter.setFromDeptLimit(obFilterData.get("from_value").getAsLong());
                                        if (obFilterData.has("end_value"))
                                            programFilter.setEndDeptLimit(obFilterData.get("end_value").getAsLong());
                                        break;
                                    case DOANH_THU_THUAN_TU_DEN:
                                        filterData = obFilterType.get("filter_data").getAsString();
                                        obFilterData = JsonParser.parseString(filterData).getAsJsonObject();
                                        if (obFilterData.has("from_value"))
                                            programFilter.setFromDtt(obFilterData.get("from_value").getAsLong());
                                        if (obFilterData.has("end_value"))
                                            programFilter.setEndDtt(obFilterData.get("end_value").getAsLong());
                                        break;
                                    case DOANH_SO_TU_DEN:
                                        filterData = obFilterType.get("filter_data").getAsString();
                                        obFilterData = JsonParser.parseString(filterData).getAsJsonObject();
                                        if (obFilterData.has("from_value"))
                                            programFilter.setFromTotalPriceSales(obFilterData.get("from_value").getAsLong());
                                        if (obFilterData.has("end_value"))
                                            programFilter.setEndTotalPriceSales(obFilterData.get("end_value").getAsLong());
                                        break;
                                    case CONG_NO_CUOI_KY_TU_DEN:
                                        filterData = obFilterType.get("filter_data").getAsString();
                                        obFilterData = JsonParser.parseString(filterData).getAsJsonObject();
                                        if (obFilterData.has("from_value"))
                                            programFilter.setFromDeptCycleEnd(obFilterData.get("from_value").getAsLong());
                                        if (obFilterData.has("end_value"))
                                            programFilter.setEndDeptCycleEnd(obFilterData.get("end_value").getAsLong());
                                        break;
                                    case TIEN_THU:
                                        filterData = obFilterType.get("filter_data").getAsString();
                                        obFilterData = JsonParser.parseString(filterData).getAsJsonObject();
                                        if (obFilterData.has("from_value"))
                                            programFilter.setFromTt(obFilterData.get("from_value").getAsLong());
                                        if (obFilterData.has("end_value"))
                                            programFilter.setEndTt(obFilterData.get("end_value").getAsLong());
                                        break;
                                }
                            }
                            program.getLtProgramFilter().add(programFilter);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            program = null;
            LogUtil.printDebug("", ex);
        }
        return program;
    }

    protected int getPromoVisibilityByAgency(int agency_id, int promo_id) {
        if (this.checkProgramVisibility(
                this.dataManager.getProgramManager().getAgency(agency_id),
                this.getProgramById(promo_id)
        )) {
            return VisibilityType.SHOW.getId();
        }
        return VisibilityType.HIDE.getId();
    }

    /**
     * Tính chương trình săn sale cho sản phẩm
     */
    public void checkSanSaleForProduct(Agency agency,
                                       Map<Integer, ProgramOrderProduct> mpProgramOrderProduct, // danh sách sản phẩm dùng đểm kiểm tra
                                       Source source,
                                       Map<Integer, ProgramSanSaleOffer> mpResponseProgramSanSaleOffer // danh sách kết quả ưu đãi theo từng chương trình
    ) {
        if (mpProgramOrderProduct.isEmpty())
            return;
        try {
            List<Program> ltSanSale = new ArrayList<>(this.dataManager.getProgramManager().getMpHuntSale().values());
            // Sắp xếp săn sale theo thứ tự ưu tiên
            sortUtil.sortProgram(ltSanSale);
            // Lấy thông tin công nợ hiện tại của đại lý
            DeptInfo deptInfo = this.dataManager.getProgramManager().getDeptInfo(agency.getId());
            for (Program program : ltSanSale) {
                if (!mpProgramOrderProduct.isEmpty()) {
                    Map<Integer, ProgramOrderProduct> mpTmp = new LinkedHashMap<>();
                    if (program.isSanSaleRunning() && checkProgramFilter(agency, program, source, deptInfo)) {
                        for (ProgramOrderProduct programOrderProduct : mpProgramOrderProduct.values()) {
                            int limitQuantity = (int) getLimitProductQuantity(agency, programOrderProduct.getProduct().getId(), program);
                            if (limitQuantity == 0) {
                                mpTmp.put(programOrderProduct.getProduct().getId(), programOrderProduct);
                                mpProgramOrderProduct.remove(programOrderProduct.getProduct().getId());
                            } else {
                                double sanSalePrice = getSanSalePrice(program.getApplyPrivatePrice(), programOrderProduct.getProductPrice(), programOrderProduct.getCommonPrice());
                                if (sanSalePrice == 0) {
                                    mpTmp.put(programOrderProduct.getProduct().getId(), programOrderProduct);
                                    mpProgramOrderProduct.remove(programOrderProduct.getProduct().getId());
                                } else if (limitQuantity > 0 && limitQuantity < programOrderProduct.getProductQuantity()) {
                                    ProgramOrderProduct tmp = new ProgramOrderProduct();
                                    tmp.setProduct(programOrderProduct.getProduct());
                                    tmp.setProductQuantity(programOrderProduct.getProductQuantity() - limitQuantity);
                                    tmp.setProductPrice(programOrderProduct.getProductPrice());
                                    tmp.setBeginPrice(tmp.getProductQuantity() * tmp.getProductPrice());
                                    tmp.setCommonPrice(programOrderProduct.getCommonPrice());
                                    mpTmp.put(programOrderProduct.getProduct().getId(), tmp);
                                    programOrderProduct.setProductQuantity(limitQuantity);
                                    programOrderProduct.setBeginPrice(programOrderProduct.getProductQuantity() * programOrderProduct.getProductPrice());
                                }
                            }
                        }
                        ProgramSanSaleOffer programSanSaleOffer = new ProgramSanSaleOffer();
                        List<ProgramOffer> ltProgramOffer = handleSanSaleForProduct(agency, program, mpProgramOrderProduct, programSanSaleOffer);
                        if (!ltProgramOffer.isEmpty()) {
                            handleSanSaleOfferForProduct(ltProgramOffer, programSanSaleOffer);
                            mpResponseProgramSanSaleOffer.put(program.getId(), programSanSaleOffer);
                        }
                        for (Integer productId : mpTmp.keySet()) {
                            if (mpProgramOrderProduct.containsKey(productId)) {
                                ProgramOrderProduct programOrderProduct = mpProgramOrderProduct.get(productId);
                                programOrderProduct.setProductQuantity(programOrderProduct.getProductQuantity() + mpTmp.get(productId).getProductQuantity());
                                programOrderProduct.setBeginPrice(programOrderProduct.getProductQuantity() * programOrderProduct.getProductPrice());
                            } else
                                mpProgramOrderProduct.put(productId, mpTmp.get(productId));
                        }
                    }
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
    }

    private void checkUpdateSanSaleForProduct(Agency agency,
                                              long updateQuantity,
                                              long remainQuantity,
                                              boolean isExit,
                                              ProgramProductGroup programProductGroup,
                                              Map<Integer, ProgramOrderProduct> mpProgramOrderProduct,
                                              ProgramSanSaleOffer programSanSaleOffer) {
        if (programProductGroup.isCombo()) {
            long quantity = updateQuantity;
            if (isExit)
                quantity = (updateQuantity + remainQuantity);
            for (int i = 1; i <= quantity; i++) {
                int comboQuantity = 0;
                Map<Integer, Integer> mpRatio = new LinkedHashMap<>();
                for (ProgramProduct programProduct : programProductGroup.getLtProgramProduct()) {
                    if (!mpProgramOrderProduct.containsKey(programProduct.getProduct().getId()))
                        break;
                    int productQuantity = mpProgramOrderProduct.get(programProduct.getProduct().getId()).getProductQuantity();
                    if (productQuantity < programProduct.getQuantity())
                        break;
                    int ratio = productQuantity / programProduct.getQuantity();
                    mpRatio.put(programProduct.getProduct().getId(), ratio);
                }
                if (mpRatio.size() == programProductGroup.getLtProgramProduct().size()) {
                    Optional<Integer> minValue = mpRatio.values().stream().min(Integer::compareTo);
                    comboQuantity = minValue.orElse(0);
                }
                if (comboQuantity > 0) {
                    for (ProgramProduct programProduct : programProductGroup.getLtProgramProduct()) {
                        ProgramOrderProduct programOrderProduct = mpProgramOrderProduct.get(programProduct.getProduct().getId());
                        int productQuantity = programOrderProduct.getProductQuantity() - programProduct.getQuantity();
                        if (productQuantity <= 0)
                            mpProgramOrderProduct.remove(programProduct.getProduct().getId());
                        else {
                            programOrderProduct.setProductQuantity(productQuantity);
                            programOrderProduct.setBeginPrice(programOrderProduct.getProductQuantity() * programOrderProduct.getProductPrice());
                        }
                    }
                    int value = programSanSaleOffer.getMpComboQuantity().getOrDefault(programProductGroup.getComboId(), 0);
                    programSanSaleOffer.getMpComboQuantity().put(programProductGroup.getComboId(), value + 1);
                }
            }
        } else {
            if (isExit) {
                for (ProgramProduct programProduct : programProductGroup.getLtProgramProduct()) {
                    ProgramOrderProduct programOrderProduct = mpProgramOrderProduct.get(programProduct.getProduct().getId());
                    if (programOrderProduct != null) {
                        programSanSaleOffer.getMpProductQuantity().put(programProduct.getProduct().getId(), programOrderProduct.getProductQuantity());
                        mpProgramOrderProduct.remove(programOrderProduct.getProduct().getId());
                    }
                }
            } else {
                List<ProgramProduct> ltProgramProduct = new ArrayList<>(programProductGroup.getLtProgramProduct());
                for (ProgramProduct programProduct : ltProgramProduct) {
                    ProductDataSetting productDataSetting = this.getProductSetting(agency, programProduct.getProduct());
                    double sanSalePrice = getSanSalePrice(programProductGroup.getProgramLimit().getProgram().getApplyPrivatePrice(),
                            productDataSetting.getProductPrice(),
                            programProduct.getProduct().getPrice());
                    programProduct.setProductPrice(sanSalePrice);
                }
                int quantity = (int) updateQuantity;
                ltProgramProduct.sort(sortUtil.getProgramProductComparator());
                for (ProgramProduct programProduct : ltProgramProduct) {
                    ProgramOrderProduct programOrderProduct = mpProgramOrderProduct.get(programProduct.getProduct().getId());
                    if (programOrderProduct != null) {
                        if (programOrderProduct.getProductQuantity() < quantity) {
                            int tmp = programSanSaleOffer.getMpProductQuantity().getOrDefault(programProduct.getProduct().getId(), 0);
                            programSanSaleOffer.getMpProductQuantity().put(programProduct.getProduct().getId(), tmp + programOrderProduct.getProductQuantity());
                            quantity = quantity - programOrderProduct.getProductQuantity();
                            mpProgramOrderProduct.remove(programOrderProduct.getProduct().getId());
                        } else {
                            programOrderProduct.setProductQuantity(programOrderProduct.getProductQuantity() - quantity);
                            programOrderProduct.setBeginPrice(programOrderProduct.getProductQuantity() * programOrderProduct.getProductPrice());
                            if (programOrderProduct.getProductQuantity() == 0)
                                mpProgramOrderProduct.remove(programOrderProduct.getProduct().getId());
                            int tmp = programSanSaleOffer.getMpProductQuantity().getOrDefault(programProduct.getProduct().getId(), 0);
                            programSanSaleOffer.getMpProductQuantity().put(programProduct.getProduct().getId(), tmp + quantity);
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Xử lý phần dữ liệu cho sản phẩm trong săn sale
     */
    private List<ProgramOffer> handleSanSaleForProduct(
            Agency agency,
            Program program,
            Map<Integer, ProgramOrderProduct> mpProgramOrderProduct,
            ProgramSanSaleOffer programSanSaleOffer) {
        List<ProgramOffer> ltProgramOffer = new ArrayList<>();
        try {
            if (program.getConditionType() == ProgramConditionType.PRODUCT_QUANTITY) {
                Map<Integer, Long> mpProductGroupQuantity = new LinkedHashMap<>();
                // Kiểm tra hạn mức cao nhất
                for (ProgramLimit programLimit : program.getLtProgramLimit()) {
                    boolean isExit = false;
                    for (ProgramProductGroup programProductGroup : programLimit.getLtProgramProductGroup()) {
                        long totalProductOrComboQuantity = 0; // tổng số lượng sản phẩm trong nhóm hoặc số lượng combo
                        if (programProductGroup.isCombo()) { // tính số lượng combo thỏa điều kiện
                            Map<Integer, Integer> mpRatio = new LinkedHashMap<>();
                            for (ProgramProduct programProduct : programProductGroup.getLtProgramProduct()) {
                                if (!mpProgramOrderProduct.containsKey(programProduct.getProduct().getId()))
                                    break;
                                int quantity = mpProgramOrderProduct.get(programProduct.getProduct().getId()).getProductQuantity();
                                if (quantity < programProduct.getQuantity())
                                    break;
                                int ratio = quantity / programProduct.getQuantity();
                                mpRatio.put(programProduct.getProduct().getId(), ratio);
                            }
                            if (mpRatio.size() == programProductGroup.getLtProgramProduct().size()) {
                                Optional<Integer> minValue = mpRatio.values().stream().min(Integer::compareTo);
                                totalProductOrComboQuantity = minValue.orElse(0);
                            }
                        } else { // Tính tổng số lượng sản phẩm thỏa điều kiện
                            totalProductOrComboQuantity = programProductGroup.getLtProgramProduct().stream()
                                    .filter(object -> mpProgramOrderProduct.containsKey(object.getProduct().getId()))
                                    .map(object -> mpProgramOrderProduct.get(object.getProduct().getId()).getProductQuantity())
                                    .reduce(0, Integer::sum);
                        }
                        if (totalProductOrComboQuantity >= programProductGroup.getFromValue() && !program.isEndLimit()) {
                            if (programProductGroup.getOffer().getOfferType() == ProgramOfferType.MONEY_DISCOUNT ||
                                    programProductGroup.getOffer().getOfferType() == ProgramOfferType.PERCENT_DISCOUNT ||
                                    programProductGroup.getOffer().getOfferType() == ProgramOfferType.FIXED_PRICE)
                                isExit = true;
                            ltProgramOffer.add(programProductGroup.getOffer());
                            totalProductOrComboQuantity -= programProductGroup.getFromValue();
                            checkUpdateSanSaleForProduct(agency, programProductGroup.getFromValue(), totalProductOrComboQuantity, isExit,
                                    programProductGroup, mpProgramOrderProduct, programSanSaleOffer);
                        } else if (totalProductOrComboQuantity >= programProductGroup.getFromValue() && programProductGroup.getEndValue() > 0) {
                            if (programProductGroup.getOffer().getOfferType() == ProgramOfferType.MONEY_DISCOUNT ||
                                    programProductGroup.getOffer().getOfferType() == ProgramOfferType.PERCENT_DISCOUNT ||
                                    programProductGroup.getOffer().getOfferType() == ProgramOfferType.FIXED_PRICE)
                                isExit = true;
                            ltProgramOffer.add(programProductGroup.getOffer());
                            totalProductOrComboQuantity -= programProductGroup.getUnderEndValue();
                            if (totalProductOrComboQuantity < 0)
                                totalProductOrComboQuantity = 0;
                            checkUpdateSanSaleForProduct(agency, programProductGroup.getUnderEndValue(), totalProductOrComboQuantity, isExit,
                                    programProductGroup, mpProgramOrderProduct, programSanSaleOffer);
                        } else if (totalProductOrComboQuantity >= programProductGroup.getFromValue()) {
                            isExit = true;
                            ltProgramOffer.add(programProductGroup.getOffer());
                            checkUpdateSanSaleForProduct(agency, 0, totalProductOrComboQuantity, isExit,
                                    programProductGroup, mpProgramOrderProduct, programSanSaleOffer);
                            totalProductOrComboQuantity = 0;
                        }
                        mpProductGroupQuantity.put(programProductGroup.getDataIndex(), totalProductOrComboQuantity);
                    }
                    if (isExit)
                        return ltProgramOffer;
                    break;
                }
                // Kiểm tra bước nhảy
                if (!ltProgramOffer.isEmpty() && program.getLtStepLimit() != null) {
                    for (ProgramProductGroup programProductGroup : program.getLtStepLimit().getLtProgramProductGroup()) {
                        long totalProductOrComboQuantity = 0;
                        if (mpProductGroupQuantity.containsKey(programProductGroup.getDataIndex()))
                            totalProductOrComboQuantity = mpProductGroupQuantity.get(programProductGroup.getDataIndex());
                        while (totalProductOrComboQuantity >= programProductGroup.getFromValue()) {
                            ltProgramOffer.add(programProductGroup.getOffer());
                            totalProductOrComboQuantity -= programProductGroup.getFromValue();
                            checkUpdateSanSaleForProduct(agency, programProductGroup.getFromValue(), totalProductOrComboQuantity, false,
                                    programProductGroup, mpProgramOrderProduct, programSanSaleOffer);
                        }
                        mpProductGroupQuantity.put(programProductGroup.getDataIndex(), totalProductOrComboQuantity);
                    }
                }
                // Kiểm tra tất cả hạn mức
                for (ProgramLimit programLimit : program.getLtProgramLimit()) {
                    boolean isExit = false;
                    for (ProgramProductGroup programProductGroup : programLimit.getLtProgramProductGroup()) {
                        long totalProductOrComboQuantity = 0;
                        if (mpProductGroupQuantity.containsKey(programProductGroup.getDataIndex()))
                            totalProductOrComboQuantity = mpProductGroupQuantity.get(programProductGroup.getDataIndex());
                        if (totalProductOrComboQuantity >= programProductGroup.getFromValue() && !program.isEndLimit()) {
                            long offerNumber = totalProductOrComboQuantity / programProductGroup.getFromValue();
                            if (programProductGroup.getOffer().getOfferType() == ProgramOfferType.MONEY_DISCOUNT ||
                                    programProductGroup.getOffer().getOfferType() == ProgramOfferType.PERCENT_DISCOUNT ||
                                    programProductGroup.getOffer().getOfferType() == ProgramOfferType.FIXED_PRICE) {
                                offerNumber = 1;
                                isExit = true;
                            }
                            for (int i = 1; i <= offerNumber; i++) {
                                ltProgramOffer.add(programProductGroup.getOffer());
                                totalProductOrComboQuantity -= programProductGroup.getFromValue();
                                checkUpdateSanSaleForProduct(agency, programProductGroup.getFromValue(), totalProductOrComboQuantity, isExit,
                                        programProductGroup, mpProgramOrderProduct, programSanSaleOffer);
                            }
                        } else if (totalProductOrComboQuantity >= programProductGroup.getFromValue() && programProductGroup.getEndValue() > 0) {
                            long offerNumber = 1;
                            long endQuantity = programProductGroup.getUnderEndValue();
                            if (totalProductOrComboQuantity > endQuantity)
                                offerNumber = totalProductOrComboQuantity / endQuantity;
                            if (programProductGroup.getOffer().getOfferType() == ProgramOfferType.MONEY_DISCOUNT ||
                                    programProductGroup.getOffer().getOfferType() == ProgramOfferType.PERCENT_DISCOUNT ||
                                    programProductGroup.getOffer().getOfferType() == ProgramOfferType.FIXED_PRICE) {
                                offerNumber = 1;
                                isExit = true;
                            }
                            for (int i = 1; i <= offerNumber; i++) {
                                ltProgramOffer.add(programProductGroup.getOffer());
                                totalProductOrComboQuantity -= endQuantity;
                                if (totalProductOrComboQuantity < 0)
                                    totalProductOrComboQuantity = 0;
                                checkUpdateSanSaleForProduct(agency, endQuantity, totalProductOrComboQuantity, isExit,
                                        programProductGroup, mpProgramOrderProduct, programSanSaleOffer);
                            }
                        } else if (totalProductOrComboQuantity >= programProductGroup.getFromValue()) {
                            isExit = true;
                            ltProgramOffer.add(programProductGroup.getOffer());
                            checkUpdateSanSaleForProduct(agency, 0, totalProductOrComboQuantity, isExit,
                                    programProductGroup, mpProgramOrderProduct, programSanSaleOffer);
                            totalProductOrComboQuantity = 0;
                        }
                        mpProductGroupQuantity.put(programProductGroup.getDataIndex(), totalProductOrComboQuantity);
                    }
                    if (isExit)
                        return ltProgramOffer;
                }
            }
        } catch (Exception ex) {
            ltProgramOffer = new ArrayList<>();
            LogUtil.printDebug("", ex);
        }
        return ltProgramOffer;
    }

    private void handleSanSaleOfferForProduct(List<ProgramOffer> ltProgramOffer,
                                              ProgramSanSaleOffer responseProgramSanSaleOffer) {
        try {
            for (ProgramOffer programOffer : ltProgramOffer) {
                if (programOffer.getOfferType() == ProgramOfferType.MONEY_DISCOUNT) {
                    MoneyDiscountOffer moneyDiscountOffer = (MoneyDiscountOffer) programOffer;
                    responseProgramSanSaleOffer.getMpProductMoneyDiscount().putAll(
                            moneyDiscountOffer.getMpProductMoney()
                    );
                } else if (programOffer.getOfferType() == ProgramOfferType.PERCENT_DISCOUNT) {
                    PercentDiscountOffer percentDiscountOffer = (PercentDiscountOffer) programOffer;
                    responseProgramSanSaleOffer.getMpProductPercentDiscount().putAll(percentDiscountOffer.getMpProductPercent());
                } else if (programOffer.getOfferType() == ProgramOfferType.GIFT_OFFER) {
                    GiftOffer giftOffer = (GiftOffer) programOffer;
                    for (OfferProduct offerProduct : giftOffer.getLtBonusGift()) {
                        OfferProduct object = new OfferProduct(offerProduct);
                        if (!responseProgramSanSaleOffer.getLtBonusGift().contains(object))
                            responseProgramSanSaleOffer.getLtBonusGift().add(object);
                        else {
                            int index = responseProgramSanSaleOffer.getLtBonusGift().indexOf(object);
                            OfferProduct bonusGift = responseProgramSanSaleOffer.getLtBonusGift().get(index);
                            bonusGift.setQuantity(bonusGift.getQuantity() + object.getQuantity());
                        }
                    }
                } else if (programOffer.getOfferType() == ProgramOfferType.FIXED_PRICE) {
                    FixedPriceOffer fixedPriceOffer = (FixedPriceOffer) programOffer;
                    responseProgramSanSaleOffer.getMpProductFixedPrice().putAll(fixedPriceOffer.getMpProductPrice());
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
    }

    /**
     * Xử lý phần dữ liệu cho sản phẩm trong săn sale
     */
    private boolean checkSanSaleLimitForCombo(Agency agency, Program program, ProgramLimit limit,
                                              Map<Integer, ProgramOrderProduct> mpProgramOrderProduct,
                                              ProgramSanSaleOffer programSanSaleOffer) {
        try {
            for (ProgramProductGroup programProductGroup : limit.getLtProgramProductGroup()) {
                long totalComboQuantity = 0;
                if (programProductGroup.isCombo()) {
                    Map<Integer, Integer> mpRatio = new LinkedHashMap<>();
                    for (ProgramProduct programProduct : programProductGroup.getLtProgramProduct()) {
                        if (!mpProgramOrderProduct.containsKey(programProduct.getProduct().getId()))
                            break;
                        int quantity = mpProgramOrderProduct.get(programProduct.getProduct().getId()).getProductQuantity();
                        if (quantity < programProduct.getQuantity())
                            break;
                        int ratio = quantity / programProduct.getQuantity();
                        mpRatio.put(programProduct.getProduct().getId(), ratio);
                    }
                    if (mpRatio.size() == programProductGroup.getLtProgramProduct().size()) {
                        Optional<Integer> minValue = mpRatio.values().stream().min(Integer::compareTo);
                        totalComboQuantity = minValue.orElse(0);
                    }
                    if (totalComboQuantity >= programProductGroup.getFromValue() && !program.isEndLimit()) {
                        return true;
                    } else if (totalComboQuantity >= programProductGroup.getFromValue() && programProductGroup.getEndValue() > 0)
                        return true;
                    else if (totalComboQuantity >= programProductGroup.getFromValue())
                        return true;
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return false;
    }

    /**
     * Lấy danh sách sản phẩm, combo hiển thị trang chủ chương trình săn sale
     */
    public List<SanSaleItem> getSanSaleItem(Agency agency) {
        List<SanSaleItem> ltSanSaleItem = new ArrayList<>();
        try {
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ltSanSaleItem;
    }

    /**
     * Lấy danh sách chương trình săn sale có chứa sản phẩm
     */
    public List<SanSaleItem> getSanSaleForProduct(Product product, Agency agency) {
        List<SanSaleItem> ltSanSaleItem = new ArrayList<>();
        try {
            List<Program> ltSanSale = new ArrayList<>(this.dataManager.getProgramManager().getMpHuntSale().values());
            // Sắp xếp săn sale theo thứ tự ưu tiên
            sortUtil.sortProgram(ltSanSale);
            // Lấy thông tin công nợ hiện tại của đại lý
            DeptInfo deptInfo = this.dataManager.getProgramManager().getDeptInfo(agency.getId());
            for (Program program : ltSanSale) {
                if (program.isSanSaleRunning()
                        && program.getMpProduct().get(product.getId()) != null
                        && checkProgramFilter(agency, program, Source.APP, deptInfo)) {
                    SanSaleItem sanSaleItem = new SanSaleItem();
                    sanSaleItem.setProgram(program);
                    sanSaleItem.setType(SanSaleItemType.PRODUCT);
                    sanSaleItem.setProduct(program.getMpProduct().get(product.getId()));
                    ltSanSaleItem.add(sanSaleItem);
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ltSanSaleItem;
    }

    /**
     * Lấy giá ưu đãi săn sale tốt nhất cho 1 sản phẩm
     * Lấy dựa vào chương trình săn sale có độ ưu tiên cao nhất, có hạn mức cao nhất
     * Nếu tặng quà, thì giá bằng giá bán hiện tại
     */
    public double getSanSalePriceForProduct(Product product, ProductDataSetting productDataSetting, Agency agency) {
        double price = 0;
        try {
            List<Program> ltSanSale = new ArrayList<>(this.dataManager.getProgramManager().getMpHuntSale().values());
            // Sắp xếp săn sale theo thứ tự ưu tiên
            sortUtil.sortProgram(ltSanSale);
            // Lấy thông tin công nợ hiện tại của đại lý
            DeptInfo deptInfo = this.dataManager.getProgramManager().getDeptInfo(agency.getId());
            for (Program program : ltSanSale) {
                if (program.isSanSaleRunning()
                        && program.getMpProduct().get(product.getId()) != null
                        && checkProgramFilter(agency, program, Source.APP, deptInfo)) {
                    for (ProgramLimit programLimit : program.getLtProgramLimit()) {
                        for (ProgramProductGroup programProductGroup : programLimit.getLtProgramProductGroup()) {
                            ProgramProduct programProduct = new ProgramProduct();
                            programProduct.setProduct(product);
                            programProduct.setDescription("");
                            if (!programProductGroup.isCombo() && programProductGroup.getLtProgramProduct().contains(programProduct)) {
                                ProgramOffer programOffer = programProductGroup.getOffer();
                                if (programOffer.getOfferType() == ProgramOfferType.MONEY_DISCOUNT) {
                                    MoneyDiscountOffer moneyDiscountOffer = (MoneyDiscountOffer) programOffer;
                                    double moneyDiscount = moneyDiscountOffer.getMpProductMoney().get(product.getId());
                                    price = productDataSetting.getProductPrice() - moneyDiscount;
                                } else if (programOffer.getOfferType() == ProgramOfferType.PERCENT_DISCOUNT) {
                                    PercentDiscountOffer percentDiscountOffer = (PercentDiscountOffer) programOffer;
                                    int percentDiscount = percentDiscountOffer.getMpProductPercent().get(product.getId());
                                    double value = (percentDiscount * productDataSetting.getProductPrice() * 1.0) / 100;
                                    long moneyDiscount = Math.round(value);
                                    price = productDataSetting.getProductPrice() - moneyDiscount;
                                } else if (programOffer.getOfferType() == ProgramOfferType.FIXED_PRICE) {
                                    FixedPriceOffer fixedPriceOffer = (FixedPriceOffer) programOffer;
                                    price = fixedPriceOffer.getMpProductPrice().get(product.getId());
                                }
                            }
                        }
                        break;
                    }
                    break;
                }
            }
        } catch (Exception ex) {
            price = 0;
            LogUtil.printDebug("", ex);
        }
        if (price <= 0)
            price = productDataSetting.getProductPrice();
        return price;
    }

    private long getLimitProductQuantity(Agency agency,
                                         int productId,
                                         Program program) {
        long maxOfferPerPromo = 0;
        long maxOfferPerAgency = 0;
        long soldOfferPromo = 0;
        long soldOfferPerAgency = 0;
        if (program.getMpProductWithCombo().containsKey(productId)) {
            maxOfferPerPromo = program.getMpProductWithCombo().get(productId).getMaxOfferPerPromo();
            maxOfferPerAgency = program.getMpProductWithCombo().get(productId).getMaxOfferPerAgency();
            soldOfferPromo = this.dataManager.getProgramManager().getQuantitySoldProductForSanSale(program.getId(), productId);
            soldOfferPerAgency = this.dataManager.getProgramManager().getQuantitySoldProductForSanSale(program.getId(), productId, agency.getId());
        }
        if (maxOfferPerPromo > 0) { // có thiết lập số lượng tối đa cho chương trình
            maxOfferPerPromo = maxOfferPerPromo - soldOfferPromo;
            if (maxOfferPerPromo < 0)
                maxOfferPerPromo = 0;
        } else
            maxOfferPerPromo = -1;
        if (maxOfferPerAgency > 0) { // có thiết lập số lượng tối đa cho đại lý
            maxOfferPerAgency = maxOfferPerAgency - soldOfferPerAgency;
            if (maxOfferPerAgency < 0)
                maxOfferPerAgency = 0;
        } else
            maxOfferPerAgency = -1;

        if (maxOfferPerAgency == -1) {
            return maxOfferPerPromo;
        } else {
            if (maxOfferPerPromo == -1)
                return maxOfferPerAgency;
            else
                return Math.min(maxOfferPerAgency, maxOfferPerPromo);
        }
    }

    public double getSanSalePrice(int applyPrivatePrice, double privatePrice, double commonPrice) {
        if (privatePrice <= 0 || commonPrice <= 0)
            return 0;
        if (applyPrivatePrice == 1)
            return privatePrice;
        return Math.max(privatePrice, commonPrice);
    }

    /**
     * Tạo bộ lọc
     */
    public boolean checkProgramFilterByPromoApplyObject(
            Agency agency,
            Source source,
            DeptInfo deptInfo,
            Program program) {
        try {
            //Kiểm tra đại lý xem có bị chặn csbh
            if (program.getType()
                    == ProgramType.SALE_POLICY && agency.isBlockCsbh()) {
                return false;
            }
            // kiểm tra đại lý xem có bị chặn ctsn
            if (program.getType() == ProgramType.PROMOTION && agency.isBlockCtsn() && program.isBirthdayFilter())
                return false;
            // kiểm tra đại lý xem có bị chặn ctkm ngoại trừ ctsn
            if (program.getType() == ProgramType.PROMOTION && agency.isBlockCtkm() && !program.isBirthdayFilter())
                return false;
            // kiểm tra giới hạn số lượng đơn hàng theo tất cả

            if (program.getUserLimit() > 0) {
                int countOrderByProgram = this.dataManager.getProgramManager().countOrderByProgram(0, program.getId());
                if (countOrderByProgram >= program.getUserLimit())
                    return false;
            }

            // kiểm tra giới hạn số lượng đơn hàng theo đại lý
            if (program.getUseLimitPerAgency() > 0) {
                int countOrderByProgram = this.dataManager.getProgramManager().countOrderByProgram(agency.getId(), program.getId());
                if (countOrderByProgram >= program.getUseLimitPerAgency())
                    return false;
            }
            // Loại trừ đại lý
            if (program.getLtIgnoreAgencyId().contains(agency.getId()))
                return false;
            // Bao gồm đại lý
            if (program.getLtIncludeAgencyId().contains(agency.getId()))
                return true;
            if (program.getLtIncludeAgencyId().isEmpty() && program.getLtProgramFilter().isEmpty())
                return true;
            if (!program.getLtIncludeAgencyId().isEmpty() && program.getLtProgramFilter().isEmpty())
                return false;
            // Bộ lọc
            for (ProgramFilter programFilter : program.getLtProgramFilter()) {
                // Kiểm tra cấp bậc
                boolean isMatchedMembership = true;
                if (!programFilter.getLtAgencyMembershipId().isEmpty())
                    isMatchedMembership = programFilter.getLtAgencyMembershipId().contains(agency.getMembershipId());
                if (!isMatchedMembership)
                    continue;
                // Kiểm tra phòng kinh doanh
                boolean isMatchedAgencyBusinessDepartment = true;
                if (!programFilter.getLtAgencyBusinessDepartmentId().isEmpty())
                    isMatchedAgencyBusinessDepartment = programFilter.getLtAgencyBusinessDepartmentId().contains(agency.getBusinessDepartmentId());
                if (!isMatchedAgencyBusinessDepartment)
                    continue;
                // Kiểm tra tỉnh - tp
                boolean isMatchedAgencyCity = true;
                if (!programFilter.getLtAgencyCityId().isEmpty())
                    isMatchedAgencyCity = programFilter.getLtAgencyCityId().contains(agency.getCityId());
                if (!isMatchedAgencyCity)
                    continue;
                // Kiểm tra ngày sinh nhật
                boolean isMatchedBirthday = true;
                if (programFilter.isBirthday()) {
                    if (StringUtils.isBlank(agency.getBirthday()))
                        isMatchedBirthday = false;
                    else {
                        String pattern = "dd/MM";
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                        String now = simpleDateFormat.format(new Date());
                        isMatchedBirthday = now.equals(agency.getBirthday().substring(0, 5));
                    }
                }
                if (!isMatchedBirthday)
                    continue;
                // Kiểm tra giới tính
                boolean isMatchedGender = true;
                if (!programFilter.getLtGender().isEmpty())
                    isMatchedGender = programFilter.getLtGender().contains(agency.getGender());
                if (!isMatchedGender)
                    continue;
                // Kiểm tra khoảng cách đơn hàng cuối
                boolean isMatchedDiffOrderDay = true;
                if (programFilter.getDiffOrderDay() > 0) {
                    Order order = this.getLatestCompletedOrder(agency.getId(), "", "");
                    if (order == null)
                        isMatchedDiffOrderDay = false;
                    else {
                        long time = System.currentTimeMillis() - order.getConfirmDeliveryDate().getTime();
                        long day = TimeUnit.DAYS.convert(time, TimeUnit.MILLISECONDS);
                        isMatchedDiffOrderDay = (day <= programFilter.getDiffOrderDay());
                    }
                }
                if (!isMatchedDiffOrderDay)
                    continue;
                boolean isSource = true;
                if (!programFilter.getLtSource().isEmpty())
                    isSource = (programFilter.getLtSource().contains(source.getValue()));
                if (!isSource)
                    continue;
                // Kiểm tra số lần cam kết sai
                boolean isFailedCommit = true;
                if (programFilter.getSaiCamKet() > 0) {
                    int countFailedCommit = this.countFailedCommit(agency.getId());
                    isFailedCommit = !(countFailedCommit > programFilter.getSaiCamKet());
                }
                if (!isFailedCommit)
                    continue;
                // kiểm tra nợ xấu
                boolean isNX = true;
                if (programFilter.isNx())
                    isNX = !(deptInfo.getNx() > 0);
                if (!isNX)
                    continue;
                // kiểm tra nợ quá hạn trong khoảng
                boolean isNqhRange = true;
                if (programFilter.getFromNqh() > 0 && programFilter.getEndNqh() > 0)
                    isNqhRange = (deptInfo.getNqh() >= programFilter.getFromNqh() && deptInfo.getNqh() <= programFilter.getEndNqh());
                else if (programFilter.getFromNqh() > 0)
                    isNqhRange = (deptInfo.getNqh() >= programFilter.getFromNqh());
                else if (programFilter.getEndNqh() > 0)
                    isNqhRange = (deptInfo.getNqh() <= programFilter.getEndNqh());
                if (!isNqhRange)
                    continue;
                // kiểm tra có nợ quá hạn
                boolean isNqh = true;
                if (programFilter.isNqh())
                    isNqh = !(deptInfo.getNqh() > 0);
                if (!isNqh)
                    continue;
                // kiểm tra cho phép hạn mức gối đầu
                boolean isNgdLimit = true;
                if (programFilter.getNgdLimitStatus() == 0)
                    isNgdLimit = (deptInfo.getNgdLimit() <= 0);
                else if (programFilter.getNgdLimitStatus() == 1)
                    isNgdLimit = (deptInfo.getNgdLimit() > 0);
                if (!isNgdLimit)
                    continue;
                // kiểm tra kỳ hạn nợ trong khoảng
                boolean isDeptCycleRange = true;
                if (programFilter.getFromDeptCycle() > 0 && programFilter.getEndDeptCycle() > 0)
                    isDeptCycleRange = (deptInfo.getDeptCycle() >= programFilter.getFromDeptCycle() && deptInfo.getDeptCycle() <= programFilter.getEndDeptCycle());
                else if (programFilter.getFromDeptCycle() > 0)
                    isDeptCycleRange = (deptInfo.getDeptCycle() >= programFilter.getFromDeptCycle());
                else if (programFilter.getEndDeptCycle() > 0)
                    isDeptCycleRange = (deptInfo.getDeptCycle() <= programFilter.getEndDeptCycle());
                if (!isDeptCycleRange)
                    continue;
                // kiểm tra hạn mức nợ trong khoảng
                boolean isDeptLimitRange = true;
                if (programFilter.getFromDeptLimit() > 0 && programFilter.getEndDeptLimit() > 0)
                    isDeptLimitRange = (deptInfo.getDeptLimit() >= programFilter.getFromDeptLimit() && deptInfo.getDeptLimit() <= programFilter.getEndDeptLimit());
                else if (programFilter.getFromDeptLimit() > 0)
                    isDeptLimitRange = (deptInfo.getDeptLimit() >= programFilter.getFromDeptLimit());
                else if (programFilter.getEndDeptLimit() > 0)
                    isDeptLimitRange = (deptInfo.getDeptLimit() <= programFilter.getEndDeptLimit());
                if (!isDeptLimitRange)
                    continue;
                // kiểm tra doanh thu thuần trong khoảng
                boolean isDttRange = true;
                if (programFilter.getFromDtt() > 0 && programFilter.getEndDtt() > 0)
                    isDttRange = (deptInfo.getTotalDttCycle() >= programFilter.getFromDtt() && deptInfo.getTotalDttCycle() <= programFilter.getEndDtt());
                else if (programFilter.getFromDtt() > 0)
                    isDttRange = (deptInfo.getTotalDttCycle() >= programFilter.getFromDtt());
                else if (programFilter.getEndDtt() > 0)
                    isDttRange = (deptInfo.getTotalDttCycle() <= programFilter.getEndDtt());
                if (!isDttRange)
                    continue;
                // kiểm tra doanh số trong khoảng
                boolean isTotalPriceSaleRange = true;
                if (programFilter.getFromTotalPriceSales() > 0 && programFilter.getEndTotalPriceSales() > 0)
                    isTotalPriceSaleRange = (deptInfo.getTotalPriceSales() >= programFilter.getFromTotalPriceSales() && deptInfo.getTotalPriceSales() <= programFilter.getEndTotalPriceSales());
                else if (programFilter.getFromTotalPriceSales() > 0)
                    isTotalPriceSaleRange = (deptInfo.getTotalPriceSales() >= programFilter.getFromTotalPriceSales());
                else if (programFilter.getEndTotalPriceSales() > 0)
                    isTotalPriceSaleRange = (deptInfo.getTotalPriceSales() <= programFilter.getEndTotalPriceSales());
                if (!isTotalPriceSaleRange)
                    continue;
                // kiểm tra công nợ cuối kỳ trong khoảng
                boolean isDeptCycleEndRange = true;
                if (programFilter.getFromDeptCycleEnd() > 0 && programFilter.getEndDeptCycleEnd() > 0)
                    isDeptCycleEndRange = (deptInfo.getDeptCycleEnd() >= programFilter.getFromDeptCycleEnd() && deptInfo.getDeptCycleEnd() <= programFilter.getEndDeptCycleEnd());
                else if (programFilter.getFromDeptCycleEnd() > 0)
                    isDeptCycleEndRange = (deptInfo.getDeptCycleEnd() >= programFilter.getFromDeptCycleEnd());
                else if (programFilter.getEndDeptCycleEnd() > 0)
                    isDeptCycleEndRange = (deptInfo.getDeptCycleEnd() <= programFilter.getEndDeptCycleEnd());
                if (!isDeptCycleEndRange)
                    continue;
                // kiểm tra tiền thu trong khoảng
                boolean isTtRange = true;
                if (programFilter.getFromTt() > 0 && programFilter.getEndTt() > 0)
                    isTtRange = (deptInfo.getTotalTtCycle() >= programFilter.getFromTt() && deptInfo.getTotalTtCycle() <= programFilter.getEndTt());
                else if (programFilter.getFromTt() > 0)
                    isTtRange = (deptInfo.getTotalTtCycle() >= programFilter.getFromTt() && deptInfo.getTotalTtCycle() <= programFilter.getEndTt());
                else if (programFilter.getEndTt() > 0)
                    isTtRange = (deptInfo.getTotalTtCycle() <= programFilter.getEndTt());
                if (!isTtRange)
                    continue;
                return true;
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return false;
    }

    /**
     * Kiểm tra ưu đãi sản phẩm cho chương trình đam mê
     */
    public Map<Integer, TLProductReward> checkDMForProduct(Agency agency, Map<Integer, ProgramOrderProduct> mpProgramOrderProduct) {
        Map<Integer, TLProductReward> mpProductReward = new LinkedHashMap<>();
        try {
            List<Program> ltCtdm = new ArrayList<>(
                    this.dataManager.getProgramManager().getMpDamMe().values());
            List<Integer> ltCtdmId = ltCtdm
                    .stream()
                    .filter(ctdm -> !ctdm.isCancel() && ctdm.isRunning())
                    .map(Program::getId)
                    .collect(Collectors.toList());
            if (ltCtdmId.isEmpty()) {
                return mpProductReward;
            }
            Map<Integer, Integer> mpProgramReward = this.dataManager.getProgramManager().getDMProgramReward(agency.getId(), ltCtdmId);
            for (Integer programId : mpProgramReward.keySet()) {
                Program ctdm = getProgramById(programId);
                int percent = mpProgramReward.get(programId);
                for (Integer productId : mpProgramOrderProduct.keySet()) {
                    boolean isContain = this.checkContainProduct(agency, productId, ctdm);
                    if (isContain) {
                        TLProductReward tlProductReward = mpProductReward.get(productId);
                        if (tlProductReward == null) {
                            tlProductReward = new TLProductReward();
                            tlProductReward.setProductId(productId);
                            tlProductReward.setProgram(ctdm);
                            tlProductReward.setOfferPercent(percent);
                        } else if (tlProductReward.getOfferPercent() < percent) {
                            tlProductReward.setProductId(productId);
                            tlProductReward.setProgram(ctdm);
                            tlProductReward.setOfferPercent(percent);
                        }
                        double tmp = (double) (tlProductReward.getOfferPercent() * mpProgramOrderProduct.get(productId).getBeginPrice()) / 100;
                        long price = new BigDecimal(tmp).setScale(0, RoundingMode.HALF_UP).longValue();
                        tlProductReward.setOfferValue(price);
                        mpProductReward.put(productId, tlProductReward);
                    }
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return mpProductReward;
    }

    private boolean checkContainProduct(Agency agency, Integer productId, Program ctdm) {
        ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(
                productId
        );
        if (productCache == null) {
            return false;
        }

        for (ProgramItem programItem : ctdm.getLtProgramItem()) {
            if (programItem.getItemType() == ProgramItemType.CATEGORY &&
                    (programItem.getItemId() == productCache.getMat_hang_id() ||
                            programItem.getItemId() == productCache.getPlsp_id() ||
                            programItem.getItemId() == productCache.getPltth_id())
            ) {
                return true;
            } else if (programItem.getItemType() == ProgramItemType.PRODUCT_GROUP &&
                    (programItem.getItemId() == productCache.getProduct_group_id())
            ) {
                return true;
            } else if (programItem.getItemType() == ProgramItemType.PRODUCT &&
                    (programItem.getItemId() == productCache.getId())
            ) {
                return true;
            }
        }

        return false;
    }

    protected String getCmsCSDMDescriptionForProduct(int offerPercent) {
        return "Được giảm " + (offerPercent == 0 ? "" : (offerPercent + "% ")) + "khi mua sản phẩm";
    }

    public void checkQuotationForProduct(
            int business_department_id,
            Map<Integer, ProgramOrderProduct> mpProgramOrderProduct,
            Source source,
            Map<Integer, ProgramSanSaleOffer> mpResponseProgramSanSaleOffer
    ) {
        if (mpProgramOrderProduct.isEmpty())
            return;
        try {
            List<CreatePromoRequest> ltSanSale = this.loadQOM(business_department_id);
            // Lấy thông tin công nợ hiện tại của đại lý
            for (Map.Entry<Integer, ProgramOrderProduct> programOrderProductEntry : mpProgramOrderProduct.entrySet()) {
                for (CreatePromoRequest program : ltSanSale) {
                    if (programOrderProductEntry.getValue().getProductId() == program.getPromo_item_groups().get(0).getProducts().get(0).getItem_id()) {
                        List<PromoLimitRequest> promoLimitRequestList = program.getPromo_limits();
                        Collections.sort(promoLimitRequestList, (a, b) -> a.getId() > b.getId() ? -1 : a.getId() == b.getId() ? 0 : 1);
                        for (int iLimit = program.getPromo_limits().size() - 1;
                             iLimit >= 0; iLimit--) {
                            if (programOrderProductEntry.getValue().getProductQuantity() >=
                                    program.getPromo_limits().get(iLimit).getPromo_limit_groups().get(0).getFrom_value()) {
                                ProgramSanSaleOffer programSanSaleOffer = new ProgramSanSaleOffer();
                                programSanSaleOffer.getMpProductFixedPrice().put(
                                        programOrderProductEntry.getValue().getProductId(),
                                        program.getPromo_limits().get(iLimit).getPromo_limit_groups().get(0).getOffer().getOffer_products().get(0).getOffer_value()
                                );
                                mpResponseProgramSanSaleOffer.put(program.getPromo_info().getId(), programSanSaleOffer);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
    }

    private List<CreatePromoRequest> loadQOM(int businessDepartmentId) {
        List<CreatePromoRequest> result = new ArrayList<>();
        try {
            List<JSONObject> qomList = this.pomDB.getListQOMRunning(businessDepartmentId);
            for (JSONObject jsQOM : qomList) {
                result.add(JsonUtils.DeSerialize(jsQOM.get("data").toString(), CreatePromoRequest.class));
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return result;
    }

    private List<ProgramOffer> handleQuotationForProduct(
            Program program,
            Map<Integer, ProgramOrderProduct> mpProgramOrderProduct,
            ProgramSanSaleOffer programSanSaleOffer) {
        List<ProgramOffer> ltProgramOffer = new ArrayList<>();
        try {
            if (program.getConditionType() == ProgramConditionType.PRODUCT_QUANTITY) {
                Map<Integer, Long> mpProductGroupQuantity = new LinkedHashMap<>();
                // Kiểm tra hạn mức cao nhất
                for (ProgramLimit programLimit : program.getLtProgramLimit()) {
                    boolean isExit = false;
                    for (ProgramProductGroup programProductGroup : programLimit.getLtProgramProductGroup()) {
                        long totalProductOrComboQuantity = 0; // tổng số lượng sản phẩm trong nhóm hoặc số lượng combo
                        if (programProductGroup.isCombo()) { // tính số lượng combo thỏa điều kiện
                            Map<Integer, Integer> mpRatio = new LinkedHashMap<>();
                            for (ProgramProduct programProduct : programProductGroup.getLtProgramProduct()) {
                                if (!mpProgramOrderProduct.containsKey(programProduct.getProduct().getId()))
                                    break;
                                int quantity = mpProgramOrderProduct.get(programProduct.getProduct().getId()).getProductQuantity();
                                if (quantity < programProduct.getQuantity())
                                    break;
                                int ratio = quantity / programProduct.getQuantity();
                                mpRatio.put(programProduct.getProduct().getId(), ratio);
                            }
                            if (mpRatio.size() == programProductGroup.getLtProgramProduct().size()) {
                                Optional<Integer> minValue = mpRatio.values().stream().min(Integer::compareTo);
                                totalProductOrComboQuantity = minValue.orElse(0);
                            }
                        } else { // Tính tổng số lượng sản phẩm thỏa điều kiện
                            totalProductOrComboQuantity = programProductGroup.getLtProgramProduct().stream()
                                    .filter(object -> mpProgramOrderProduct.containsKey(object.getProduct().getId()))
                                    .map(object -> mpProgramOrderProduct.get(object.getProduct().getId()).getProductQuantity())
                                    .reduce(0, Integer::sum);
                        }
                        if (totalProductOrComboQuantity >= programProductGroup.getFromValue() && !program.isEndLimit()) {
                            if (programProductGroup.getOffer().getOfferType() == ProgramOfferType.MONEY_DISCOUNT ||
                                    programProductGroup.getOffer().getOfferType() == ProgramOfferType.PERCENT_DISCOUNT ||
                                    programProductGroup.getOffer().getOfferType() == ProgramOfferType.FIXED_PRICE)
                                isExit = true;
                            ltProgramOffer.add(programProductGroup.getOffer());
                            totalProductOrComboQuantity -= programProductGroup.getFromValue();
                            checkUpdateQuotationForProduct(programProductGroup.getFromValue(), totalProductOrComboQuantity, isExit,
                                    programProductGroup, mpProgramOrderProduct, programSanSaleOffer);
                        } else if (totalProductOrComboQuantity >= programProductGroup.getFromValue() && programProductGroup.getEndValue() > 0) {
                            if (programProductGroup.getOffer().getOfferType() == ProgramOfferType.MONEY_DISCOUNT ||
                                    programProductGroup.getOffer().getOfferType() == ProgramOfferType.PERCENT_DISCOUNT ||
                                    programProductGroup.getOffer().getOfferType() == ProgramOfferType.FIXED_PRICE)
                                isExit = true;
                            ltProgramOffer.add(programProductGroup.getOffer());
                            totalProductOrComboQuantity -= programProductGroup.getUnderEndValue();
                            if (totalProductOrComboQuantity < 0)
                                totalProductOrComboQuantity = 0;
                            checkUpdateQuotationForProduct(programProductGroup.getUnderEndValue(), totalProductOrComboQuantity, isExit,
                                    programProductGroup, mpProgramOrderProduct, programSanSaleOffer);
                        } else if (totalProductOrComboQuantity >= programProductGroup.getFromValue()) {
                            isExit = true;
                            ltProgramOffer.add(programProductGroup.getOffer());
                            checkUpdateQuotationForProduct(0, totalProductOrComboQuantity, isExit,
                                    programProductGroup, mpProgramOrderProduct, programSanSaleOffer);
                            totalProductOrComboQuantity = 0;
                        }
                        mpProductGroupQuantity.put(programProductGroup.getDataIndex(), totalProductOrComboQuantity);
                    }
                    if (isExit)
                        return ltProgramOffer;
                    break;
                }
                // Kiểm tra bước nhảy
                if (!ltProgramOffer.isEmpty() && program.getLtStepLimit() != null) {
                    for (ProgramProductGroup programProductGroup : program.getLtStepLimit().getLtProgramProductGroup()) {
                        long totalProductOrComboQuantity = 0;
                        if (mpProductGroupQuantity.containsKey(programProductGroup.getDataIndex()))
                            totalProductOrComboQuantity = mpProductGroupQuantity.get(programProductGroup.getDataIndex());
                        while (totalProductOrComboQuantity >= programProductGroup.getFromValue()) {
                            ltProgramOffer.add(programProductGroup.getOffer());
                            totalProductOrComboQuantity -= programProductGroup.getFromValue();
                            checkUpdateQuotationForProduct(programProductGroup.getFromValue(), totalProductOrComboQuantity, false,
                                    programProductGroup, mpProgramOrderProduct, programSanSaleOffer);
                        }
                        mpProductGroupQuantity.put(programProductGroup.getDataIndex(), totalProductOrComboQuantity);
                    }
                }
                // Kiểm tra tất cả hạn mức
                for (ProgramLimit programLimit : program.getLtProgramLimit()) {
                    boolean isExit = false;
                    for (ProgramProductGroup programProductGroup : programLimit.getLtProgramProductGroup()) {
                        long totalProductOrComboQuantity = 0;
                        if (mpProductGroupQuantity.containsKey(programProductGroup.getDataIndex()))
                            totalProductOrComboQuantity = mpProductGroupQuantity.get(programProductGroup.getDataIndex());
                        if (totalProductOrComboQuantity >= programProductGroup.getFromValue() && !program.isEndLimit()) {
                            long offerNumber = totalProductOrComboQuantity / programProductGroup.getFromValue();
                            if (programProductGroup.getOffer().getOfferType() == ProgramOfferType.MONEY_DISCOUNT ||
                                    programProductGroup.getOffer().getOfferType() == ProgramOfferType.PERCENT_DISCOUNT ||
                                    programProductGroup.getOffer().getOfferType() == ProgramOfferType.FIXED_PRICE) {
                                offerNumber = 1;
                                isExit = true;
                            }
                            for (int i = 1; i <= offerNumber; i++) {
                                ltProgramOffer.add(programProductGroup.getOffer());
                                totalProductOrComboQuantity -= programProductGroup.getFromValue();
                                checkUpdateQuotationForProduct(programProductGroup.getFromValue(), totalProductOrComboQuantity, isExit,
                                        programProductGroup, mpProgramOrderProduct, programSanSaleOffer);
                            }
                        } else if (totalProductOrComboQuantity >= programProductGroup.getFromValue() && programProductGroup.getEndValue() > 0) {
                            long offerNumber = 1;
                            long endQuantity = programProductGroup.getUnderEndValue();
                            if (totalProductOrComboQuantity > endQuantity)
                                offerNumber = totalProductOrComboQuantity / endQuantity;
                            if (programProductGroup.getOffer().getOfferType() == ProgramOfferType.MONEY_DISCOUNT ||
                                    programProductGroup.getOffer().getOfferType() == ProgramOfferType.PERCENT_DISCOUNT ||
                                    programProductGroup.getOffer().getOfferType() == ProgramOfferType.FIXED_PRICE) {
                                offerNumber = 1;
                                isExit = true;
                            }
                            for (int i = 1; i <= offerNumber; i++) {
                                ltProgramOffer.add(programProductGroup.getOffer());
                                totalProductOrComboQuantity -= endQuantity;
                                if (totalProductOrComboQuantity < 0)
                                    totalProductOrComboQuantity = 0;
                                checkUpdateQuotationForProduct(endQuantity, totalProductOrComboQuantity, isExit,
                                        programProductGroup, mpProgramOrderProduct, programSanSaleOffer);
                            }
                        } else if (totalProductOrComboQuantity >= programProductGroup.getFromValue()) {
                            isExit = true;
                            ltProgramOffer.add(programProductGroup.getOffer());
                            checkUpdateQuotationForProduct(0, totalProductOrComboQuantity, isExit,
                                    programProductGroup, mpProgramOrderProduct, programSanSaleOffer);
                            totalProductOrComboQuantity = 0;
                        }
                        mpProductGroupQuantity.put(programProductGroup.getDataIndex(), totalProductOrComboQuantity);
                    }
                    if (isExit)
                        return ltProgramOffer;
                }
            }
        } catch (Exception ex) {
            ltProgramOffer = new ArrayList<>();
            LogUtil.printDebug("", ex);
        }
        return ltProgramOffer;
    }

    private void checkUpdateQuotationForProduct(
            long updateQuantity,
            long remainQuantity,
            boolean isExit,
            ProgramProductGroup programProductGroup,
            Map<Integer, ProgramOrderProduct> mpProgramOrderProduct,
            ProgramSanSaleOffer programSanSaleOffer) {
        if (programProductGroup.isCombo()) {
            long quantity = updateQuantity;
            if (isExit)
                quantity = (updateQuantity + remainQuantity);
            for (int i = 1; i <= quantity; i++) {
                int comboQuantity = 0;
                Map<Integer, Integer> mpRatio = new LinkedHashMap<>();
                for (ProgramProduct programProduct : programProductGroup.getLtProgramProduct()) {
                    if (!mpProgramOrderProduct.containsKey(programProduct.getProduct().getId()))
                        break;
                    int productQuantity = mpProgramOrderProduct.get(programProduct.getProduct().getId()).getProductQuantity();
                    if (productQuantity < programProduct.getQuantity())
                        break;
                    int ratio = productQuantity / programProduct.getQuantity();
                    mpRatio.put(programProduct.getProduct().getId(), ratio);
                }
                if (mpRatio.size() == programProductGroup.getLtProgramProduct().size()) {
                    Optional<Integer> minValue = mpRatio.values().stream().min(Integer::compareTo);
                    comboQuantity = minValue.orElse(0);
                }
                if (comboQuantity > 0) {
                    for (ProgramProduct programProduct : programProductGroup.getLtProgramProduct()) {
                        ProgramOrderProduct programOrderProduct = mpProgramOrderProduct.get(programProduct.getProduct().getId());
                        int productQuantity = programOrderProduct.getProductQuantity() - programProduct.getQuantity();
                        if (productQuantity <= 0)
                            mpProgramOrderProduct.remove(programProduct.getProduct().getId());
                        else {
                            programOrderProduct.setProductQuantity(productQuantity);
                            programOrderProduct.setBeginPrice(programOrderProduct.getProductQuantity() * programOrderProduct.getProductPrice());
                        }
                    }
                    int value = programSanSaleOffer.getMpComboQuantity().getOrDefault(programProductGroup.getComboId(), 0);
                    programSanSaleOffer.getMpComboQuantity().put(programProductGroup.getComboId(), value + 1);
                }
            }
        } else {
            if (isExit) {
                for (ProgramProduct programProduct : programProductGroup.getLtProgramProduct()) {
                    ProgramOrderProduct programOrderProduct = mpProgramOrderProduct.get(programProduct.getProduct().getId());
                    if (programOrderProduct != null) {
                        programSanSaleOffer.getMpProductQuantity().put(programProduct.getProduct().getId(), programOrderProduct.getProductQuantity());
                        mpProgramOrderProduct.remove(programOrderProduct.getProduct().getId());
                    }
                }
            } else {
                List<ProgramProduct> ltProgramProduct = new ArrayList<>(programProductGroup.getLtProgramProduct());
                for (ProgramProduct programProduct : ltProgramProduct) {
                    ProductDataSetting productDataSetting = new ProductDataSetting();
                    productDataSetting.setProductId(programProduct.getProduct().getId());
                    productDataSetting.setProductPrice(programProduct.getProduct().getPrice());
                    productDataSetting.setProductMinimumPurchase(programProduct.getProduct().getMinimumPurchase());
                    double sanSalePrice = getSanSalePrice(programProductGroup.getProgramLimit().getProgram().getApplyPrivatePrice(),
                            productDataSetting.getProductPrice(),
                            programProduct.getProduct().getPrice());
                    programProduct.setProductPrice(sanSalePrice);
                }
                int quantity = (int) updateQuantity;
                ltProgramProduct.sort(sortUtil.getProgramProductComparator());
                for (ProgramProduct programProduct : ltProgramProduct) {
                    ProgramOrderProduct programOrderProduct = mpProgramOrderProduct.get(programProduct.getProduct().getId());
                    if (programOrderProduct != null) {
                        if (programOrderProduct.getProductQuantity() < quantity) {
                            int tmp = programSanSaleOffer.getMpProductQuantity().getOrDefault(programProduct.getProduct().getId(), 0);
                            programSanSaleOffer.getMpProductQuantity().put(programProduct.getProduct().getId(), tmp + programOrderProduct.getProductQuantity());
                            quantity = quantity - programOrderProduct.getProductQuantity();
                            mpProgramOrderProduct.remove(programOrderProduct.getProduct().getId());
                        } else {
                            programOrderProduct.setProductQuantity(programOrderProduct.getProductQuantity() - quantity);
                            programOrderProduct.setBeginPrice(programOrderProduct.getProductQuantity() * programOrderProduct.getProductPrice());
                            if (programOrderProduct.getProductQuantity() == 0)
                                mpProgramOrderProduct.remove(programOrderProduct.getProduct().getId());
                            int tmp = programSanSaleOffer.getMpProductQuantity().getOrDefault(programProduct.getProduct().getId(), 0);
                            programSanSaleOffer.getMpProductQuantity().put(programProduct.getProduct().getId(), tmp + quantity);
                            break;
                        }
                    }
                }
            }
        }
    }
}