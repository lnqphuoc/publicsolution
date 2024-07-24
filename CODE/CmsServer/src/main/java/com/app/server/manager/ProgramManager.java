package com.app.server.manager;

import com.app.server.config.ConfigInfo;
import com.app.server.data.dto.agency.AgencyBasicData;
import com.app.server.data.dto.ctxh.CTXHAgencyResult;
import com.app.server.data.dto.program.*;
import com.app.server.data.dto.program.agency.Agency;
import com.app.server.data.dto.program.filter.ProgramFilter;
import com.app.server.data.dto.program.filter.ProgramFilterType;
import com.app.server.data.dto.program.limit.ProgramLimit;
import com.app.server.data.dto.program.offer.*;
import com.app.server.data.dto.program.product.*;
import com.app.server.data.dto.promo.PromoBasicData;
import com.app.server.data.dto.promo.PromoItemGroupData;
import com.app.server.data.dto.promo.PromoRunningData;
import com.app.server.data.entity.PromoEntity;
import com.app.server.data.request.promo.PromoApplyFilterDetailRequest;
import com.app.server.data.request.promo.PromoApplyFilterRequest;
import com.app.server.data.request.promo.PromoApplyObjectRequest;
import com.app.server.data.request.promo.PromoItemGroupDetailRequest;
import com.app.server.database.ProgramDB;
import com.app.server.database.PromoDB;
import com.app.server.enums.*;
import com.app.server.response.ClientResponse;
import com.app.server.service.ReloadService;
import com.app.server.utils.JsonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.dbconn.ClientManager;
import com.ygame.framework.dbconn.ManagerIF;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.JSONUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Getter
@Setter
@Service
public class ProgramManager {
    private ProgramDB programDB;

    @Autowired
    public void setProgramDB(ProgramDB programDB) {
        this.programDB = programDB;
    }

    private PromoDB promoDB;

    @Autowired
    public void setPromoDB(PromoDB promoDB) {
        this.promoDB = promoDB;
    }

    private ReloadService reloadService;

    @Autowired
    public void setReloadService(ReloadService reloadService) {
        this.reloadService = reloadService;
    }

    protected SortUtil sortUtil;

    @Autowired
    public void setSortUtil(SortUtil sortUtil) {
        this.sortUtil = sortUtil;
    }

    private Map<Integer, Brand> mpBrand = new LinkedHashMap<>(); // map of all brand of product
    private Map<Integer, Color> mpColor = new LinkedHashMap<>(); // map of color of product
    private Map<Integer, Unit> mpBigUnit = new LinkedHashMap<>(); // map of big unit of product
    private Map<Integer, Unit> mpSmallUnit = new LinkedHashMap<>(); // map of small unit of product
    private Map<Integer, Category> mpCategory = new LinkedHashMap<>(); // map of all category
    private List<Category> ltProductCategory = new ArrayList<>(); // list of product category
    private Map<Integer, ProductGroup> mpProductGroup = new ConcurrentHashMap<>(); // map of all product
    private Map<Integer, Product> mpProduct = new ConcurrentHashMap<>(); // map of all product
    private Map<Integer, String> mpPromoRunning = new ConcurrentHashMap<>();
    private Map<Integer, Program> mpSalePolicy = new ConcurrentHashMap<>();
    private Map<Integer, Program> mpPromotion = new ConcurrentHashMap<>();
    private Map<Integer, Program> mpHuntSale = new ConcurrentHashMap<>();
    private Map<Integer, Program> mpCTTL = new ConcurrentHashMap<>();
    private Map<Integer, Program> mpDamMe = new ConcurrentHashMap<>();
    private Map<Integer, Program> mpCTXH = new ConcurrentHashMap<>();

    public Product getProductById(int productId, String s) {
        Product product = mpProduct.get(productId);
        if (product == null) {
            product = this.loadOneProduct(productId);
        }

        return product;
    }

    public void loadData() {
        LogUtil.printDebug("PROMO-LOAD DATA");
        loadAllBrand();
        loadAllColor();
        loadAllBigUnit();
        loadAllSmallUnit();
        loadAllCategory();
        loadAllProductGroup();
        loadAllProduct();
        loadAllSalePolicy();
        loadDamMeRunning();
        loadCTXHRunning();
        LogUtil.printDebug("PROMO-LOAD DATA DONE");
    }

    public void loadAllSalePolicy() {
        LogUtil.printDebug("PROMO-LOAD DATA DONE");
        mpSalePolicy.clear();
        mpPromoRunning.clear();
        mpPromotion.clear();
        mpHuntSale.clear();
        mpCTTL.clear();
        this.loadAllProgram(mpSalePolicy, mpPromoRunning, mpPromotion, mpHuntSale, mpCTTL);
    }

    public void loadAllProductGroup() {
        mpProductGroup.clear();
        programDB.loadAllProductGroup(mpProductGroup, mpCategory);
    }

    public void loadAllBrand() {
        mpBrand.clear();
        programDB.loadAllBrand(mpBrand);
    }

    // load all color
    public void loadAllColor() {
        mpColor.clear();
        programDB.loadAllColor(mpColor);
    }

    // load all big unit
    private void loadAllBigUnit() {
        mpBigUnit.clear();
        programDB.loadAllBigUnit(mpBigUnit);
    }

    // load all small unit
    private void loadAllSmallUnit() {
        mpSmallUnit.clear();
        programDB.loadAllSmallUnit(mpSmallUnit);
    }

    // load all category
    public void loadAllCategory() {
        mpCategory.clear();
        programDB.loadAllCategory(mpCategory);
        indexAllCategory();
    }

    public void loadAllProduct() {
        mpProduct.clear();
        this.programDB.loadAllProduct(mpProduct, mpProductGroup, mpBrand);
    }

    public Product loadOneProduct(int id) {
        return this.programDB.loadOneProduct(mpProduct, mpProductGroup, mpBrand, id);
    }

    private void indexAllCategory() {
        try {
            List<Category> ltTmpProductCategory = new ArrayList<>();
            try {
                for (Category category : mpCategory.values()) {
                    category.getLtChild().clear();
                    category.setActivatedStatus(false);
                }
                List<Category> ltBranchCategory = new ArrayList<>();
                for (Category category : mpCategory.values()) {
                    if (category.getParentId() != 0) {
                        Category parent = mpCategory.get(category.getParentId());
                        if (parent != null) {
                            parent.getLtChild().add(category);
                            category.setParent(parent);
                        }
                    }
                    if (category.getIsBranch() == YesNoStatus.YES.getValue())
                        ltBranchCategory.add(category);
                }
                for (Category branchCategory : ltBranchCategory) {
                    if (branchCategory.getStatus() == ActiveStatus.ACTIVATED.getValue()) {
                        branchCategory.setActivatedStatus(true);
                        for (Category itemCategory : branchCategory.getLtChild()) {
                            if (itemCategory.getStatus() == ActiveStatus.ACTIVATED.getValue()) {
                                itemCategory.setActivatedStatus(true);
                                for (Category productCategory : itemCategory.getLtChild()) {
                                    if (productCategory.getStatus() == ActiveStatus.ACTIVATED.getValue()) {
                                        productCategory.setActivatedStatus(true);
                                        ltTmpProductCategory.add(productCategory);
                                        for (Category brandProductCategory : productCategory.getLtChild()) {
                                            if (brandProductCategory.getStatus() == ActiveStatus.ACTIVATED.getValue())
                                                brandProductCategory.setActivatedStatus(true);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                LogUtil.printDebug("", ex);
            }
            ltProductCategory.clear();
            ltProductCategory.addAll(ltTmpProductCategory);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
    }

    public List<PromoBasicData> getPromoByProduct(Integer product_id) {
        List<PromoBasicData> promoBasicDataList = new ArrayList<>();
        try {
            for (String program : mpPromoRunning.values()) {
                PromoRunningData promoRunningData = JsonUtils.DeSerialize(program, PromoRunningData.class);
                if (promoRunningData == null) {
                    continue;
                }
                if (PromoConditionType.ORDER_PRICE.getKey().equals(promoRunningData.getPromo_info().getCondition_type())) {
//                    PromoBasicData promoBasicData = new PromoBasicData();
//                    promoBasicData.setId(promoRunningData.getPromo_info().getId());
//                    promoBasicData.setCode(promoRunningData.getPromo_info().getCode());
//                    promoBasicData.setName(promoRunningData.getPromo_info().getName());
//                    promoBasicDataList.add(promoBasicData);
                } else {
                    for (PromoItemGroupData promoItemGroupData : promoRunningData.getPromo_item_groups()) {
                        PromoItemGroupDetailRequest promoItemGroupDetailRequest = promoItemGroupData.getProducts().stream()
                                .filter(item -> product_id == item.getItem_id()).findAny().orElse(null);
                        if (promoItemGroupDetailRequest != null) {
                            PromoBasicData promoBasicData = new PromoBasicData();
                            promoBasicData.setId(promoRunningData.getPromo_info().getId());
                            promoBasicData.setCode(promoRunningData.getPromo_info().getCode());
                            promoBasicData.setName(promoRunningData.getPromo_info().getName());
                            promoBasicData.setDescription(promoItemGroupDetailRequest.getNote());
                            promoBasicData.setCondition_type(promoRunningData.getPromo_info().getCondition_type());
                            promoBasicData.setPromo_type(promoRunningData.getPromo_info().getPromo_type());
                            promoBasicDataList.add(promoBasicData);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return promoBasicDataList;
    }


    public boolean reloadPromoRunning(
            int id,
            PromoScheduleType promoScheduleType,
            String promo_type) {
        if (PromoScheduleType.STOP.getKey().equals(promoScheduleType.getKey())) {
            if (PromoType.SALE_POLICY.getKey().equals(promo_type) ||
                    PromoType.PROMO.getKey().equals(promo_type) ||
                    PromoType.CTSS.getKey().equals(promo_type)) {
                this.removePromoRunning(id, promo_type);
            }
        } else if (PromoScheduleType.START.getKey().equals(promoScheduleType.getKey())) {
            this.loadPromoRunning(id);
        }

        this.reloadService.callAppServerReload(CacheType.PROMO.getValue(), id);

        return true;
    }

    public boolean reloadDamMeRunning(
            int id,
            PromoScheduleType promoScheduleType,
            String promo_type) {
        if (PromoScheduleType.STOP.getKey().equals(promoScheduleType.getKey())) {
            this.mpDamMe.remove(id);
            this.mpPromoRunning.remove(id);
        } else if (PromoScheduleType.START.getKey().equals(promoScheduleType.getKey())) {
            this.loadOneDamMe(id);
        }

        this.reloadService.callAppServerReload(CacheType.DAM_ME.getValue(), id);

        return true;
    }

    public boolean reloadCTXHRunning(
            int id,
            PromoScheduleType promoScheduleType,
            String promo_type) {
        if (PromoScheduleType.STOP.getKey().equals(promoScheduleType.getKey())) {
            this.mpPromoRunning.remove(id);
        } else if (PromoScheduleType.START.getKey().equals(promoScheduleType.getKey())) {
            this.loadOneCTXH(id);
        }

        this.reloadService.callAppServerReload(CacheType.BXH.getValue(), id);

        return true;
    }

    private void loadPromoRunning(int promo_id) {
        this.loadOneProgram(mpSalePolicy, mpPromoRunning, mpPromotion, mpHuntSale, mpCTTL, promo_id);
    }

    private void removePromoRunning(
            int id,
            String promo_type) {
        if (PromoType.SALE_POLICY.getKey().equals(promo_type)) {
            this.mpSalePolicy.remove(id);
        } else if (PromoType.PROMO.getKey().equals(promo_type)) {
            this.mpPromotion.remove(id);
        } else if (PromoType.CTSS.getKey().equals(promo_type)) {
            this.mpHuntSale.remove(id);
        }
        this.mpPromoRunning.remove(id);
    }

    public Agency getAgency(Integer agency_id) {
        return this.programDB.getAgency(agency_id);
    }

    public List<Agency> getListAgencyReadyJoinCTXH() {
        return this.programDB.getListAgencyReadyJoinCTXH();
    }

    public DeptInfo getDeptInfo(int id) {
        return this.programDB.getDeptInfo(id);
    }

    public int countOrderByProgram(int agencyId, int programId) {
        return this.programDB.countOrderByProgram(
                agencyId,
                programId,
                getListStatusForBuyingOrder());
    }

    public List<Integer> getListStatusForDeptWaiting() {
        return Arrays.asList(OrderStatus.PREPARE.getKey(), OrderStatus.WAITING_CONFIRM.getKey(),
                OrderStatus.SHIPPING.getKey(), OrderStatus.WAITING_APPROVE.getKey(),
                OrderStatus.RESPONSE.getKey());
    }

    /**
     * Lấy danh sách trạng thái đơn hàng đang tính đang mua
     */
    public List<Integer> getListStatusForBuyingOrder() {
        return Arrays.asList(OrderStatus.PREPARE.getKey(), OrderStatus.WAITING_CONFIRM.getKey(),
                OrderStatus.SHIPPING.getKey(), OrderStatus.WAITING_APPROVE.getKey(),
                OrderStatus.RESPONSE.getKey(), OrderStatus.COMPLETE.getKey());
    }

    public Program importProgram(String data) {
        Program program;
        try {
            JsonObject object = (JsonObject) JsonParser.parseString(data);
            JsonObject info = object.get("promo_info").getAsJsonObject();
            ProgramType programType = ProgramType.valueOf(info.get("promo_type").getAsString());
            if (programType == null) {
                return null;
            }
            program = new Program();
            program.setId(info.get("id").getAsInt());
            program.setType(programType);
            program.setCode(info.get("code").getAsString());
            program.setName(info.get("name").getAsString());
            program.setDescription(info.get("description").getAsString());
            program.setStatus(info.get("status").getAsInt());
            if (info.has("priority"))
                program.setPriority(info.get("priority").getAsInt());
            if (info.has("use_limit"))
                program.setUserLimit(info.get("use_limit").getAsInt());
            if (info.has("use_limit_per_agency"))
                program.setUseLimitPerAgency(info.get("use_limit_per_agency").getAsInt());
            if (info.has("promo_max_value"))
                program.setPromoMaxValue(info.get("promo_max_value").getAsLong());
            if (info.has("payment_duration"))
                program.setDeptCycle(info.get("payment_duration").getAsInt());
            if (info.has("show_at_hunt_sale"))
                program.setShowSanSale(info.get("show_at_hunt_sale").getAsInt());
            if (info.has("apply_for_private_price"))
                program.setApplyPrivatePrice(info.get("apply_for_private_price").getAsInt());
            if (info.has("circle_type"))
                program.setCircle_type(info.get("circle_type").getAsString());
            String image = ConvertUtils.toString(info.get("image"));
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
            Map<Integer, ProgramProductGroup> mpProgramProductGroup = new LinkedHashMap<>();
            JsonArray arrPromoItemGroups = object.get("promo_item_groups").getAsJsonArray();
            for (JsonElement elPromoItemGroup : arrPromoItemGroups) {
                ProgramProductGroup programProductGroup = new ProgramProductGroup();
                JsonObject obPromoItemGroup = elPromoItemGroup.getAsJsonObject();
                int dataIndex = obPromoItemGroup.get("data_index").getAsInt();
                JsonArray arrProduct = obPromoItemGroup.get("products").getAsJsonArray();
                boolean isCombo = (obPromoItemGroup.has("type") && obPromoItemGroup.get("type").getAsString().equals(SanSaleItemType.COMBO.name()));
                for (JsonElement elProduct : arrProduct) {
                    if (program.getType() == ProgramType.DAMME) {
                        JsonObject obProduct = elProduct.getAsJsonObject();
                        String description = obProduct.get("note").getAsString();
                        ProgramProduct programProduct = new ProgramProduct();
                        programProduct.setDescription(description);
                        if (obProduct.has("item_quantity"))
                            programProduct.setQuantity(obProduct.get("item_quantity").getAsInt());
                        if (obProduct.has("max_offer_per_promo"))
                            programProduct.setMaxOfferPerPromo(obProduct.get("max_offer_per_promo").getAsInt());
                        if (obProduct.has("max_offer_per_agency"))
                            programProduct.setMaxOfferPerAgency(obProduct.get("max_offer_per_agency").getAsInt());
                        programProduct.setItemId(obProduct.get("item_id").getAsInt());
                        programProduct.setItemType(ProgramItemType.valueOf(obProduct.get("item_type").getAsString()));
                        programProduct.setCategoryLevel(obProduct.get("category_level").getAsInt());
                        programProductGroup.getLtProgramProduct().add(programProduct);
                        ProgramItem programItem = new ProgramItem();
                        programItem.setItemType(programProduct.getItemType());
                        programItem.setItemId(programProduct.getItemId());
                        programItem.setCategoryLevel(programProduct.getCategoryLevel());
                        program.getLtProgramItem().add(programItem);
                    } else {
                        JsonObject obProduct = elProduct.getAsJsonObject();
                        Product product = this.getProductById(obProduct.get("item_id").getAsInt(), "");
                        if (product == null) {
                            return null;
                        }
                        String description = obProduct.get("note").getAsString();
                        ProgramProduct programProduct = new ProgramProduct();
                        programProduct.setProduct(product);
                        programProduct.setDescription(description);
                        if (obProduct.has("item_quantity"))
                            programProduct.setQuantity(obProduct.get("item_quantity").getAsInt());
                        if (obProduct.has("max_offer_per_promo"))
                            programProduct.setMaxOfferPerPromo(obProduct.get("max_offer_per_promo").getAsInt());
                        if (obProduct.has("max_offer_per_agency"))
                            programProduct.setMaxOfferPerAgency(obProduct.get("max_offer_per_agency").getAsInt());
                        if (product.getProductGroup() != null && product.getProductGroup().getCategory() != null && product.getProductGroup().getCategory().getParent() != null)
                            program.getMpProductCategory().put(product.getProductGroup().getCategory().getParent().getId(), 1);
                        if (!isCombo) {
                            program.getMpProduct().put(programProduct.getProduct().getId(), programProduct);
                        } else {
                            programProduct.setDescription(
                                    obPromoItemGroup.get("note").getAsString()
                            );
                        }
                        program.getMpProductWithCombo().put(programProduct.getProduct().getId(), programProduct);
                        programProductGroup.getLtProgramProduct().add(programProduct);
                    }
                }
                programProductGroup.setDataIndex(dataIndex);
                programProductGroup.setCombo(isCombo);
                if (obPromoItemGroup.has("code"))
                    programProductGroup.setCode(obPromoItemGroup.get("code").getAsString());
                if (obPromoItemGroup.has("note"))
                    programProductGroup.setNote(obPromoItemGroup.get("note").getAsString());
                if (obPromoItemGroup.has("images")) {
                    String strProgramProductGroupImage = obPromoItemGroup.get("images").getAsString();
                    if (StringUtils.isNotBlank(strProgramProductGroupImage)) {
                        JsonArray arrProgramProductGroupImage = (JsonArray) JsonParser.parseString(strProgramProductGroupImage);
                        for (JsonElement elProgramProductGroupImage : arrProgramProductGroupImage)
                            programProductGroup.getLtImage().add(elProgramProductGroupImage.getAsString());
                    }
                }
                if (obPromoItemGroup.has("full_name")) {
                    String programProductGroupName = obPromoItemGroup.get("full_name").getAsString();
                    programProductGroup.setName(programProductGroupName);
                }
                if (obPromoItemGroup.has("max_offer_per_promo"))
                    programProductGroup.setMaxOfferPerPromo(obPromoItemGroup.get("max_offer_per_promo").getAsInt());
                if (obPromoItemGroup.has("max_offer_per_agency"))
                    programProductGroup.setMaxOfferPerAgency(obPromoItemGroup.get("max_offer_per_agency").getAsInt());
                if (isCombo && obPromoItemGroup.has("combo_id")) {
                    programProductGroup.setComboId(obPromoItemGroup.get("combo_id").getAsInt());
                    program.getMpCombo().put(programProductGroup.getComboId(), programProductGroup);
                }
                mpProgramProductGroup.put(dataIndex, programProductGroup);
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
                    ProgramProductGroup initProgramProductGroup = mpProgramProductGroup.get(dataIndex);
                    if (initProgramProductGroup != null) {
                        programProductGroup.setCode(initProgramProductGroup.getCode());
                        if (program.getConditionType() != ProgramConditionType.ORDER_PRICE)
                            programProductGroup.setLtProgramProduct(initProgramProductGroup.getLtProgramProduct());
                        programProductGroup.setCombo(initProgramProductGroup.isCombo());
                        programProductGroup.getLtImage().addAll(initProgramProductGroup.getLtImage());
                        programProductGroup.setName(initProgramProductGroup.getName());
                        programProductGroup.setMaxOfferPerPromo(initProgramProductGroup.getMaxOfferPerPromo());
                        programProductGroup.setMaxOfferPerAgency(initProgramProductGroup.getMaxOfferPerAgency());
                        programProductGroup.setComboId(initProgramProductGroup.getComboId());
                    }
                    JsonObject obOffer = obProgramProductGroup.get("offer").getAsJsonObject();
                    programProductGroup.setOfferValue(obOffer.get("offer_value").getAsDouble());
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
                                Product product = this.getProductById(productId, "");
                                if (product == null) {
                                    return null;
                                }
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
                                Product product = this.getProductById(productId, "");
                                if (product == null) {
                                    return null;
                                }
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
                        case FIXED_PRICE:
                            FixedPriceOffer fixedPriceOffer = new FixedPriceOffer();
                            fixedPriceOffer.setOfferType(programOfferType);
                            fixedPriceOffer.setGoodsType(program.getGoodsType());
                            JsonArray arrFixedOfferProduct = obOffer.get("offer_products").getAsJsonArray();
                            for (JsonElement elOfferProduct : arrFixedOfferProduct) {
                                JsonObject obOfferProduct = elOfferProduct.getAsJsonObject();
                                int productId = obOfferProduct.get("product_id").getAsInt();
                                double price = obOfferProduct.get("offer_value").getAsDouble();
                                fixedPriceOffer.getMpProductPrice().put(productId, price);
                            }
                            fixedPriceOffer.setProgramProductGroup(programProductGroup);
                            programProductGroup.setOffer(fixedPriceOffer);
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
            if (object.has("repeat_data_info")) {
                String strRepeatDataInfo = object.get("repeat_data_info").getAsString();
                JsonObject obRepeatDataInfo = (JsonObject) JsonParser.parseString(strRepeatDataInfo);
                int repeatType = obRepeatDataInfo.get("type").getAsInt();
                program.setRepeatType(RepeatType.getByValue(repeatType));
                if (program.getRepeatType() != RepeatType.NONE) {
                    JsonArray arrRepeatData = obRepeatDataInfo.get("data").getAsJsonArray();
                    for (JsonElement elRepeatData : arrRepeatData)
                        program.getRepeatDataList().add(elRepeatData.getAsInt());
                    LocalTime startTime = LocalTime.parse(obRepeatDataInfo.get("time_from").getAsString());
                    LocalTime endTime = LocalTime.parse(obRepeatDataInfo.get("time_to").getAsString());
                    program.setStartTime(startTime);
                    program.setEndTime(endTime);
                }
            }
        } catch (Exception ex) {
            program = null;
            LogUtil.printDebug("", ex);
        }
        return program;
    }

    public void loadAllProgram(Map<Integer, Program> mpSalePolicy,
                               Map<Integer, String> mpPromoRunning,
                               Map<Integer, Program> mpPromotion,
                               Map<Integer, Program> mpHuntSale,
                               Map<Integer, Program> mpCTTL) {
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String sql = "SELECT * FROM promo_running";
            try (Statement stmt = con.createStatement()) {
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        String data = rs.getString("promo_data");
                        mpPromoRunning.put(rs.getInt("promo_id"), data);
                        Program program = this.importProgram(data);
                        if (program == null) {
                            continue;
                        }
                        if (program.getType() == ProgramType.SALE_POLICY)
                            mpSalePolicy.put(program.getId(), program);
                        else if (program.getType() == ProgramType.PROMOTION)
                            mpPromotion.put(program.getId(), program);
                        else if (program.getType() == ProgramType.CTSS)
                            mpHuntSale.put(program.getId(), program);
                        else if (program.getType() == ProgramType.CTTL)
                            mpCTTL.put(program.getId(), program);
                    }
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
    }

    public void loadOneProgram(Map<Integer, Program> mpSalePolicy,
                               Map<Integer, String> mpPromoRunning,
                               Map<Integer, Program> mpPromotion,
                               Map<Integer, Program> mpHuntSale,
                               Map<Integer, Program> mpCTTL,
                               int promo_id) {
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String sql = "SELECT * FROM promo_running WHERE promo_id=" + promo_id;
            try (Statement stmt = con.createStatement()) {
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    if (rs.next()) {
                        String data = rs.getString("promo_data");
                        mpPromoRunning.put(rs.getInt("promo_id"), data);
                        Program program = this.importProgram(data);
                        if (program == null) {
                            return;
                        }
                        if (program.getType() == ProgramType.SALE_POLICY)
                            mpSalePolicy.put(program.getId(), program);
                        else if (program.getType() == ProgramType.PROMOTION)
                            mpPromotion.put(program.getId(), program);
                        else if (program.getType() == ProgramType.CTSS)
                            mpHuntSale.put(program.getId(), program);
                        else if (program.getType() == ProgramType.CTTL)
                            mpCTTL.put(program.getId(), program);
                    }
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
    }

    public PromoBasicData getPromoCTSSData(int promo_id) {
        Program program = this.mpHuntSale.get(promo_id);
        if (program != null) {
            PromoBasicData promoBasicData = new PromoBasicData();
            promoBasicData.setId(program.getId());
            promoBasicData.setName(program.getName());
            promoBasicData.setCode(program.getCode());
            return promoBasicData;
        }
        return null;
    }

    public long getQuantitySoldProductForSanSale(int programId, int productId) {
        long totalQuantity = 0;
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String sql = "SELECT SUM(product_quantity) AS total FROM agency_promo_hunt_sale WHERE promo_id=? AND status=1 AND product_id=?";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setInt(1, programId);
                stmt.setInt(2, productId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next())
                        totalQuantity = rs.getLong("total");
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
        return totalQuantity;
    }

    public long getQuantitySoldProductForSanSale(int programId, int productId, int agencyId) {
        long totalQuantity = 0;
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String sql = "SELECT SUM(product_quantity) AS total FROM agency_promo_hunt_sale WHERE promo_id=? AND status=1 AND product_id=? AND agency_id=?";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setInt(1, programId);
                stmt.setInt(2, productId);
                stmt.setInt(3, agencyId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next())
                        totalQuantity = rs.getLong("total");
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
        return totalQuantity;
    }

    public Map<Integer, Integer> getQuantitySoldProductListForSanSale(int programId, List<Integer> ltProductId) {
        Map<Integer, Integer> mpProductQuantity = new LinkedHashMap<>();
        ManagerIF cm = null;
        Connection con = null;
        try {
            String param = ltProductId.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(",", "(", ")"));
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String sql = "SELECT product_id,SUM(product_quantity) AS quantity " +
                    "FROM agency_promo_hunt_sale " +
                    "WHERE promo_id=? AND status=1 AND product_id IN (?) " +
                    "GROUP BY product_id";
            sql = sql.replace("(?)", param);
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setInt(1, programId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int productId = rs.getInt("product_id");
                        int quantity = rs.getInt("quantity");
                        mpProductQuantity.put(productId, quantity);
                    }
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
        return mpProductQuantity;
    }

    public Program parseProgramFilterByPromoFilter(PromoApplyObjectRequest promoApplyObjectRequest) {
        try {
            Program program = new Program();
            for (AgencyBasicData agencyBasicData : promoApplyObjectRequest.getPromo_agency_ignores()) {
                program.getLtIgnoreAgencyId().add(agencyBasicData.getId());
            }

            for (AgencyBasicData agencyBasicData : promoApplyObjectRequest.getPromo_agency_includes()) {
                program.getLtIncludeAgencyId().add(agencyBasicData.getId());
            }
            if (promoApplyObjectRequest.getPromo_filters().size() > 0) {
                for (PromoApplyFilterRequest elPromoFilter : promoApplyObjectRequest.getPromo_filters()) {
                    if (elPromoFilter.getFilter_types().size() > 0) {
                        ProgramFilter programFilter = new ProgramFilter();
                        for (PromoApplyFilterDetailRequest obFilterType : elPromoFilter.getFilter_types()) {
                            ProgramFilterType programFilterType = ProgramFilterType.valueOf(obFilterType.getFilter_type());
                            switch (programFilterType) {
                                case CAP_BAC:
                                    String filterData = obFilterType.getFilter_data();
                                    JsonArray arrAgencyMembership = JsonParser.parseString(filterData).getAsJsonArray();
                                    for (JsonElement elAgencyMembership : arrAgencyMembership) {
                                        int membershipId = elAgencyMembership.getAsInt();
                                        programFilter.getLtAgencyMembershipId().add(membershipId);
                                    }
                                    break;
                                case PKD:
                                    filterData = obFilterType.getFilter_data();
                                    JsonArray arrAgencyDepartment = JsonParser.parseString(filterData).getAsJsonArray();
                                    for (JsonElement elAgencyDepartment : arrAgencyDepartment) {
                                        int departmentId = elAgencyDepartment.getAsInt();
                                        programFilter.getLtAgencyBusinessDepartmentId().add(departmentId);
                                    }
                                    break;
                                case TINH_THANH:
                                    filterData = obFilterType.getFilter_data();
                                    JsonArray arrAgencyCity = JsonParser.parseString(filterData).getAsJsonArray();
                                    for (JsonElement elAgencyCity : arrAgencyCity) {
                                        int cityId = elAgencyCity.getAsInt();
                                        programFilter.getLtAgencyCityId().add(cityId);
                                    }
                                    break;
                                case NGAY_SINH_NHAT:
                                    filterData = obFilterType.getFilter_data();
                                    programFilter.setBirthday(JsonParser.parseString(filterData).getAsBoolean());
                                    break;
                                case GIOI_TINH:
                                    filterData = obFilterType.getFilter_data();
                                    JsonArray arrGender = JsonParser.parseString(filterData).getAsJsonArray();
                                    for (JsonElement elGender : arrGender) {
                                        int gender = elGender.getAsInt();
                                        programFilter.getLtGender().add(gender);
                                    }
                                    break;
                                case KHOANG_CACH_DON_HANG_CUOI:
                                    filterData = obFilterType.getFilter_data();
                                    programFilter.setDiffOrderDay(JsonParser.parseString(filterData).getAsInt());
                                    break;
                                case NGUON_DON_HANG:
                                    filterData = obFilterType.getFilter_data();
                                    JsonArray arrSource = JsonParser.parseString(filterData).getAsJsonArray();
                                    for (JsonElement elSource : arrSource) {
                                        int source = elSource.getAsInt();
                                        programFilter.getLtSource().add(source);
                                    }
                                    break;
                                case CO_NO_XAU:
                                    filterData = obFilterType.getFilter_data();
                                    programFilter.setNx(JsonParser.parseString(filterData).getAsBoolean());
                                    break;
                                case GIA_TRI_NO_QUA_HAN:
                                    filterData = obFilterType.getFilter_data();
                                    JsonObject obFilterData = JsonParser.parseString(filterData).getAsJsonObject();
                                    if (obFilterData.has("from_value"))
                                        programFilter.setFromNqh(obFilterData.get("from_value").getAsLong());
                                    if (obFilterData.has("end_value"))
                                        programFilter.setEndNqh(obFilterData.get("end_value").getAsLong());
                                    break;
                                case CO_NO_QUA_HAN:
                                    filterData = obFilterType.getFilter_data();
                                    programFilter.setNqh(JsonParser.parseString(filterData).getAsBoolean());
                                    break;
                                case SO_LAN_SAI_CAM_KET:
                                    filterData = obFilterType.getFilter_data();
                                    programFilter.setSaiCamKet(JsonParser.parseString(filterData).getAsInt());
                                    break;
                                case CO_HAN_MUC_GOI_DAU:
                                    filterData = obFilterType.getFilter_data();
                                    boolean status = JsonParser.parseString(filterData).getAsBoolean();
                                    if (status)
                                        programFilter.setNgdLimitStatus(1);
                                    else
                                        programFilter.setNgdLimitStatus(0);
                                    break;
                                case KY_HAN_NO:
                                    filterData = obFilterType.getFilter_data();
                                    obFilterData = JsonParser.parseString(filterData).getAsJsonObject();
                                    if (obFilterData.has("from_value"))
                                        programFilter.setFromDeptCycle(obFilterData.get("from_value").getAsInt());
                                    if (obFilterData.has("end_value"))
                                        programFilter.setEndDeptCycle(obFilterData.get("end_value").getAsInt());
                                    break;
                                case HAN_MUC_NO:
                                    filterData = obFilterType.getFilter_data();
                                    obFilterData = JsonParser.parseString(filterData).getAsJsonObject();
                                    if (obFilterData.has("from_value"))
                                        programFilter.setFromDeptLimit(obFilterData.get("from_value").getAsLong());
                                    if (obFilterData.has("end_value"))
                                        programFilter.setEndDeptLimit(obFilterData.get("end_value").getAsLong());
                                    break;
                                case DOANH_THU_THUAN_TU_DEN:
                                    filterData = obFilterType.getFilter_data();
                                    obFilterData = JsonParser.parseString(filterData).getAsJsonObject();
                                    if (obFilterData.has("from_value"))
                                        programFilter.setFromDtt(obFilterData.get("from_value").getAsLong());
                                    if (obFilterData.has("end_value"))
                                        programFilter.setEndDtt(obFilterData.get("end_value").getAsLong());
                                    break;
                                case DOANH_SO_TU_DEN:
                                    filterData = obFilterType.getFilter_data();
                                    obFilterData = JsonParser.parseString(filterData).getAsJsonObject();
                                    if (obFilterData.has("from_value"))
                                        programFilter.setFromTotalPriceSales(obFilterData.get("from_value").getAsLong());
                                    if (obFilterData.has("end_value"))
                                        programFilter.setEndTotalPriceSales(obFilterData.get("end_value").getAsLong());
                                    break;
                                case CONG_NO_CUOI_KY_TU_DEN:
                                    filterData = obFilterType.getFilter_data();
                                    obFilterData = JsonParser.parseString(filterData).getAsJsonObject();
                                    if (obFilterData.has("from_value"))
                                        programFilter.setFromDeptCycleEnd(obFilterData.get("from_value").getAsLong());
                                    if (obFilterData.has("end_value"))
                                        programFilter.setEndDeptCycleEnd(obFilterData.get("end_value").getAsLong());
                                    break;
                                case TIEN_THU:
                                    filterData = obFilterType.getFilter_data();
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

            return program;
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return null;
    }

    public Program parseProgramFilterByPromoSufficientCondition(PromoApplyObjectRequest promoApplyObjectRequest) {
        try {
            Program program = new Program();
            for (AgencyBasicData agencyBasicData : promoApplyObjectRequest.getPromo_agency_ignores()) {
                program.getLtIgnoreAgencyId().add(agencyBasicData.getId());
            }

            for (AgencyBasicData agencyBasicData : promoApplyObjectRequest.getPromo_agency_includes()) {
                program.getLtIncludeAgencyId().add(agencyBasicData.getId());
            }
            if (promoApplyObjectRequest.getPromo_sufficient_conditions().size() > 0) {
                for (PromoApplyFilterRequest elPromoFilter : promoApplyObjectRequest.getPromo_sufficient_conditions()) {
                    if (elPromoFilter.getFilter_types().size() > 0) {
                        ProgramFilter programFilter = new ProgramFilter();
                        for (PromoApplyFilterDetailRequest obFilterType : elPromoFilter.getFilter_types()) {
                            ProgramFilterType programFilterType = ProgramFilterType.valueOf(obFilterType.getFilter_type());
                            switch (programFilterType) {
                                case CAP_BAC:
                                    String filterData = obFilterType.getFilter_data();
                                    JsonArray arrAgencyMembership = JsonParser.parseString(filterData).getAsJsonArray();
                                    for (JsonElement elAgencyMembership : arrAgencyMembership) {
                                        int membershipId = elAgencyMembership.getAsInt();
                                        programFilter.getLtAgencyMembershipId().add(membershipId);
                                    }
                                    break;
                                case PKD:
                                    filterData = obFilterType.getFilter_data();
                                    JsonArray arrAgencyDepartment = JsonParser.parseString(filterData).getAsJsonArray();
                                    for (JsonElement elAgencyDepartment : arrAgencyDepartment) {
                                        int departmentId = elAgencyDepartment.getAsInt();
                                        programFilter.getLtAgencyBusinessDepartmentId().add(departmentId);
                                    }
                                    break;
                                case TINH_THANH:
                                    filterData = obFilterType.getFilter_data();
                                    JsonArray arrAgencyCity = JsonParser.parseString(filterData).getAsJsonArray();
                                    for (JsonElement elAgencyCity : arrAgencyCity) {
                                        int cityId = elAgencyCity.getAsInt();
                                        programFilter.getLtAgencyCityId().add(cityId);
                                    }
                                    break;
                                case NGAY_SINH_NHAT:
                                    filterData = obFilterType.getFilter_data();
                                    programFilter.setBirthday(JsonParser.parseString(filterData).getAsBoolean());
                                    break;
                                case GIOI_TINH:
                                    filterData = obFilterType.getFilter_data();
                                    JsonArray arrGender = JsonParser.parseString(filterData).getAsJsonArray();
                                    for (JsonElement elGender : arrGender) {
                                        int gender = elGender.getAsInt();
                                        programFilter.getLtGender().add(gender);
                                    }
                                    break;
                                case KHOANG_CACH_DON_HANG_CUOI:
                                    filterData = obFilterType.getFilter_data();
                                    programFilter.setDiffOrderDay(JsonParser.parseString(filterData).getAsInt());
                                    break;
                                case NGUON_DON_HANG:
                                    filterData = obFilterType.getFilter_data();
                                    JsonArray arrSource = JsonParser.parseString(filterData).getAsJsonArray();
                                    for (JsonElement elSource : arrSource) {
                                        int source = elSource.getAsInt();
                                        programFilter.getLtSource().add(source);
                                    }
                                    break;
                                case CO_NO_XAU:
                                    filterData = obFilterType.getFilter_data();
                                    programFilter.setNx(JsonParser.parseString(filterData).getAsBoolean());
                                    break;
                                case GIA_TRI_NO_QUA_HAN:
                                    filterData = obFilterType.getFilter_data();
                                    JsonObject obFilterData = JsonParser.parseString(filterData).getAsJsonObject();
                                    if (obFilterData.has("from_value"))
                                        programFilter.setFromNqh(obFilterData.get("from_value").getAsLong());
                                    if (obFilterData.has("end_value"))
                                        programFilter.setEndNqh(obFilterData.get("end_value").getAsLong());
                                    break;
                                case CO_NO_QUA_HAN:
                                    filterData = obFilterType.getFilter_data();
                                    programFilter.setNqh(JsonParser.parseString(filterData).getAsBoolean());
                                    break;
                                case SO_LAN_SAI_CAM_KET:
                                    filterData = obFilterType.getFilter_data();
                                    programFilter.setSaiCamKet(JsonParser.parseString(filterData).getAsInt());
                                    break;
                                case CO_HAN_MUC_GOI_DAU:
                                    filterData = obFilterType.getFilter_data();
                                    boolean status = JsonParser.parseString(filterData).getAsBoolean();
                                    if (status)
                                        programFilter.setNgdLimitStatus(1);
                                    else
                                        programFilter.setNgdLimitStatus(0);
                                    break;
                                case KY_HAN_NO:
                                    filterData = obFilterType.getFilter_data();
                                    obFilterData = JsonParser.parseString(filterData).getAsJsonObject();
                                    if (obFilterData.has("from_value"))
                                        programFilter.setFromDeptCycle(obFilterData.get("from_value").getAsInt());
                                    if (obFilterData.has("end_value"))
                                        programFilter.setEndDeptCycle(obFilterData.get("end_value").getAsInt());
                                    break;
                                case HAN_MUC_NO:
                                    filterData = obFilterType.getFilter_data();
                                    obFilterData = JsonParser.parseString(filterData).getAsJsonObject();
                                    if (obFilterData.has("from_value"))
                                        programFilter.setFromDeptLimit(obFilterData.get("from_value").getAsLong());
                                    if (obFilterData.has("end_value"))
                                        programFilter.setEndDeptLimit(obFilterData.get("end_value").getAsLong());
                                    break;
                                case DOANH_THU_THUAN_TU_DEN:
                                    filterData = obFilterType.getFilter_data();
                                    obFilterData = JsonParser.parseString(filterData).getAsJsonObject();
                                    if (obFilterData.has("from_value"))
                                        programFilter.setFromDtt(obFilterData.get("from_value").getAsLong());
                                    if (obFilterData.has("end_value"))
                                        programFilter.setEndDtt(obFilterData.get("end_value").getAsLong());
                                    break;
                                case DOANH_SO_TU_DEN:
                                    filterData = obFilterType.getFilter_data();
                                    obFilterData = JsonParser.parseString(filterData).getAsJsonObject();
                                    if (obFilterData.has("from_value"))
                                        programFilter.setFromTotalPriceSales(obFilterData.get("from_value").getAsLong());
                                    if (obFilterData.has("end_value"))
                                        programFilter.setEndTotalPriceSales(obFilterData.get("end_value").getAsLong());
                                    break;
                                case CONG_NO_CUOI_KY_TU_DEN:
                                    filterData = obFilterType.getFilter_data();
                                    obFilterData = JsonParser.parseString(filterData).getAsJsonObject();
                                    if (obFilterData.has("from_value"))
                                        programFilter.setFromDeptCycleEnd(obFilterData.get("from_value").getAsLong());
                                    if (obFilterData.has("end_value"))
                                        programFilter.setEndDeptCycleEnd(obFilterData.get("end_value").getAsLong());
                                    break;
                                case TIEN_THU:
                                    filterData = obFilterType.getFilter_data();
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

            return program;
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return null;
    }

    public void loadDamMeRunning() {
        mpDamMe.clear();
        this.loadAllDamMeRunning();
    }

    public void loadCTXHRunning() {
        mpCTXH.clear();
        this.loadAllCTXHRunning();
    }

    public void loadAllDamMeRunning() {
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String sql = "SELECT * FROM dam_me_running";
            try (Statement stmt = con.createStatement()) {
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        String data = rs.getString("promo_data");
                        mpPromoRunning.put(rs.getInt("promo_id"), data);
                        Program program = this.importProgram(data);
                        if (program == null) {
                            continue;
                        }
                        mpDamMe.put(program.getId(), program);
                    }
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
    }

    public void loadAllCTXHRunning() {
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String sql = "SELECT * FROM bxh_running";
            try (Statement stmt = con.createStatement()) {
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        String data = rs.getString("promo_data");
                        mpPromoRunning.put(rs.getInt("promo_id"), data);
                        Program program = this.importProgram(data);
                        if (program == null) {
                            continue;
                        }
                        mpCTXH.put(program.getId(), program);
                    }
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
    }

    public void loadOneDamMe(int promo_id) {
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String sql = "SELECT * FROM dam_me_running WHERE promo_id=" + promo_id;
            try (Statement stmt = con.createStatement()) {
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    if (rs.next()) {
                        String data = rs.getString("promo_data");
                        mpPromoRunning.put(rs.getInt("promo_id"), data);
                        Program program = this.importProgram(data);
                        if (program == null) {
                            return;
                        }
                        mpDamMe.put(program.getId(), program);
                    }
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
    }

    public void loadOneCTXH(int promo_id) {
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String sql = "SELECT * FROM bxh_running WHERE promo_id=" + promo_id;
            try (Statement stmt = con.createStatement()) {
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    if (rs.next()) {
                        String data = rs.getString("promo_data");
                        mpPromoRunning.put(rs.getInt("promo_id"), data);
                        Program program = this.importProgram(data);
                        if (program == null) {
                            return;
                        }
                        mpCTXH.put(program.getId(), program);
                    }
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
    }

    public Map<Integer, TLProductReward> getDMProductReward(int agencyId, List<Integer> ltProgramId, List<Integer> ltProductId) {
        Map<Integer, TLProductReward> mpProductReward = new LinkedHashMap<>();
        ManagerIF cm = null;
        Connection con = null;
        try {
            String programParam = ltProgramId.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(",", "(", ")"));
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String sql = "SELECT program_id,data,value FROM agency_csdm_product_offer WHERE agency_id=? AND program_id IN (1)";
            sql = sql.replace("(1)", programParam);
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setInt(1, agencyId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int programId = rs.getInt("program_id");
                        String data = rs.getString("data");
                        int value = rs.getInt("value");
                        if (StringUtils.isNotBlank(data) && value > 0) {
                            List<Integer> ltId = JSONUtil.DeSerialize(data, new TypeToken<List<Integer>>() {
                            }.getType());
                            for (int productId : ltId) {
                                if (ltProductId.contains(productId)) {
                                    Program program = new Program();
                                    program.setId(programId);
                                    TLProductReward tlProductReward = new TLProductReward();
                                    tlProductReward.setProductId(productId);
                                    tlProductReward.setProgram(program);
                                    tlProductReward.setOfferPercent(value);
                                    if (!mpProductReward.containsKey(tlProductReward.getProductId()))
                                        mpProductReward.put(tlProductReward.getProductId(), tlProductReward);
                                    else if (value > mpProductReward.get(tlProductReward.getProductId()).getOfferPercent())
                                        mpProductReward.put(tlProductReward.getProductId(), tlProductReward);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
        return mpProductReward;
    }

    public Map<Integer, Integer> getDMProgramReward(int agencyId, List<Integer> ltProgramId) {
        Map<Integer, Integer> mpData = new ConcurrentHashMap<>();
        ManagerIF cm = null;
        Connection con = null;
        try {
            String param = ltProgramId.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(",", "(", ")"));
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String sql = "SELECT program_id,value FROM agency_csdm_product_offer WHERE agency_id=? AND program_id IN (1) AND value>0";
            sql = sql.replace("(1)", param);
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setInt(1, agencyId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int programId = rs.getInt("program_id");
                        int value = rs.getInt("value");
                        mpData.put(programId, value);
                    }
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
        return mpData;
    }

    public List<CTXHAgencyResult> getAllAgencyReadyJoinCTXH() {
        return this.programDB.getAllAgencyReadyJoinCTXH();
    }

    public void parseFilter(Program program, List<PromoApplyFilterRequest> promoFilters) {
        try {
            JsonArray arrPromoFilter = JsonUtils.DeSerialize(
                    JsonUtils.Serialize(promoFilters), JsonArray.class);
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
                            case DOANH_THU_THUAN_NAM_TRUOC:
                                filterData = obFilterType.get("filter_data").getAsString();
                                obFilterData = JsonParser.parseString(filterData).getAsJsonObject();
                                if (obFilterData.has("from_value"))
                                    programFilter.setFromDtt(obFilterData.get("from_value").getAsLong());
                                if (obFilterData.has("end_value"))
                                    programFilter.setEndDtt(obFilterData.get("end_value").getAsLong());
                                break;
                        }
                    }
                    program.getLtProgramFilter().add(programFilter);
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
    }

    public List<Agency> getListAgencyReadyJoinMission() {
        return this.programDB.getListAgencyReadyJoinMission();
    }
}