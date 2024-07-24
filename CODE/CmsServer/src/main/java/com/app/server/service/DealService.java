package com.app.server.service;

import com.app.server.config.ConfigInfo;
import com.app.server.constants.ResponseMessage;
import com.app.server.data.SessionData;
import com.app.server.data.dto.agency.AgencyBasicData;
import com.app.server.data.dto.product.ProductCache;
import com.app.server.data.dto.product.ProductPrice;
import com.app.server.data.dto.program.DeptInfo;
import com.app.server.data.dto.program.Program;
import com.app.server.data.dto.program.Source;
import com.app.server.data.dto.program.agency.Agency;
import com.app.server.data.dto.promo.*;
import com.app.server.data.entity.AgencyDealPriceRoundEntity;
import com.app.server.data.entity.AgencyEntity;
import com.app.server.data.entity.PromoApplyObjectEntity;
import com.app.server.data.request.ApplyFilterRequest;
import com.app.server.data.request.BasicRequest;
import com.app.server.data.request.CancelRequest;
import com.app.server.data.request.FilterListRequest;
import com.app.server.data.request.deal.CreateDealPriceSettingRequest;
import com.app.server.data.request.deal.ResponseDealPriceRequest;
import com.app.server.data.request.promo.PromoApplyFilterDetailRequest;
import com.app.server.data.request.promo.PromoApplyFilterRequest;
import com.app.server.data.request.promo.PromoApplyObjectRequest;
import com.app.server.enums.*;
import com.app.server.response.ClientResponse;
import com.app.server.utils.JsonUtils;
import com.google.gson.reflect.TypeToken;
import com.ygame.framework.common.Config;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class DealService extends ProgramService {
    public ClientResponse filterDealPrice(SessionData sessionData, FilterListRequest request) {
        try {
            this.addFilterAgencyData(sessionData, request);
            String query = this.filterUtils.getQuery(FunctionList.LIST_DEAL_PRICE, request.getFilters(), request.getSorts());
            List<JSONObject> records = this.dealDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE,
                    request.getIsLimit());
            List<JSONObject> dealList = new ArrayList<>();
            for (JSONObject deal : records) {
                deal.put("agency_info", this.dataManager.getAgencyManager().getAgencyBasicData(
                        ConvertUtils.toInt(deal.get("agency_id"))));
                if (ConvertUtils.toInt(deal.get("is_new")) != YesNoStatus.YES.getValue()) {
                    deal.put("product_info",
                            this.dataManager.getProductManager().getProductBasicData(
                                    ConvertUtils.toInt(deal.get("product_id"))));
                } else {
                    ProductCache productCache = new ProductCache();
                    productCache.setId(ConvertUtils.toInt(deal.get("product_id")));
                    productCache.setCode(ConvertUtils.toString(deal.get(("product_code"))));
                    productCache.setFull_name(ConvertUtils.toString(deal.get("product_full_name")));
                    productCache.setImages(ConvertUtils.toString(deal.get("product_images")));
                    productCache.setProduct_small_unit_id(ConvertUtils.toInt(deal.get("product_small_unit_id")));
                    deal.put("product_info", productCache);
                }

                if (ConvertUtils.toInt(deal.get("status")) == DealPriceStatus.WAITING_APP.getId() ||
                        ConvertUtils.toInt(deal.get("status")) == DealPriceStatus.WAITING_CMS.getId()) {
                    if (ConvertUtils.toInt(deal.get("is_new")) != YesNoStatus.YES.getValue()) {
                        int visibility = this.getProductVisibilityByAgency(
                                ConvertUtils.toInt(deal.get("agency_id")),
                                ConvertUtils.toInt(deal.get("product_id"))
                        );
                        if (visibility == VisibilityType.HIDE.getId()) {
                            this.dealDB.hideDealPrice(ConvertUtils.toInt(deal.get("id")));
                            continue;
                        }
                    }

                    JSONObject agencyInfo = this.agencyDB.getAgencyInfoById(
                            ConvertUtils.toInt(deal.get("agency_id"))
                    );
                    if (agencyInfo == null) {
                        continue;
                    }
                    ProductCache productPrice = this.getFinalPriceByAgency(
                            ConvertUtils.toInt(deal.get("product_id")),
                            ConvertUtils.toInt(deal.get("agency_id")),
                            ConvertUtils.toInt(agencyInfo.get("city_id")),
                            ConvertUtils.toInt(agencyInfo.get("region_id")),
                            ConvertUtils.toInt(agencyInfo.get("membership_id"))
                    );
                    deal.put("product_price", productPrice == null ? 0 : productPrice.getPrice());
                } else {
                    ProductCache productCache = new ProductCache();
                    productCache.setId(ConvertUtils.toInt(deal.get("product_id")));
                    productCache.setCode(ConvertUtils.toString(deal.get(("product_code"))));
                    productCache.setFull_name(ConvertUtils.toString(deal.get("product_full_name")));
                    productCache.setImages(ConvertUtils.toString(deal.get("product_images")));
                    productCache.setProduct_small_unit_id(ConvertUtils.toInt(deal.get("product_small_unit_id")));
                    deal.put("product_info", productCache);
                }

                deal.put("promos", this.getListPromoByDealPrice(deal));

                dealList.add(deal);
            }
            int total = this.deptDB.getTotal(query);
            JSONObject data = new JSONObject();
            data.put("records", dealList);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }

        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private List<JSONObject> getListPromoByDealPrice(JSONObject deal) {
        int status = ConvertUtils.toInt(deal.get("status"));
        if (status != DealPriceStatus.CANCEL.getId() &&
                status != DealPriceStatus.CONFIRMED.getId()) {
            List<JSONObject> promoBasicDataList = new ArrayList<>();
            Agency agency = this.dataManager.getProgramManager().getAgency(
                    ConvertUtils.toInt(deal.get("agency_id")));
            if (agency == null) {
                return promoBasicDataList;
            }
            int product_id = ConvertUtils.toInt(deal.get("product_id"));
            if (agency != null) {
                DeptInfo deptInfo = this.dataManager.getProgramManager().getDeptInfo(agency.getId());
                List<PromoBasicData> promos = this.dataManager.getProgramManager().getPromoByProduct(
                        product_id);
                for (PromoBasicData promoBasicData : promos) {
                    Program programCSBH = this.dataManager.getProgramManager().getMpSalePolicy().get(promoBasicData.getId());
                    Program programCTKM = this.dataManager.getProgramManager().getMpPromotion().get(promoBasicData.getId());
                    //LogUtil.printDebug(JsonUtils.Serialize(program));
                    if (programCSBH != null && programCSBH.isRunning()) {
                        if (this.checkProgramFilter(
                                agency,
                                programCSBH,
                                Source.WEB,
                                deptInfo)) {
                            promoBasicData.setDescription(
                                    this.getCmsProgramDescriptionForProduct(
                                            agency,
                                            product_id,
                                            programCSBH));
                            promoBasicDataList.add(
                                    JsonUtils.DeSerialize(JsonUtils.Serialize(promoBasicData), JSONObject.class)
                            );
                        }
                    } else if (programCTKM != null && programCTKM.isRunning()) {
                        if (this.checkProgramFilter(
                                agency, programCTKM, Source.WEB, deptInfo)) {
                            promoBasicData.setDescription(
                                    this.getCmsProgramDescriptionForProduct(
                                            agency,
                                            product_id,
                                            programCTKM));
                            promoBasicDataList.add(
                                    JsonUtils.DeSerialize(JsonUtils.Serialize(promoBasicData), JSONObject.class)
                            );
                        }
                    }
                }
                return promoBasicDataList;
            }

            return promoBasicDataList;
        } else {

            /**
             * Chính sách cho sản phẩm
             */
            List<JSONObject> promoProductList = new ArrayList<>();
            if (deal.get("promo_product_info") != null &&
                    !deal.get("promo_product_info").toString().isEmpty()
            ) {
                List<PromoProductInfoData> promoProductInfoDataList = this.convertPromoProductToList(
                        deal.get("promo_product_info").toString()
                );

                for (PromoProductInfoData promoProductInfoData : promoProductInfoDataList) {
                    for (PromoOrderData promoOrderData : promoProductInfoData.getPromo()) {
                        PromoProductBasicData promoProductBasicData = new PromoProductBasicData();
                        promoProductBasicData.setProduct_id(promoProductInfoData.getProduct_id());
                        promoProductBasicData.setId(promoOrderData.getPromo_id());
                        promoProductBasicData.setName(promoOrderData.getPromo_name());
                        promoProductBasicData.setCode(promoOrderData.getPromo_code());
                        promoProductBasicData.setDescription(promoOrderData.getPromo_description());
                        promoProductList.add(
                                JsonUtils.DeSerialize(JsonUtils.Serialize(promoProductBasicData),
                                        JSONObject.class)
                        );
                    }
                }
            }

            if (deal.get("promo_product_info_ctkm") != null &&
                    !deal.get("promo_product_info_ctkm").toString().isEmpty()
            ) {
                List<PromoProductInfoData> promoProductInfoDataList = this.convertPromoProductToList(
                        deal.get("promo_product_info_ctkm").toString());

                for (PromoProductInfoData promoProductInfoData : promoProductInfoDataList) {
                    for (PromoOrderData promoOrderData : promoProductInfoData.getPromo()) {
                        PromoProductBasicData promoProductCtkm = new PromoProductBasicData();
                        promoProductCtkm.setProduct_id(promoProductInfoData.getProduct_id());
                        promoProductCtkm.setId(promoOrderData.getPromo_id());
                        promoProductCtkm.setName(promoOrderData.getPromo_name());
                        promoProductCtkm.setCode(promoOrderData.getPromo_code());
                        promoProductCtkm.setDescription(promoOrderData.getPromo_description());
                        promoProductList.add(
                                JsonUtils.DeSerialize(JsonUtils.Serialize(promoProductCtkm),
                                        JSONObject.class)
                        );
                    }
                }
            }

            return promoProductList;
        }
    }

    public ClientResponse createDealPriceSetting(SessionData sessionData, CreateDealPriceSettingRequest request) {
        try {
            JSONObject setting = this.dealDB.getDealPriceSetting();
            if (setting == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            /**
             * Đại lý chỉ định không trùng với đại lý loại trừ
             */
            for (int iDaiLyChiDinh = 0; iDaiLyChiDinh < request.getAgency_includes().size(); iDaiLyChiDinh++) {
                for (int iDaiLyLoaiTru = 0; iDaiLyLoaiTru < request.getAgency_ignores().size(); iDaiLyLoaiTru++) {
                    if (request.getAgency_includes().get(iDaiLyChiDinh).getId()
                            == request.getAgency_ignores().get(iDaiLyLoaiTru).getId()) {
                        ClientResponse clientResponse = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_INCLUDE_CONTAIN_AGENCY_IGNORE);
                        clientResponse.setMessage("[Đại lý chỉ định thứ " + (iDaiLyChiDinh + 1) + "]" + clientResponse.getMessage());
                        return clientResponse;
                    }
                }
            }

            boolean rsUpdate = this.dealDB.updateDealPriceSetting(
                    JsonUtils.Serialize(request.getAgency_includes()),
                    JsonUtils.Serialize(request.getAgency_ignores()),
                    JsonUtils.Serialize(request.getFilters())
            );
            if (!rsUpdate) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            /**
             * Call reload cache APP
             */
            this.dataManager.getProductManager().callReloadCacheDealPrice();

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }

        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse getDealPriceSetting(SessionData sessionData) {
        try {
            JSONObject setting = this.dealDB.getDealPriceSetting();
            if (setting == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            CreateDealPriceSettingRequest applyObjectRequest = new CreateDealPriceSettingRequest();

            applyObjectRequest.setAgency_includes(
                    JsonUtils.DeSerialize(
                            setting.get("agency_include").toString(),
                            new TypeToken<List<AgencyBasicData>>() {
                            }.getType()));
            for (AgencyBasicData agencyBasicData : applyObjectRequest.getAgency_includes()) {
                AgencyEntity agencyEntity = this.dataManager.getAgencyManager().getAgency(agencyBasicData.getId());
                if (agencyEntity != null) {
                    agencyBasicData.initInfo(agencyEntity);
                }
            }

            applyObjectRequest.setAgency_ignores(
                    JsonUtils.DeSerialize(
                            setting.get("agency_ignore").toString()
                            , new TypeToken<List<AgencyBasicData>>() {
                            }.getType()));
            for (AgencyBasicData agencyBasicData : applyObjectRequest.getAgency_ignores()) {
                AgencyEntity agencyEntity = this.dataManager.getAgencyManager().getAgency(agencyBasicData.getId());
                if (agencyEntity != null) {
                    agencyBasicData.initInfo(agencyEntity);
                }
            }

            applyObjectRequest.setFilters(
                    JsonUtils.DeSerialize(
                            setting.get("filters").toString()
                            , new TypeToken<List<ApplyFilterRequest>>() {
                            }.getType())
            );

            JSONObject data = new JSONObject();
            data.put("setting", applyObjectRequest);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }

        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse confirmDealPrice(SessionData sessionData, BasicRequest request) {
        try {
            JSONObject deal = this.dealDB.getDealPrice(request.getId());
            if (deal == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            if (ConvertUtils.toInt(deal.get("status")) != DealPriceStatus.WAITING_CMS.getId()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            int round = ConvertUtils.toInt(deal.get("round"));

            JSONObject roundDetail = this.dealDB.getDealPriceRoundDetail(request.getId(), round);
            if (roundDetail == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            AgencyDealPriceRoundEntity agencyDealPriceRoundEntity =
                    AgencyDealPriceRoundEntity.from(roundDetail);

            boolean rsResponseDeal = this.dealDB.saveResponseDeal(
                    request.getId(),
                    round,
                    agencyDealPriceRoundEntity.getProduct_price_app(),
                    agencyDealPriceRoundEntity.getProduct_total_quantity_app(),
                    agencyDealPriceRoundEntity.getDeposit_percent_app(),
                    agencyDealPriceRoundEntity.getPayment_duration_app(),
                    agencyDealPriceRoundEntity.getComplete_payment_duration_app(),
                    agencyDealPriceRoundEntity.getRequest_delivery_date_app(),
                    ConvertUtils.toString(deal.get("note"))
            );
            if (!rsResponseDeal) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            long total_end_price = agencyDealPriceRoundEntity.getProduct_price_app() * agencyDealPriceRoundEntity.getProduct_total_quantity_app();
            long deposit_money = total_end_price * agencyDealPriceRoundEntity.getDeposit_percent_app() / 100;
            long remain_payment_money = total_end_price - deposit_money;

            Date confirmed_date = DateTimeUtils.getNow();
            Date payment_date = this.appUtils.getDateAfterDay(
                    confirmed_date,
                    agencyDealPriceRoundEntity.getPayment_duration_app()
            );

            Date complete_payment_date = this.appUtils.getDateAfterDay(
                    confirmed_date,
                    agencyDealPriceRoundEntity.getComplete_payment_duration_app()
            );

            boolean rsUpdateDealPrice = this.dealDB.confirmDealPrice(
                    request.getId(),
                    DealPriceStatus.CONFIRMED.getId(),
                    agencyDealPriceRoundEntity.getProduct_price_app(),
                    agencyDealPriceRoundEntity.getProduct_total_quantity_app(),
                    agencyDealPriceRoundEntity.getDeposit_percent_app(),
                    agencyDealPriceRoundEntity.getPayment_duration_app(),
                    agencyDealPriceRoundEntity.getComplete_payment_duration_app(),
                    total_end_price,
                    deposit_money,
                    remain_payment_money,
                    agencyDealPriceRoundEntity.getRequest_delivery_date_app(),
                    confirmed_date,
                    payment_date,
                    complete_payment_date
            );
            if (!rsUpdateDealPrice) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(
                    ConvertUtils.toInt(deal.get("product_id"))
            );
            if (productCache != null) {
                JSONObject agencyInfo = this.dataManager.getAgencyManager().getAgencyInfo(
                        ConvertUtils.toInt(deal.get("agency_id"))
                );
                if (agencyInfo == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                ProductCache productPrice = this.getFinalPriceByAgency(
                        ConvertUtils.toInt(deal.get("product_id")),
                        ConvertUtils.toInt(deal.get("agency_id")),
                        ConvertUtils.toInt(agencyInfo.get("city_id")),
                        ConvertUtils.toInt(agencyInfo.get("region_id")),
                        ConvertUtils.toInt(agencyInfo.get("membership_id"))
                );

                this.dealDB.saveProductInfoToDealPrice(
                        request.getId(),
                        ConvertUtils.toLong(productPrice == null ? 0 : productPrice.getPrice()),
                        productCache.getFull_name(),
                        productCache.getImages(),
                        productCache.getProduct_small_unit_id(),
                        productCache.getCode()
                );
            }

            if (ConvertUtils.toInt(deal.get("is_new")) != YesNoStatus.YES.getValue()) {
                this.savePromoIntoDealPrice(
                        request.getId(),
                        ConvertUtils.toInt(deal.get("agency_id")),
                        ConvertUtils.toInt(deal.get("product_id")));
            }
            /**
             * Thông báo cho đại lý
             */
            this.pushNotifyToAgency(
                    0,
                    NotifyAutoContentType.THACH_GIA_CHI_TIET,
                    "",
                    NotifyAutoContentType.THACH_GIA_CHI_TIET.getType(),
                    JsonUtils.Serialize(
                            Arrays.asList(ConvertUtils.toString(request.getId()))
                    ),
                    "Thách giá " + ConvertUtils.toString(deal.get("code")) + " đã được Anh Tin xác nhận. Quý khách vui lòng kiểm tra.",
                    ConvertUtils.toInt(deal.get("agency_id"))
            );

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private void savePromoIntoDealPrice(int id, int agency_id, int product_id) {
        try {
            /**
             * Chính sách của sản phẩm
             */
            List<PromoBasicData> promoCSBHList = new ArrayList<>();
            List<PromoBasicData> promoCTKMList = new ArrayList<>();
            List<PromoBasicData> promos = this.dataManager.getProgramManager().getPromoByProduct(
                    product_id);
            Agency agency = this.dataManager.getProgramManager().getAgency(
                    agency_id);
            if (agency == null) {
                return;
            }
            DeptInfo deptInfo = this.dataManager.getProgramManager().getDeptInfo(agency.getId());

            for (PromoBasicData promoBasicData : promos) {
                //LogUtil.printDebug(JsonUtils.Serialize(program));
                if (promoBasicData.getPromo_type().equals(PromoType.SALE_POLICY.getKey())) {
                    Program program = this.dataManager.getProgramManager().getMpSalePolicy().get(promoBasicData.getId());
                    if (program.isRunning() && this.checkProgramFilter(
                            agency,
                            program,
                            Source.APP,
                            deptInfo)) {
                        promoBasicData.setDescription(
                                this.getCmsProgramDescriptionForProduct(
                                        agency,
                                        product_id,
                                        program));
                        promoCSBHList.add(promoBasicData);
                    }
                } else {
                    Program program = this.dataManager.getProgramManager().getMpPromotion().get(promoBasicData.getId());
                    if (program.isRunning() && this.checkProgramFilter(
                            agency, program, Source.APP, deptInfo)) {
                        promoBasicData.setDescription(
                                this.getCmsProgramDescriptionForProduct(
                                        agency,
                                        product_id,
                                        program));
                        promoCTKMList.add(promoBasicData);
                    }
                }
            }

            List<PromoProductInfoData> promoCSBHProducts = new ArrayList<>();
            if (promoCSBHList.size() > 0) {
                PromoProductInfoData promoProductInfoData = new PromoProductInfoData();
                promoProductInfoData.setProduct_id(product_id);
                for (PromoBasicData program : promoCSBHList) {
                    PromoOrderData promoOrderData = new PromoOrderData();
                    promoOrderData.setPromo_id(program.getId());
                    promoOrderData.setPromo_name(program.getName());
                    promoOrderData.setPromo_code(program.getCode());
                    promoOrderData.setPromo_description(program.getDescription());
                    promoProductInfoData.getPromo().add(promoOrderData);
                }
                promoCSBHProducts.add(promoProductInfoData);
            }

            List<PromoProductInfoData> promoCTKMProducts = new ArrayList<>();
            if (promoCTKMList.size() > 0) {
                PromoProductInfoData promoCTKMProductInfoData = new PromoProductInfoData();
                promoCTKMProductInfoData.setProduct_id(product_id);
                for (PromoBasicData program : promoCTKMList) {
                    PromoOrderData promoOrderData = new PromoOrderData();
                    promoOrderData.setPromo_id(program.getId());
                    promoOrderData.setPromo_name(program.getName());
                    promoOrderData.setPromo_code(program.getCode());
                    promoOrderData.setPromo_description(program.getDescription());
                    promoCTKMProductInfoData.getPromo().add(promoOrderData);
                }
                promoCTKMProducts.add(promoCTKMProductInfoData);
            }

            this.dealDB.updatePromo(
                    id,
                    this.convertPromoProductToString(promoCSBHProducts),
                    this.convertPromoProductToString(promoCTKMProducts)
            );
        } catch (Exception ex) {
            LogUtil.printDebug(Module.DEAL.name(), ex);
        }
    }

    public ClientResponse responseDealPrice(SessionData sessionData, ResponseDealPriceRequest request) {
        try {
            JSONObject deal = this.dealDB.getDealPrice(request.getId());
            if (deal == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            if (ConvertUtils.toInt(deal.get("status")) != DealPriceStatus.WAITING_CMS.getId()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            int round = ConvertUtils.toInt(deal.get("round"));

            JSONObject roundDetail = this.dealDB.getDealPriceRoundDetail(request.getId(), round);
            if (roundDetail == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            if (ConvertUtils.toInt(deal.get("is_new")) == YesNoStatus.YES.getValue()) {
                boolean rsSaveDealProductNew = this.dealDB.saveDealProductNew(
                        request.getId(),
                        request.getProduct_full_name(),
                        request.getProduct_description(),
                        JsonUtils.Serialize(request.getProduct_images())
                );
            }

            boolean rsSaveResponseDeal = this.dealDB.saveResponseDeal(
                    request.getId(),
                    round,
                    request.getProduct_price(),
                    request.getProduct_total_quantity(),
                    request.getDeposit_percent(),
                    request.getPayment_duration(),
                    request.getComplete_payment_duration(),
                    request.getRequest_delivery_date_millisecond() == null ? null :
                            DateTimeUtils.getDateTime(request.getRequest_delivery_date_millisecond()),
                    request.getNote()
            );
            if (!rsSaveResponseDeal) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            long total_end_price = request.getProduct_price() * request.getProduct_total_quantity();
            long deposit_money = total_end_price * request.getDeposit_percent() / 100;
            long remain_payment_money = total_end_price - deposit_money;

            Date confirmed_date = DateTimeUtils.getNow();
            Date payment_date = this.appUtils.getDateAfterDay(
                    confirmed_date,
                    request.getPayment_duration()
            );

            Date complete_payment_date = this.appUtils.getDateAfterDay(
                    confirmed_date,
                    request.getComplete_payment_duration()
            );

            boolean rsUpdateDealPrice = this.dealDB.updateDealPrice(
                    request.getId(),
                    DealPriceStatus.WAITING_APP.getId(),
                    request.getProduct_price(),
                    request.getProduct_total_quantity(),
                    request.getDeposit_percent(),
                    request.getPayment_duration(),
                    request.getComplete_payment_duration(),
                    total_end_price,
                    deposit_money,
                    remain_payment_money,
                    DateTimeUtils.getDateTime(request.getRequest_delivery_date_millisecond()),
                    confirmed_date,
                    payment_date,
                    complete_payment_date,
                    request.getNote()
            );
            if (!rsUpdateDealPrice) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            /**
             * Thông báo cho đại lý
             */
            this.pushNotifyToAgency(
                    0,
                    NotifyAutoContentType.THACH_GIA_CHI_TIET,
                    "",
                    NotifyAutoContentType.THACH_GIA_CHI_TIET.getType(),
                    JsonUtils.Serialize(
                            Arrays.asList(ConvertUtils.toString(request.getId()))
                    ),
                    "Thách giá " + ConvertUtils.toString(deal.get("code")) + " đã được Anh Tin phản hồi. Quý khách vui lòng kiểm tra và xác nhận.",
                    ConvertUtils.toInt(deal.get("agency_id"))
            );

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse getDealPriceDetail(SessionData sessionData, BasicRequest request) {
        try {
            JSONObject deal = this.dealDB.getDealPrice(request.getId());
            if (deal == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            if (!this.dataManager.getStaffManager().checkStaffManageAgency(
                    sessionData.getId(),
                    this.agencyDB.getAgencyInfoById(
                            ConvertUtils.toInt(deal.get("agency_id"))
                    )
            )) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.USER_FORBIDDEN);
            }

            if (ConvertUtils.toInt(deal.get("is_new")) == YesNoStatus.YES.getValue()) {
                JSONObject productInfo = new JSONObject();
                productInfo.put("full_name", deal.get("product_full_name"));
                productInfo.put("images", deal.get("product_images"));
                productInfo.put("description", deal.get("product_description"));
                productInfo.put("product_price", deal.get("product_price"));
                deal.put("product_info", productInfo);
            } else {
                ProductCache product = this.dataManager.getProductManager().getProductBasicData(
                        ConvertUtils.toInt(deal.get("product_id"))
                );
                deal.put("product_info", product);
            }

            if (ConvertUtils.toInt(deal.get("status")) == DealPriceStatus.WAITING_APP.getId() ||
                    ConvertUtils.toInt(deal.get("status")) == DealPriceStatus.WAITING_CMS.getId()) {
                if (ConvertUtils.toInt(deal.get("is_new")) != YesNoStatus.YES.getValue()) {
                    int visibility = this.getProductVisibilityByAgency(
                            ConvertUtils.toInt(deal.get("agency_id")),
                            ConvertUtils.toInt(deal.get("product_id"))
                    );
                    if (visibility == VisibilityType.HIDE.getId()) {
                        this.dealDB.hideDealPrice(ConvertUtils.toInt(deal.get("id")));
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.DEAL_PRICE_NOT_FOUND);
                    }

                    JSONObject agencyInfo = this.agencyDB.getAgencyInfoById(
                            ConvertUtils.toInt(deal.get("agency_id"))
                    );
                    if (agencyInfo == null) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
                    }
                    ProductCache productPrice = this.getFinalPriceByAgency(
                            ConvertUtils.toInt(deal.get("product_id")),
                            ConvertUtils.toInt(deal.get("agency_id")),
                            ConvertUtils.toInt(agencyInfo.get("city_id")),
                            ConvertUtils.toInt(agencyInfo.get("region_id")),
                            ConvertUtils.toInt(agencyInfo.get("membership_id"))
                    );
                    deal.put("product_price", productPrice == null ? 0 : productPrice.getPrice());
                } else {
                    deal.put("product_price", 0);
                }
            } else {
                JSONObject productCache = new JSONObject();
                productCache.put("id", ConvertUtils.toInt(deal.get("product_id")));
                productCache.put("code", ConvertUtils.toString(deal.get(("product_code"))));
                productCache.put("full_name", ConvertUtils.toString(deal.get(("product_full_name"))));
                productCache.put("images", ConvertUtils.toString(deal.get("product_images")));
                productCache.put("description", ConvertUtils.toString(deal.get("product_description")));
                productCache.put("product_small_unit_id", ConvertUtils.toInt(deal.get("product_small_unit_id")));
                deal.put("product_info", productCache);
            }


            deal.put("agency_info", this.dataManager.getAgencyManager().getAgencyBasicData(
                    ConvertUtils.toInt(deal.get("agency_id"))
            ));
            JSONObject deal_price_detail = new JSONObject();
            JSONArray price_array = new JSONArray();
            JSONArray quantity_array = new JSONArray();
            JSONArray deposit_percent_array = new JSONArray();
            JSONArray payment_duration_array = new JSONArray();
            JSONArray complete_payment_duration_array = new JSONArray();
            JSONArray request_delivery_date_array = new JSONArray();
            JSONArray total_end_price_array = new JSONArray();
            JSONArray deposit_money_array = new JSONArray();
            JSONArray remain_payment_money_array = new JSONArray();
            JSONArray note_array = new JSONArray();
            JSONArray updated_date_array = new JSONArray();

            JSONObject prev = null;
            List<JSONObject> rounds = this.dealDB.getListRound(request.getId());
            for (int iRound = 0; iRound < rounds.size(); iRound++) {
                JSONObject deal_detail_round = rounds.get(iRound);
                price_array.add(
                        this.convertPriceInfoData(deal_detail_round, prev));
                quantity_array.add(
                        this.convertQuantityInfoData(deal_detail_round, prev));
                deposit_percent_array.add(
                        this.convertDepositPercentInfoData(deal_detail_round, prev));
                payment_duration_array.add(
                        this.convertPaymentDurationInfoData(deal_detail_round, prev));
                complete_payment_duration_array.add(
                        this.convertCompletePaymentDurationInfoData(deal_detail_round, prev));
                request_delivery_date_array.add(
                        this.convertRequestDeliveryDateInfoData(deal_detail_round, prev));
                total_end_price_array.add(
                        this.convertTotalEndPriceInfoData(deal_detail_round, prev));
                deposit_money_array.add(
                        this.convertDepositMoneyInfoData(deal_detail_round, prev));
                remain_payment_money_array.add(
                        this.convertRemainPaymentMoneyInfoData(deal_detail_round, prev));
                note_array.add(
                        this.convertNoteInfoData(deal_detail_round, prev));
                updated_date_array.add(
                        this.convertUpdatedDateInfoData(deal_detail_round, prev));

                prev = deal_detail_round;
            }

            deal_price_detail.put("price_array", price_array);
            deal_price_detail.put("quantity_array", quantity_array);
            deal_price_detail.put("deposit_percent_array", deposit_percent_array);
            deal_price_detail.put("payment_duration_array", payment_duration_array);
            deal_price_detail.put("complete_payment_duration_array", complete_payment_duration_array);
            deal_price_detail.put("request_delivery_date_array", request_delivery_date_array);
            deal_price_detail.put("total_end_price_array", total_end_price_array);
            deal_price_detail.put("deposit_money_array", deposit_money_array);
            deal_price_detail.put("remain_payment_money_array", remain_payment_money_array);
            deal_price_detail.put("note_array", note_array);
            deal_price_detail.put("updated_date_array", updated_date_array);

            /**
             * Danh sách promo của sản phẩm
             */
            deal.put("promos", this.getListPromoByDealPrice(deal));

            JSONObject data = new JSONObject();
            data.put("deal", deal);
            data.put("deal_price_detail", deal_price_detail);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private JSONObject convertRequestDeliveryDateInfoData(JSONObject deal_detail_round, JSONObject prev) {
        JSONObject jsInfo = new JSONObject();
        jsInfo.put("app_value", deal_detail_round.get("request_delivery_date_app"));
        jsInfo.put("cms_value", deal_detail_round.get("request_delivery_date_cms"));
        jsInfo.put("app_confirm",
                this.getDataStatusOfApp(
                        deal_detail_round.get("request_delivery_date_app"),
                        prev == null ? null : prev.get("request_delivery_date_cms")
                )
        );
        jsInfo.put("cms_confirm",
                this.getDataStatusOfCms(
                        deal_detail_round.get("request_delivery_date_app"),
                        deal_detail_round.get("request_delivery_date_cms")
                )
        );

        return jsInfo;
    }

    private JSONObject convertTotalEndPriceInfoData(
            JSONObject deal_detail_round,
            JSONObject prev) {
        JSONObject jsInfo = new JSONObject();
        jsInfo.put("app_value",
                this.getTotalEndPriceApp(deal_detail_round));
        jsInfo.put("cms_value",
                this.getTotalEndPriceCms(deal_detail_round));
        return jsInfo;
    }

    private JSONObject convertDepositMoneyInfoData(
            JSONObject deal_detail_round,
            JSONObject prev) {
        JSONObject jsInfo = new JSONObject();
        jsInfo.put("app_value",
                this.getDepositMoneyApp(deal_detail_round));
        jsInfo.put("cms_value",
                this.getDepositMoneyCms(deal_detail_round));
        return jsInfo;
    }

    private JSONObject convertRemainPaymentMoneyInfoData(
            JSONObject deal_detail_round,
            JSONObject prev) {
        JSONObject jsInfo = new JSONObject();
        jsInfo.put("app_value",
                this.getRemainPaymentMoneyApp(deal_detail_round));
        jsInfo.put("cms_value",
                this.getRemainPaymentMoneyCms(deal_detail_round));
        return jsInfo;
    }

    private JSONObject convertNoteInfoData(
            JSONObject deal_detail_round,
            JSONObject prev) {
        JSONObject jsInfo = new JSONObject();
        jsInfo.put("app_value",
                deal_detail_round.get("note_app"));
        jsInfo.put("cms_value",
                deal_detail_round.get("note_cms"));
        return jsInfo;
    }

    private JSONObject convertUpdatedDateInfoData(
            JSONObject deal_detail_round,
            JSONObject prev) {
        JSONObject jsInfo = new JSONObject();
        jsInfo.put("app_value",
                deal_detail_round.get("updated_date_app"));
        jsInfo.put("cms_value",
                deal_detail_round.get("updated_date_cms"));
        return jsInfo;
    }

    private Long getTotalEndPriceApp(JSONObject deal_detail_round) {
        return ConvertUtils.toInt(deal_detail_round.get("product_total_quantity_app"))
                * ConvertUtils.toLong(deal_detail_round.get("product_price_app"));
    }

    private Long getTotalEndPriceCms(JSONObject deal_detail_round) {
        return ConvertUtils.toInt(deal_detail_round.get("product_total_quantity_cms"))
                * ConvertUtils.toLong(deal_detail_round.get("product_price_cms"));
    }

    private Long getDepositMoneyCms(JSONObject deal_detail_round) {
        return ConvertUtils.toInt(deal_detail_round.get("deposit_percent_cms")) *
                ConvertUtils.toInt(deal_detail_round.get("product_total_quantity_cms"))
                * ConvertUtils.toLong(deal_detail_round.get("product_price_cms")) / 100;
    }

    private Long getDepositMoneyApp(JSONObject deal_detail_round) {
        return ConvertUtils.toInt(deal_detail_round.get("deposit_percent_app")) *
                ConvertUtils.toInt(deal_detail_round.get("product_total_quantity_app"))
                * ConvertUtils.toLong(deal_detail_round.get("product_price_app")) / 100;
    }

    private Long getRemainPaymentMoneyCms(JSONObject deal_detail_round) {
        return getTotalEndPriceCms(deal_detail_round) - getDepositMoneyCms(deal_detail_round);
    }

    private Long getRemainPaymentMoneyApp(JSONObject deal_detail_round) {
        return getTotalEndPriceApp(deal_detail_round) - getDepositMoneyApp(deal_detail_round);
    }

    private JSONObject convertCompletePaymentDurationInfoData(JSONObject deal_detail_round, JSONObject prev) {
        JSONObject jsInfo = new JSONObject();
        jsInfo.put("app_value", deal_detail_round.get("complete_payment_duration_app"));
        jsInfo.put("cms_value", deal_detail_round.get("complete_payment_duration_cms"));
        jsInfo.put("app_confirm",
                this.getDataStatusOfApp(
                        deal_detail_round.get("complete_payment_duration_app"),
                        prev == null ? null : prev.get("complete_payment_duration_cms")
                )
        );
        jsInfo.put("cms_confirm",
                this.getDataStatusOfCms(
                        deal_detail_round.get("complete_payment_duration_app"),
                        deal_detail_round.get("complete_payment_duration_cms")
                ));
        return jsInfo;
    }

    private JSONObject convertPaymentDurationInfoData(JSONObject deal_detail_round, JSONObject prev) {
        JSONObject jsInfo = new JSONObject();
        jsInfo.put("app_value", deal_detail_round.get("payment_duration_app"));
        jsInfo.put("cms_value", deal_detail_round.get("payment_duration_cms"));
        jsInfo.put("app_confirm",
                this.getDataStatusOfApp(
                        deal_detail_round.get("payment_duration_app"),
                        prev == null ? prev : prev.get("payment_duration_cms")
                )
        );
        jsInfo.put("cms_confirm",
                this.getDataStatusOfCms(
                        deal_detail_round.get("payment_duration_app"),
                        deal_detail_round.get("payment_duration_cms")
                )
        );
        return jsInfo;
    }

    private JSONObject convertDepositPercentInfoData(JSONObject deal_detail_round, JSONObject prev) {
        JSONObject jsInfo = new JSONObject();
        jsInfo.put("app_value", deal_detail_round.get("deposit_percent_app"));
        jsInfo.put("cms_value", deal_detail_round.get("deposit_percent_cms"));
        jsInfo.put("app_confirm",
                this.getDataStatusOfApp(
                        deal_detail_round.get("deposit_percent_app"),
                        prev == null ? null : prev.get("deposit_percent_cms")
                )
        );
        jsInfo.put("cms_confirm",
                this.getDataStatusOfCms(
                        deal_detail_round.get("deposit_percent_app"),
                        deal_detail_round.get("deposit_percent_cms")
                ));
        return jsInfo;
    }

    private JSONObject convertQuantityInfoData(JSONObject deal_detail_round, JSONObject prev) {
        JSONObject jsInfo = new JSONObject();
        jsInfo.put("app_value", deal_detail_round.get("product_total_quantity_app"));
        jsInfo.put("cms_value", deal_detail_round.get("product_total_quantity_cms"));
        jsInfo.put("app_confirm",
                this.getDataStatusOfApp(
                        deal_detail_round.get("product_total_quantity_app"),
                        prev == null ? null : prev.get("product_total_quantity_cms")
                )
        );
        jsInfo.put("cms_confirm",
                this.getDataStatusOfCms(
                        deal_detail_round.get("product_total_quantity_app"),
                        deal_detail_round.get("product_total_quantity_cms")
                )
        );
        return jsInfo;
    }

    private JSONObject convertPriceInfoData(JSONObject deal_detail_round, JSONObject pre_data) {
        JSONObject jsInfo = new JSONObject();
        jsInfo.put("app_value", deal_detail_round.get("product_price_app"));
        jsInfo.put("cms_value", deal_detail_round.get("product_price_cms"));

        jsInfo.put("app_confirm",
                this.getDataStatusOfApp(
                        deal_detail_round.get("product_price_app"),
                        pre_data == null ? null : pre_data.get("product_price_cms"))
        );

        jsInfo.put("cms_confirm",
                this.getDataStatusOfCms(
                        deal_detail_round.get("product_price_app"),
                        deal_detail_round.get("product_price_cms")));
        return jsInfo;
    }

    private int getDataStatusOfApp(Object app, Object cms) {
        if (cms == null) {
            return 0;
        }

        if (ConvertUtils.toString(app).equals(ConvertUtils.toString(cms))) {
            return 1;
        }

        return 2;
    }

    private int getDataStatusOfCms(Object app, Object cms) {
        if (app == null) {
            return 0;
        }

        if (ConvertUtils.toString(app).equals(ConvertUtils.toString(cms))) {
            return 1;
        }

        return 2;
    }

    public ClientResponse cancelDealPrice(SessionData sessionData, CancelRequest request) {
        try {
            JSONObject deal = this.dealDB.getDealPrice(request.getId());
            if (deal == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            if (ConvertUtils.toInt(deal.get("status")) == DealPriceStatus.CONFIRMED.getId()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            boolean rsCancel = this.dealDB.cancelDealPrice(
                    request.getId(),
                    request.getNote(),
                    sessionData.getId()
            );
            if (!rsCancel) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(
                    ConvertUtils.toInt(deal.get("product_id"))
            );
            if (productCache != null) {
                JSONObject agencyInfo = this.dataManager.getAgencyManager().getAgencyInfo(
                        ConvertUtils.toInt(deal.get("agency_id"))
                );
                if (agencyInfo == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                ProductCache productPrice = this.getFinalPriceByAgency(
                        ConvertUtils.toInt(deal.get("product_id")),
                        ConvertUtils.toInt(deal.get("agency_id")),
                        ConvertUtils.toInt(agencyInfo.get("city_id")),
                        ConvertUtils.toInt(agencyInfo.get("region_id")),
                        ConvertUtils.toInt(agencyInfo.get("membership_id"))
                );

                this.dealDB.saveProductInfoToDealPrice(
                        request.getId(),
                        ConvertUtils.toLong(productPrice == null ? 0 : productPrice.getPrice()),
                        productCache.getFull_name(),
                        productCache.getImages(),
                        productCache.getProduct_small_unit_id(),
                        productCache.getCode()
                );
            }

            this.savePromoIntoDealPrice(
                    request.getId(),
                    ConvertUtils.toInt(deal.get("agency_id")),
                    ConvertUtils.toInt(deal.get("product_id")));

            /**
             * Thông báo cho đại lý
             */
            this.pushNotifyToAgency(
                    0,
                    NotifyAutoContentType.THACH_GIA_CHI_TIET,
                    "",
                    NotifyAutoContentType.THACH_GIA_CHI_TIET.getType(),
                    JsonUtils.Serialize(
                            Arrays.asList(ConvertUtils.toString(request.getId()))
                    ),
                    "Thách giá " + ConvertUtils.toString(deal.get("code")) + " của Quý khách đã bị từ chối vì " +
                            request.getNote(),
                    ConvertUtils.toInt(deal.get("agency_id"))
            );

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }
}