package com.app.server.service;

import com.app.server.config.ConfigInfo;
import com.app.server.constants.ResponseMessage;
import com.app.server.data.SessionData;
import com.app.server.data.dto.agency.Agency;
import com.app.server.data.dto.product.ProductCache;
import com.app.server.data.entity.*;
import com.app.server.data.request.BasicRequest;
import com.app.server.data.request.FilterListByIdRequest;
import com.app.server.data.request.FilterListRequest;
import com.app.server.data.request.FilterRequest;
import com.app.server.data.request.product.CreateProductPriceSettingRequest;
import com.app.server.data.request.product.ProductPriceSettingDetailRequest;
import com.app.server.database.AgencyDB;
import com.app.server.database.PriceDB;
import com.app.server.database.ProductDB;
import com.app.server.enums.*;
import com.app.server.manager.DataManager;
import com.app.server.response.ClientResponse;
import com.app.server.utils.AppUtils;
import com.app.server.utils.FilterUtils;
import com.app.server.utils.JsonUtils;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ProductPriceSettingTimerService {
    private ProductDB productDB;

    @Autowired
    public void setProductDB(ProductDB productDB) {
        this.productDB = productDB;
    }

    private DataManager dataManager;

    @Autowired
    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    private PriceDB priceDB;

    @Autowired
    public void setPriceDB(PriceDB priceDB) {
        this.priceDB = priceDB;
    }

    private FilterUtils filterUtils;

    @Autowired
    public void setFilterUtils(FilterUtils filterUtils) {
        this.filterUtils = filterUtils;
    }

    private AppUtils appUtils;

    @Autowired
    public void setAppUtils(AppUtils appUtils) {
        this.appUtils = appUtils;
    }

    private BravoService bravoService;

    @Autowired
    public void setBravoService(BravoService bravoService) {
        this.bravoService = bravoService;
    }

    private AgencyDB agencyDB;

    @Autowired
    public void setAgencyDB(AgencyDB agencyDB) {
        this.agencyDB = agencyDB;
    }

    public ClientResponse createProductPriceSettingTimer(SessionData sessionData, CreateProductPriceSettingRequest request) {
        try {
            if (!this.dataManager.getStaffManager().checkStaffManageAgency(
                    sessionData.getId(),
                    this.agencyDB.getAgencyInfoById(request.getPrice_object_id()))) {
                return ClientResponse.fail(ResponseStatus.NOT_PERMISSION, ResponseMessage.USER_FORBIDDEN);
            }

            ProductPriceSettingEntity productPriceSettingEntity =
                    this.priceDB.getProductPriceSettingEntityByAgency(
                            request.getPrice_object_id()
                    );
            if (productPriceSettingEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SETTING_OBJECT_NOT_EXIST);
            }

            if (productPriceSettingEntity.getStatus() !=
                    SettingStatus.RUNNING.getId()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SETTING_OBJECT_NOT_ACTIVE);
            }

            Date now = DateTimeUtils.getNow();
            ProductPriceSettingTimerEntity priceSettingTimerEntity
                    = new ProductPriceSettingTimerEntity();
            priceSettingTimerEntity.setName(request.getName());
            priceSettingTimerEntity.setCreator_id(sessionData.getId());
            priceSettingTimerEntity.setCreated_date(DateTimeUtils.getNow());
            priceSettingTimerEntity.setStart_date(DateTimeUtils.getDateTime(request.getStart_date()));
            priceSettingTimerEntity.setEnd_date(request.getEnd_date() == null ? null : DateTimeUtils.getDateTime(request.getEnd_date()));
            priceSettingTimerEntity.setPrice_object_type(request.getPrice_object_type());
            priceSettingTimerEntity.setPrice_object_id(request.getPrice_object_id());
            SettingObjectType settingObjectType = SettingObjectType.from(
                    request.getPrice_object_type());
            priceSettingTimerEntity.setNote(
                    request.getNote()
            );
            switch (settingObjectType) {
                case AGENCY:
                    priceSettingTimerEntity.setAgency_id(request.getPrice_object_id());
                    break;
                case CITY:
                    priceSettingTimerEntity.setCity_id(request.getPrice_object_id());
                    break;
                case REGION:
                    priceSettingTimerEntity.setRegion_id(request.getPrice_object_id());
                    break;
                case MEMBERSHIP:
                    priceSettingTimerEntity.setMembership_id(request.getPrice_object_id());
                    break;
            }
            priceSettingTimerEntity.setStatus(ProductPriceTimerStatus.DRAFT.getId());

            int rsInsertSetting = this.priceDB.insertProductPriceSettingTimer(
                    priceSettingTimerEntity);
            if (rsInsertSetting <= 0) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            priceSettingTimerEntity.setId(rsInsertSetting);

            for (ProductPriceSettingDetailRequest productPriceSettingDetailRequest
                    : request.getRecords()) {
                ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(
                        productPriceSettingDetailRequest.getProduct_id()
                );
                if (productCache == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PRODUCT_INVALID);
                }
                ProductPriceSettingDetailEntity productPriceSettingDetailEntity = new ProductPriceSettingDetailEntity();
                productPriceSettingDetailEntity.setProduct_price_setting_id(
                        priceSettingTimerEntity.getId());
                productPriceSettingDetailEntity.setStart_date(
                        priceSettingTimerEntity.getStart_date()
                );
                productPriceSettingDetailEntity.setEnd_date(
                        productPriceSettingDetailRequest.getEnd_date() == null ? null :
                                DateTimeUtils.getDateTime(productPriceSettingDetailRequest.getEnd_date())
                );

                PriceDataType priceDataType = PriceDataType.from(
                        productPriceSettingDetailRequest.getPrice_data_type()
                );
                if (priceDataType == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                productPriceSettingDetailEntity.setCreator_id(sessionData.getId());
                productPriceSettingDetailEntity.setPrice_setting_value(
                        productPriceSettingDetailRequest.getPrice_setting_value()
                );

                productPriceSettingDetailEntity.setStatus(
                        productPriceSettingDetailRequest.getStatus()
                );

                productPriceSettingDetailEntity.setCreated_date(DateTimeUtils.getNow());
                productPriceSettingDetailEntity.setPrice_data_type(
                        productPriceSettingDetailRequest.getPrice_data_type()
                );
                productPriceSettingDetailEntity.setPrice_setting_type(
                        productPriceSettingDetailRequest.getPrice_setting_type()
                );
                productPriceSettingDetailEntity.setPrice_setting_value(
                        productPriceSettingDetailRequest.getPrice_setting_value()
                );
                productPriceSettingDetailEntity.setProduct_id(
                        productPriceSettingDetailRequest.getProduct_id()
                );
                productPriceSettingDetailEntity.setMinimum_purchase(
                        productPriceSettingDetailRequest.getMinimum_purchase()
                );
                productPriceSettingDetailEntity.setIs_auto(
                        productPriceSettingDetailRequest.getIs_auto()
                );

                JSONObject jsSettingDetail = JsonUtils.DeSerialize(
                        JsonUtils.Serialize(productPriceSettingDetailEntity),
                        JSONObject.class);

                int rsInsertDetail =
                        this.priceDB.insertProductPriceSettingTimerDetail(
                                productPriceSettingDetailRequest.getProduct_id(),
                                rsInsertSetting,
                                JsonUtils.Serialize(productPriceSettingDetailEntity),
                                productPriceSettingDetailEntity.getStart_date(),
                                productPriceSettingDetailEntity.getEnd_date(),
                                productPriceSettingDetailRequest.getStatus(),
                                ""
                        );
                if (rsInsertDetail <= 0) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
                productPriceSettingDetailEntity.setId(rsInsertDetail);
            }

            JSONObject data = new JSONObject();
            data.put("id", priceSettingTimerEntity.getId());
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse editProductPriceSettingTimer(SessionData sessionData, CreateProductPriceSettingRequest request) {
        try {
            JSONObject setting = this.priceDB.getProductPriceSettingTimer(
                    request.getId());
            if (setting == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            Date now = DateTimeUtils.getNow();
            ProductPriceSettingTimerEntity priceSettingTimerEntity
                    = ProductPriceSettingTimerEntity.from(setting);
            priceSettingTimerEntity.setName(request.getName());
            priceSettingTimerEntity.setStart_date(DateTimeUtils.getDateTime(request.getStart_date()));
            priceSettingTimerEntity.setEnd_date(request.getEnd_date() == null ? null : DateTimeUtils.getDateTime(request.getEnd_date()));
            priceSettingTimerEntity.setPrice_object_type(request.getPrice_object_type());
            priceSettingTimerEntity.setPrice_object_id(request.getPrice_object_id());
            SettingObjectType settingObjectType = SettingObjectType.from(
                    request.getPrice_object_type());
            switch (settingObjectType) {
                case AGENCY:
                    priceSettingTimerEntity.setAgency_id(request.getPrice_object_id());
                    break;
                case CITY:
                    priceSettingTimerEntity.setCity_id(request.getPrice_object_id());
                    break;
                case REGION:
                    priceSettingTimerEntity.setRegion_id(request.getPrice_object_id());
                    break;
                case MEMBERSHIP:
                    priceSettingTimerEntity.setMembership_id(request.getPrice_object_id());
                    break;
            }

            /**
             * Xóa dữ liệu cũ
             */
            boolean rsClearOldData = this.priceDB.clearProductPriceSettingTimerDetail(
                    request.getId()
            );
            if (!rsClearOldData) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            boolean rsUpdateSetting = this.priceDB.updateProductPriceSettingTimer(
                    priceSettingTimerEntity);
            if (!rsUpdateSetting) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            for (ProductPriceSettingDetailRequest productPriceSettingDetailRequest
                    : request.getRecords()) {
                ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(
                        productPriceSettingDetailRequest.getProduct_id()
                );
                if (productCache == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PRODUCT_INVALID);
                }
                ProductPriceSettingDetailEntity productPriceSettingDetailEntity = new ProductPriceSettingDetailEntity();
                productPriceSettingDetailEntity.setProduct_price_setting_id(
                        priceSettingTimerEntity.getId());
                productPriceSettingDetailEntity.setStart_date(
                        priceSettingTimerEntity.getStart_date());

                productPriceSettingDetailEntity.setEnd_date(
                        productPriceSettingDetailRequest.getEnd_date() == null ? null :
                                DateTimeUtils.getDateTime(productPriceSettingDetailRequest.getEnd_date())
                );

                PriceDataType priceDataType = PriceDataType.from(
                        productPriceSettingDetailRequest.getPrice_data_type()
                );
                if (priceDataType == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                productPriceSettingDetailEntity.setCreator_id(sessionData.getId());
                productPriceSettingDetailEntity.setPrice_setting_value(
                        productPriceSettingDetailRequest.getPrice_setting_value()
                );

                productPriceSettingDetailEntity.setStatus(productPriceSettingDetailRequest.getStatus());

                productPriceSettingDetailEntity.setCreated_date(DateTimeUtils.getNow());
                productPriceSettingDetailEntity.setPrice_data_type(
                        productPriceSettingDetailRequest.getPrice_data_type()
                );
                productPriceSettingDetailEntity.setPrice_setting_type(
                        productPriceSettingDetailRequest.getPrice_setting_type()
                );
                productPriceSettingDetailEntity.setPrice_setting_value(
                        productPriceSettingDetailRequest.getPrice_setting_value()
                );
                productPriceSettingDetailEntity.setProduct_id(
                        productPriceSettingDetailRequest.getProduct_id()
                );
                productPriceSettingDetailEntity.setMinimum_purchase(
                        productPriceSettingDetailRequest.getMinimum_purchase()
                );
                productPriceSettingDetailEntity.setIs_auto(
                        productPriceSettingDetailRequest.getIs_auto()
                );

                JSONObject jsSettingDetail = JsonUtils.DeSerialize(
                        JsonUtils.Serialize(productPriceSettingDetailEntity),
                        JSONObject.class);

                int rsInsertDetail =
                        this.priceDB.insertProductPriceSettingTimerDetail(
                                productPriceSettingDetailRequest.getProduct_id(),
                                request.getId(),
                                JsonUtils.Serialize(productPriceSettingDetailEntity),
                                productPriceSettingDetailEntity.getStart_date(),
                                productPriceSettingDetailEntity.getEnd_date(),
                                productPriceSettingDetailRequest.getStatus(),
                                ""
                        );
                if (rsInsertDetail <= 0) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
            }

            JSONObject data = new JSONObject();
            data.put("setting", priceSettingTimerEntity);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse getProductPriceSettingTimer(SessionData sessionData, BasicRequest request) {
        try {
            JSONObject setting = this.priceDB.getProductPriceSettingTimer(
                    request.getId());
            if (setting == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            int agency_id = ConvertUtils.toInt(setting
                    .get("agency_id"));
            if (agency_id > 0) {
                if (!this.dataManager.getStaffManager().checkStaffManageAgency(
                        sessionData.getId(),
                        this.agencyDB.getAgencyInfoById(agency_id))) {
                    return ClientResponse.fail(ResponseStatus.NOT_PERMISSION, ResponseMessage.USER_FORBIDDEN);
                }

                setting.put("agency_info", this.dataManager.getAgencyManager().getAgencyBasicData(
                        ConvertUtils.toInt(setting
                                .get("agency_id"))
                ));
                setting.put("creator_info", this.dataManager.getStaffManager().getStaff(
                        ConvertUtils.toInt(setting
                                .get("creator_id"))
                ));
            }

            JSONObject data = new JSONObject();
            data.put("product_price_setting_timer", setting);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterProductPriceSettingTimer(SessionData sessionData, FilterListRequest request) {
        try {
            this.addFilterAgency(sessionData, request);
            String query = this.filterUtils.getQuery(FunctionList.LIST_PRODUCT_PRICE_SETTING_TIMER, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.productDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            for (JSONObject js : records) {
                js.put("creator_info", this.dataManager.getStaffManager().getStaff(
                        ConvertUtils.toInt(js
                                .get("creator_id"))
                ));
                int agency_id = ConvertUtils.toInt(js
                        .get("agency_id"));
                if (agency_id > 0) {
                    js.put("agency_info", this.dataManager.getAgencyManager().getAgencyBasicData(
                            ConvertUtils.toInt(js
                                    .get("agency_id"))
                    ));
                }

                js.put("confirmer_info", this.dataManager.getStaffManager().getStaff(
                        ConvertUtils.toInt(js
                                .get("confirmer_id"))
                ));
            }
            int total = this.productDB.getTotal(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterProductPriceSettingTimerDetail(FilterListByIdRequest request) {
        try {
            JSONObject setting = this.priceDB.getProductPriceSettingTimer(request.getId());
            if (setting == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            int price_object_id = ConvertUtils.toInt(setting.get("price_object_id"));
            String price_object_type = ConvertUtils.toString(setting.get("price_object_type"));

            JSONObject data = new JSONObject();
            FilterRequest filterRequest = new FilterRequest();
            filterRequest.setType("select");
            filterRequest.setKey("t.product_price_setting_timer_id");
            filterRequest.setValue(ConvertUtils.toString(request.getId()));
            request.getFilters().add(filterRequest);
            String query = this.filterUtils.getQuery(FunctionList.LIST_PRODUCT_PRICE_SETTING_TIMER_DETAIL, request.getFilters(), request.getSorts());
            List<JSONObject> records = this.productDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            List<JSONObject> settingDetailList = new ArrayList<>();
            for (JSONObject js : records) {
                int product_id = ConvertUtils.toInt(js
                        .get("product_id"));
                JSONObject settingDetail =
                        JsonUtils.DeSerialize(
                                js.get("data").toString(),
                                JSONObject.class
                        );

                settingDetail.put(
                        "status", js.get("status")
                );
                settingDetail.put(
                        "start_date", js.get("start_date")
                );
                settingDetail.put(
                        "end_date", js.get("end_date")
                );

                ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(
                        product_id
                );

                Double current_price = this.appUtils.convertProductPrice(
                        productCache.getPrice(),
                        ConvertUtils.toLong(settingDetail.get("price_original")),
                        ConvertUtils.toInt(settingDetail.get("is_auto")),
                        ConvertUtils.toString(settingDetail.get("price_setting_type")),
                        ConvertUtils.toString(settingDetail.get("price_data_type")),
                        ConvertUtils.toDouble(settingDetail.get("price_setting_value")));

                settingDetail.put("current_price", current_price);

                settingDetail.put("product_info", productCache);
                settingDetailList.add(settingDetail);
            }

            int total = this.productDB.getTotal(query);
            data.put("records", settingDetailList);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse approveProductPriceSettingTimer(
            SessionData sessionData,
            BasicRequest request) {
        try {
            ClientResponse crValidate = request.validate();
            if (crValidate.failed()) {
                return crValidate;
            }

            JSONObject jsSetting = this.priceDB.getProductPriceSettingTimer(request.getId());
            if (jsSetting == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SETTING_NOT_FOUND);
            }
            ProductPriceSettingTimerEntity entity = ProductPriceSettingTimerEntity.from(
                    jsSetting
            );
            if (entity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SETTING_NOT_FOUND);
            }

            if (entity.getStatus() != ProductPriceTimerStatus.DRAFT.getId()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            Date now = DateTimeUtils.getNow();

            entity.setModifier_id(sessionData.getId());
            entity.setModified_date(now);
            if (entity.getStart_date().before(now)) {
                entity.setStart_date(now);
            }

            entity.setConfirmer_id(sessionData.getId());

            if (entity.getStart_date().after(now)) {
                entity.setStatus(ProductPriceTimerStatus.WAITING.getId());
                boolean rsUpdate = this.priceDB.updateProductPriceSettingTimer(
                        entity
                );
                if (!rsUpdate) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
            } else {
                entity.setStatus(ProductPriceTimerStatus.RUNNING.getId());
                boolean rsUpdate = this.priceDB.updateProductPriceSettingTimer(
                        entity
                );
                if (!rsUpdate) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
                this.runProductPriceSettingTimer(
                        entity,
                        this.priceDB.getListProductPriceSettingTimerDetail(
                                entity.getId()
                        )
                );
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse runProductPriceSettingTimer(
            ProductPriceSettingTimerEntity entity,
            List<JSONObject> settingDetailList) {
        try {

            ProductPriceSettingEntity productPriceSettingEntity =
                    this.priceDB.getProductPriceSettingEntityByAgency(
                            entity.getAgency_id()
                    );
            for (JSONObject jsDetail : settingDetailList) {
                ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(
                        ConvertUtils.toInt(jsDetail.get("product_id"))
                );
                if (productCache == null) {
                    continue;
                }

                ProductPriceSettingTimerDetailEntity productPriceSettingTimerDetailEntity =
                        ProductPriceSettingTimerDetailEntity.from(
                                jsDetail
                        );

                ProductPriceSettingDetailEntity productPriceSettingDetailEntity =
                        this.priceDB.getProductPriceSettingDetailEntityByProductIdAndSettingId(
                                ConvertUtils.toInt(jsDetail.get("product_id")),
                                productPriceSettingEntity.getId()
                        );
                if (productPriceSettingDetailEntity == null) {
                    productPriceSettingDetailEntity = new ProductPriceSettingDetailEntity();
                    productPriceSettingDetailEntity.setProduct_price_setting_id(
                            productPriceSettingEntity.getId()
                    );
                    productPriceSettingDetailEntity.setCreator_id(entity.getCreator_id());
                    productPriceSettingDetailEntity = this.convertProductPriceSettingDetail(
                            productPriceSettingDetailEntity,
                            productPriceSettingEntity,
                            jsDetail,
                            ConvertUtils.toLong(productCache.getPrice())
                    );
                    this.priceDB.insertProductPriceSettingDetail(
                            productPriceSettingDetailEntity
                    );

                    productPriceSettingTimerDetailEntity.setData(
                            JsonUtils.Serialize(productPriceSettingDetailEntity)
                    );
                    this.priceDB.updateProductPriceSettingTimerDetail(
                            productPriceSettingTimerDetailEntity
                    );
                } else {
                    productPriceSettingDetailEntity.setCreator_id(entity.getCreator_id());
                    if (ConvertUtils.toInt(jsDetail.get("status")) == SettingStatus.PENDING.getId()) {
                        productPriceSettingDetailEntity.setEnd_date(
                                entity.getStart_date()
                        );
                        productPriceSettingDetailEntity.setStatus(
                                SettingStatus.PENDING.getId()
                        );
                        productPriceSettingTimerDetailEntity.setData(
                                JsonUtils.Serialize(productPriceSettingDetailEntity)
                        );
                        this.priceDB.updateProductPriceSettingTimerDetail(
                                productPriceSettingTimerDetailEntity
                        );
                    } else {
                        productPriceSettingDetailEntity = this.convertProductPriceSettingDetail(
                                productPriceSettingDetailEntity,
                                productPriceSettingEntity,
                                jsDetail,
                                ConvertUtils.toLong(productCache.getPrice())
                        );
                        productPriceSettingTimerDetailEntity.setData(
                                JsonUtils.Serialize(productPriceSettingDetailEntity)
                        );
                        this.priceDB.updateProductPriceSettingTimerDetail(
                                productPriceSettingTimerDetailEntity
                        );
                    }
                    this.priceDB.updateProductPriceSettingDetail(
                            productPriceSettingDetailEntity
                    );
                }
            }

            this.dataManager.callReloadProductPriceSetting(productPriceSettingEntity.getId());

            ClientResponse crSyncPriceSetting = this.syncPriceSettingToBravo(
                    productPriceSettingEntity.getId(),
                    entity.getStart_date()
            );
            if (crSyncPriceSetting.failed()) {
                this.priceDB.syncPriceSettingFailed(
                        productPriceSettingEntity.getId(),
                        crSyncPriceSetting.getMessage()
                );
            } else {
                this.priceDB.syncPriceSettingSuccess(
                        productPriceSettingEntity.getId()
                );
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ProductPriceSettingDetailEntity convertProductPriceSettingDetail(
            ProductPriceSettingDetailEntity productPriceSettingDetailEntity,
            ProductPriceSettingEntity productPriceSettingEntity,
            JSONObject detail,
            long price_original) {
        productPriceSettingDetailEntity.setProduct_id(
                ConvertUtils.toInt(detail.get("product_id"))
        );
        productPriceSettingDetailEntity.setStatus(
                ConvertUtils.toInt(detail.get("status"))
        );
        productPriceSettingDetailEntity.setStart_date(
                DateTimeUtils.getDateTime(
                        ConvertUtils.toString(
                                detail.get("start_date")))
        );
        productPriceSettingDetailEntity.setEnd_date(
                DateTimeUtils.getDateTime(
                        ConvertUtils.toString(
                                detail.get("end_date")))
        );

        ProductPriceSettingDetailEntity data = ProductPriceSettingDetailEntity.from(
                JsonUtils.DeSerialize(
                        detail.get("data").toString(),
                        JSONObject.class
                )
        );
        productPriceSettingDetailEntity.setPrice_setting_type(
                data.getPrice_setting_type()
        );
        productPriceSettingDetailEntity.setPrice_data_type(
                data.getPrice_data_type()
        );
        productPriceSettingDetailEntity.setPrice_setting_value(
                data.getPrice_setting_value()
        );
        productPriceSettingDetailEntity.setIs_auto(
                data.getIs_auto()
        );
        productPriceSettingDetailEntity.setMinimum_purchase(
                data.getMinimum_purchase()
        );
        productPriceSettingDetailEntity.setPrice_original(
                price_original
        );
        productPriceSettingDetailEntity.setPrice_new(
                ConvertUtils.toLong(appUtils.convertProductPrice(
                        price_original,
                        price_original,
                        productPriceSettingDetailEntity.getIs_auto(),
                        productPriceSettingDetailEntity.getPrice_setting_type(),
                        productPriceSettingDetailEntity.getPrice_data_type(),
                        productPriceSettingDetailEntity.getPrice_setting_value())
                )
        );

        return productPriceSettingDetailEntity;
    }

    public ClientResponse syncPriceSettingToBravo(int product_price_setting_id, Date settingDate) {
        try {
            JSONObject setting = this.priceDB.getProductPriceSettingRunning(
                    product_price_setting_id);
            if (setting == null) {
                this.priceDB.syncPriceSettingFailed(
                        product_price_setting_id,
                        ResponseMessage.STATUS_NOT_MATCH.getValue()
                );
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            JSONObject jsAgency = this.agencyDB.getAgencyInfo(
                    ConvertUtils.toInt(setting.get("agency_id"))
            );
            if (jsAgency == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
            }

            List<JSONObject> agencyList = new ArrayList<>();
            agencyList.add(
                    jsAgency
            );
            ClientResponse crSync = ClientResponse.success(null);
            for (JSONObject agency : agencyList) {
//                {
                JSONObject priceData = new JSONObject();
//                    "docDate": "2023-07-31",
                priceData.put("docDate",
                        DateTimeUtils.toString(
                                settingDate,
                                "yyyy-MM-dd"));
//                        "effectiveDate": "2023-07-31 10:00:00",
                priceData.put("effectiveDate",
                        setting.get("start_date") == null ?
                                null :
                                DateTimeUtils.toString(DateTimeUtils.getDateTime(
                                                setting.get("start_date").toString(), "yyyy-MM-dd HH:mm:ss"),
                                        "yyyy-MM-dd HH:mm:ss"));
//                        "finishDate": null,
                priceData.put("finishDate", setting.get("end_date") == null ?
                        null :
                        DateTimeUtils.toString(DateTimeUtils.getDateTime(
                                        setting.get("end_date").toString(), "yyyy-MM-dd HH:mm:ss"),
                                "yyyy-MM-dd HH:mm:ss"));
//                        "docNo": "BGB00003",
                priceData.put("docNo",
                        "BG02-" + ConvertUtils.toString(agency.get("id")));
//                        "description": "Tên bản giá",
                priceData.put("description", ConvertUtils.toString(setting.get("name")));
//                        "priceListType": "01",
                priceData.put("priceListType", "02");
//                        "customFieldCode1": "SI",
                priceData.put("customFieldCode1", "SI");
//                        "note": "Ghi chú",
                priceData.put("note", setting.get("note"));
//                        "customerAppId":2,
                priceData.put("customerAppId", ConvertUtils.toInt(agency.get("id")));
//                        "customerCode":"BRAVO",
                priceData.put("customerCode", ConvertUtils.toString(agency.get("code")));
//                        "customerName":"Công ty cổ phần phần mềm BRAVO",
                priceData.put("customerName", ConvertUtils.toString(agency.get("shop_name")));
//                        "customerLevelAppId":null,
                priceData.put("customerLevelAppId", null);
//                        "customerLevelCode":"",
                priceData.put("customerLevelCode", "");

                //                        "detailData": [
                List<JSONObject> detailData = new ArrayList<>();
                List<JSONObject> settingDetailList = this.priceDB.getAllSettingDetail(
                        product_price_setting_id);

                for (JSONObject settingDetail : settingDetailList) {
                    JSONObject item = new JSONObject();
//                        "bravoItemId": 7981,
                    ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(
                            ConvertUtils.toInt(settingDetail.get("product_id"))
                    );
                    if (productCache == null) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }
                    if (productCache.getBravo_id() == 0) {
                        ClientResponse crProduct = ClientResponse.fail(ResponseStatus.FAIL,
                                ResponseMessage.FAIL);
                        crProduct.fail(productCache.getCode() +
                                " chưa đồng bộ Bravo");
                        return crProduct;
                    }

                    item.put("bravoItemId", productCache.getBravo_id());
//                            "itemCode": "HYUNDAI_HD-4111",
                    item.put("itemCode", productCache.getCode());
//                            "itemName": "Máy cưa xích HYUNDAI HD-4111 (Lam 16\", xích Oregon 28.5 mắc)",
                    item.put("itemName", productCache.getFull_name());
//                            "standardPrice": 10
                    //                        "effectiveDate": "2023-07-31 10:00:00",
                    item.put("effectiveDate",
                            settingDetail.get("start_date") == null ?
                                    null :
                                    DateTimeUtils.toString(DateTimeUtils.getDateTime(
                                                    settingDetail.get("start_date").toString(), "yyyy-MM-dd HH:mm:ss"),
                                            "yyyy-MM-dd HH:mm:ss"));
//                        "finishDate": null,
                    item.put("finishDate", settingDetail.get("end_date") == null ?
                            null :
                            DateTimeUtils.toString(DateTimeUtils.getDateTime(
                                            settingDetail.get("end_date").toString(), "yyyy-MM-dd HH:mm:ss"),
                                    "yyyy-MM-dd HH:mm:ss"));

                    item.put("standardPrice", ConvertUtils.toLong(settingDetail.get("price_new")) > 0 ?
                            ConvertUtils.toLong(settingDetail.get("price_new")) :
                            0);
                    detailData.add(item);
                }
                priceData.put("detailData", detailData);
                crSync = this.bravoService.syncProductPrice(priceData);
                if (crSync.failed()) {
                    this.priceDB.syncPriceSettingFailed(
                            product_price_setting_id,
                            crSync.getMessage()
                    );
                    break;
                }
            }

            if (crSync.failed()) {
                return crSync;
            }

            this.priceDB.syncPriceSettingSuccess(
                    ConvertUtils.toInt(setting.get("id"))
            );
            return crSync;
        } catch (Exception ex) {
            this.priceDB.syncPriceSettingFailed(
                    product_price_setting_id,
                    ex.toString()
            );
            LogUtil.printDebug("PRICE: ", ex);
            ClientResponse crException = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            crException.setMessage(ex.getMessage());
            return crException;
        }
    }

    public ClientResponse runStartProductPriceSettingTimerSchedule() {
        try {
            List<JSONObject> records = this.priceDB.getListProductPriceSettingTimerNeedStart();
            for (JSONObject js : records) {
                ProductPriceSettingTimerEntity entity =
                        ProductPriceSettingTimerEntity.from(
                                js
                        );
                entity.setStatus(ProductPriceTimerStatus.RUNNING.getId());
                boolean rsUpdate = this.priceDB.updateProductPriceSettingTimer(
                        entity
                );
                if (!rsUpdate) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                this.runProductPriceSettingTimer(
                        entity,
                        this.priceDB.getListProductPriceSettingTimerDetail(
                                entity.getId()
                        )
                );
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse cancelProductPriceSettingTimer(SessionData sessionData, BasicRequest request) {
        try {
            JSONObject jsSetting =
                    this.priceDB.getProductPriceSettingTimer(request.getId());
            if (jsSetting == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SETTING_NOT_FOUND);
            }

            boolean rsUpdate = this.priceDB.cancelProductPriceSettingTimer(
                    request.getId(),
                    sessionData.getId()
            );
            if (!rsUpdate) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private void addFilterAgency(SessionData sessionData, FilterListRequest request) {
        FilterRequest filterRequest = this.dataManager.getStaffManager().getFilterAgency(
                this.dataManager.getStaffManager().getStaffManageData(
                        sessionData.getId()
                ));
        if (filterRequest != null) {
            request.getFilters().add(filterRequest);
        }
    }
}