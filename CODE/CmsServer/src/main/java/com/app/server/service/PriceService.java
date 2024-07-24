package com.app.server.service;

import com.app.server.config.ConfigInfo;
import com.app.server.constants.ResponseMessage;
import com.app.server.data.SessionData;
import com.app.server.data.dto.product.ProductCache;
import com.app.server.data.entity.*;
import com.app.server.data.request.BasicRequest;
import com.app.server.data.request.FilterListByIdRequest;
import com.app.server.data.request.FilterListRequest;
import com.app.server.data.request.FilterRequest;
import com.app.server.data.request.product.*;
import com.app.server.enums.*;
import com.app.server.response.ClientResponse;
import com.app.server.utils.JsonUtils;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import springfox.documentation.spring.web.json.Json;

import java.io.FileInputStream;
import java.util.*;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class PriceService extends BaseService {
    private BravoService bravoService;

    @Autowired
    public void setBravoService(BravoService bravoService) {
        this.bravoService = bravoService;
    }

    public ClientResponse filterProductPriceSetting(SessionData sessionData, FilterListRequest request) {
        try {
            this.addFilterAgency(sessionData, request);
            String query = this.filterUtils.getQuery(FunctionList.LIST_PRODUCT_PRICE_SETTING, request.getFilters(), request.getSorts());
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

    public ClientResponse filterProductPriceSettingDetail(FilterListByIdRequest request) {
        try {
            JSONObject setting = this.productDB.getProductPriceSetting(request.getId());
            if (setting == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            int price_object_id = ConvertUtils.toInt(setting.get("price_object_id"));
            String price_object_type = ConvertUtils.toString(setting.get("price_object_type"));

            long now = DateTimeUtils.getMilisecondsNow();
            JSONObject data = new JSONObject();
            FilterRequest filterRequest = new FilterRequest();
            filterRequest.setType("select");
            filterRequest.setKey("t.product_price_setting_id");
            filterRequest.setValue(ConvertUtils.toString(request.getId()));
            request.getFilters().add(filterRequest);
            String query = this.filterUtils.getQuery(FunctionList.LIST_PRODUCT_PRICE_SETTING_DETAIL, request.getFilters(), request.getSorts());
            List<JSONObject> records = this.productDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            for (JSONObject js : records) {
                int product_id = ConvertUtils.toInt(js
                        .get("product_id"));
                js.put("product_info", this.dataManager.getProductManager().getProductBasicData(
                        product_id
                ));

                ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(
                        ConvertUtils.toInt(js.get("product_id"))
                );

                Double current_price = this.convertProductPrice(
                        productCache.getPrice(),
                        ConvertUtils.toLong(js.get("price_original")),
                        ConvertUtils.toInt(js.get("is_auto")),
                        ConvertUtils.toString(js.get("price_setting_type")),
                        ConvertUtils.toString(js.get("price_data_type")),
                        ConvertUtils.toDouble(js.get("price_setting_value")));

                js.put("current_price", current_price);

                js.put("creator_info", this.dataManager.getStaffManager().getStaff(
                        ConvertUtils.toInt(js
                                .get("creator_id"))
                ));

                if (ConvertUtils.toInt(js.get("status")) == SettingStatus.RUNNING.getId()) {
                    ProductPriceSettingDetailEntity productPriceSettingDetailEntity =
                            ProductPriceSettingDetailEntity.from(
                                    js
                            );
                    if (productPriceSettingDetailEntity.getEnd_date() != null &&
                            productPriceSettingDetailEntity.getEnd_date().getTime() <= now) {
                        js.put("status", SettingStatus.PENDING.getId());
                        productPriceSettingDetailEntity.setStatus(
                                SettingStatus.PENDING.getId()
                        );
                        this.priceDB.updateProductPriceSettingDetail(
                                productPriceSettingDetailEntity
                        );
                    }
                }

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

    public ClientResponse getProductPriceSettingDetail(SessionData sessionData, BasicRequest request) {
        try {
            JSONObject setting = this.productDB.getProductPriceSetting(request.getId());
            if (setting == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            int agency_id = ConvertUtils.toInt(setting
                    .get("agency_id"));

            if (agency_id > 0) {
                JSONObject agencyInfo = this.dataManager.getAgencyManager().getAgencyInfo(
                        ConvertUtils.toInt(setting
                                .get("agency_id"))
                );

                if (!this.dataManager.getStaffManager().checkStaffManageAgency(
                        sessionData.getId(),
                        agencyInfo)) {
                    return ClientResponse.fail(ResponseStatus.NOT_PERMISSION, ResponseMessage.USER_FORBIDDEN);
                }

                setting.put("agency_info", agencyInfo);
                setting.put("creator_info", this.dataManager.getStaffManager().getStaff(
                        ConvertUtils.toInt(setting
                                .get("creator_id"))
                ));
            }

            JSONObject data = new JSONObject();
            data.put("product_price_setting", setting);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse createProductPriceSetting(SessionData sessionData, CreateProductPriceSettingRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            if (request.getPrice_object_type().equals("AGENCY")) {
                if (!this.dataManager.getStaffManager().checkStaffManageAgency(
                        sessionData.getId(),
                        this.agencyDB.getAgencyInfoById(request.getPrice_object_id()))) {
                    return ClientResponse.fail(ResponseStatus.NOT_PERMISSION, ResponseMessage.USER_FORBIDDEN);
                }
            }

            JSONObject pvs = this.productDB.checkProductPriceSettingByObject(
                    request.getPrice_object_id(),
                    request.getPrice_object_type()
            );
            if (pvs != null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SETTING_DUPLICATE);
            }
            Date now = DateTimeUtils.getNow();
            ProductPriceSettingEntity productPriceSettingEntity
                    = new ProductPriceSettingEntity();
            productPriceSettingEntity.setName(request.getName());
            productPriceSettingEntity.setCreator_id(sessionData.getId());
            productPriceSettingEntity.setCreated_date(DateTimeUtils.getNow());
            productPriceSettingEntity.setStart_date(DateTimeUtils.getDateTime(request.getStart_date()));
            productPriceSettingEntity.setEnd_date(request.getEnd_date() == null ? null : DateTimeUtils.getDateTime(request.getEnd_date()));
            productPriceSettingEntity.setPrice_object_type(request.getPrice_object_type());
            productPriceSettingEntity.setPrice_object_id(request.getPrice_object_id());
            SettingObjectType settingObjectType = SettingObjectType.from(
                    request.getPrice_object_type());
            switch (settingObjectType) {
                case AGENCY:
                    productPriceSettingEntity.setAgency_id(request.getPrice_object_id());
                    break;
                case CITY:
                    productPriceSettingEntity.setCity_id(request.getPrice_object_id());
                    break;
                case REGION:
                    productPriceSettingEntity.setRegion_id(request.getPrice_object_id());
                    break;
                case MEMBERSHIP:
                    productPriceSettingEntity.setMembership_id(request.getPrice_object_id());
                    break;
            }
            productPriceSettingEntity.setStatus(SettingStatus.DRAFT.getId());

            int rsInsertSetting = this.priceDB.insertProductPriceSetting(productPriceSettingEntity);
            if (rsInsertSetting <= 0) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            productPriceSettingEntity.setId(rsInsertSetting);

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
                        productPriceSettingEntity.getId());
                if (productPriceSettingDetailRequest.getStart_date() == null || productPriceSettingDetailRequest.getStart_date() == 0) {
                    productPriceSettingDetailEntity.setStart_date(
                            productPriceSettingEntity.getStart_date());
                } else {
                    productPriceSettingDetailEntity.setStart_date(
                            DateTimeUtils.getDateTime(productPriceSettingDetailRequest.getStart_date()
                            ));
                }

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

                if (productPriceSettingDetailEntity.getStart_date().after(now)) {
                    productPriceSettingDetailEntity.setStatus(SettingStatus.ACTIVE.getId());
                } else if (productPriceSettingDetailEntity.getEnd_date() == null || productPriceSettingDetailEntity.getEnd_date().after(now)) {
                    productPriceSettingDetailEntity.setStatus(SettingStatus.RUNNING.getId());
                } else {
                    productPriceSettingDetailEntity.setStatus(SettingStatus.PENDING.getId());
                }

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

                productPriceSettingDetailEntity.setPrice_original(
                        ConvertUtils.toLong(
                                productCache.getPrice()));
                productPriceSettingDetailEntity.setPrice_new(
                        ConvertUtils.toLong(this.convertProductPrice(
                                productCache.getPrice(),
                                productCache.getPrice(),
                                productPriceSettingDetailEntity.getIs_auto(),
                                productPriceSettingDetailEntity.getPrice_setting_type(),
                                productPriceSettingDetailEntity.getPrice_data_type(),
                                productPriceSettingDetailEntity.getPrice_setting_value()
                        )));

                JSONObject jsSettingDetail = JsonUtils.DeSerialize(
                        JsonUtils.Serialize(productPriceSettingDetailEntity),
                        JSONObject.class);

                int rsInsertDetail =
                        this.priceDB.insertProductPriceSettingDetail(
                                productPriceSettingDetailEntity);
                if (rsInsertDetail <= 0) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
                productPriceSettingDetailEntity.setId(rsInsertDetail);

                /**
                 * Lưu lịch sử
                 */
                this.saveProductPriceSettingDetailHistory(sessionData,
                        jsSettingDetail,
                        productPriceSettingEntity.getStatus(),
                        productPriceSettingDetailEntity.getStart_date(),
                        productPriceSettingDetailEntity.getEnd_date());
            }

            JSONObject data = new JSONObject();
            data.put("id", productPriceSettingEntity.getId());
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private void saveProductPriceSettingDetailHistory(
            SessionData sessionData,
            JSONObject settingDetail,
            int status,
            Date start_date,
            Date end_date) {
        try {
            ProductPriceSettingDetailHistoryEntity historyEntity =
                    this.createProductPriceSettingDetailHistory(sessionData, settingDetail);
            if (historyEntity == null) {
                /**
                 * Thong bao qua tele
                 */
                return;
            }
            historyEntity.setId(null);
            historyEntity.setStart_date(start_date);
            historyEntity.setEnd_date(end_date);
            historyEntity.setStatus(status);
            historyEntity.setCreator_id(sessionData.getId());
            int rsInsert = this.priceDB.insertProductPriceSettingDetailHistory(historyEntity);
            if (rsInsert <= 0) {
                /**
                 * Thong bao qua tele
                 */
                this.alertToTelegram("[PRICE] Lưu lịch sử thất bại",
                        ResponseStatus.FAIL);
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
    }

    private ProductPriceSettingDetailHistoryEntity createProductPriceSettingDetailHistory(
            SessionData sessionData,
            JSONObject settingDetail
    ) {
        try {
            ProductPriceSettingDetailHistoryEntity historyEntity
                    = JsonUtils.DeSerialize(JsonUtils.Serialize(settingDetail), ProductPriceSettingDetailHistoryEntity.class);
            historyEntity.setCreator_id(sessionData.getId());
            historyEntity.setCreated_date(DateTimeUtils.getNow());
            return historyEntity;
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return null;
    }

    public ClientResponse editProductPriceSetting(SessionData sessionData, EditProductPriceSettingRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            JSONObject pvs = this.productDB.checkProductPriceSettingByObject(
                    request.getPrice_object_id(),
                    request.getPrice_object_type()
            );
            if (pvs != null && ConvertUtils.toInt(pvs.get("id")) != request.getId()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SETTING_DUPLICATE);
            }

            ProductPriceSettingEntity productPriceSettingEntity
                    = this.priceDB.getProductPriceSettingEntity(request.getId());
            if (productPriceSettingEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            Date now = DateTimeUtils.getNow();

            if (productPriceSettingEntity.getStatus() == SettingStatus.ACTIVE.getId() &&
                    request.getStart_date() < now.getTime()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.TIME_BEGIN_INVALID);
            }

            productPriceSettingEntity.setName(request.getName());
            productPriceSettingEntity.setPrice_object_type(request.getPrice_object_type());
            productPriceSettingEntity.setPrice_object_id(request.getPrice_object_id());
            productPriceSettingEntity.setStart_date(
                    DateTimeUtils.getDateTime(request.getStart_date()));
            productPriceSettingEntity.setEnd_date(request.getEnd_date() == null ? null : DateTimeUtils.getDateTime(request.getEnd_date()));
            productPriceSettingEntity.setPrice_object_type(request.getPrice_object_type());
            productPriceSettingEntity.setPrice_object_id(request.getPrice_object_id());
            SettingObjectType visibilityObjectType = SettingObjectType.from(request.getPrice_object_type());
            switch (visibilityObjectType) {
                case AGENCY:
                    productPriceSettingEntity.setAgency_id(request.getPrice_object_id());
                    break;
                case CITY:
                    productPriceSettingEntity.setCity_id(request.getPrice_object_id());
                    break;
                case REGION:
                    productPriceSettingEntity.setRegion_id(request.getPrice_object_id());
                    break;
                case MEMBERSHIP:
                    productPriceSettingEntity.setMembership_id(request.getPrice_object_id());
                    break;
            }

            /**
             *
             */
            if (productPriceSettingEntity.getStatus() == SettingStatus.ACTIVE.getId()) {
                if (productPriceSettingEntity.getStart_date().before(DateTimeUtils.getNow())) {
                    productPriceSettingEntity.setStart_date(DateTimeUtils.getNow());
                    productPriceSettingEntity.setStatus(SettingStatus.RUNNING.getId());
                }
            }

            boolean rsUpdateSetting = this.priceDB.updateProductPriceSetting(productPriceSettingEntity);
            if (!rsUpdateSetting) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            for (ProductPriceSettingDetailRequest productPriceSettingDetailRequest : request.getRecords()) {
                ProductPriceSettingDetailEntity productPriceSettingDetailEntity;
                boolean isAddNew;
                if (productPriceSettingDetailRequest.getId() != 0) {
                    isAddNew = false;
                    productPriceSettingDetailEntity =
                            this.priceDB.getProductPriceSettingDetailEntity(
                                    productPriceSettingDetailRequest.getId()
                            );
                    if (productPriceSettingDetailEntity == null) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SETTING_DATA_TYPE_INVALID);
                    }
                } else {
                    isAddNew = true;
                    productPriceSettingDetailEntity = new ProductPriceSettingDetailEntity();
                    productPriceSettingDetailEntity.setStatus(
                            productPriceSettingDetailRequest.getStart_date() == null ?
                                    productPriceSettingEntity.getStatus() :
                                    productPriceSettingDetailRequest.getStart_date() > now.getTime() ? SettingStatus.ACTIVE.getId() :
                                            SettingStatus.RUNNING.getId()
                    );
                }

                productPriceSettingDetailEntity.setProduct_price_setting_id(productPriceSettingEntity.getId());
                if (productPriceSettingDetailRequest.getStart_date() == null || productPriceSettingDetailRequest.getStart_date() == 0) {
                    productPriceSettingDetailEntity.setStart_date(
                            productPriceSettingEntity.getStart_date());
                } else {
                    productPriceSettingDetailEntity.setStart_date(
                            DateTimeUtils.getDateTime(productPriceSettingDetailRequest.getStart_date()
                            ));
                }

                productPriceSettingDetailEntity.setEnd_date(
                        productPriceSettingDetailRequest.getEnd_date() == null ? null :
                                DateTimeUtils.getDateTime(productPriceSettingDetailRequest.getEnd_date())
                );

                if (productPriceSettingDetailEntity.getStart_date().after(now)) {
                    productPriceSettingDetailEntity.setStatus(SettingStatus.ACTIVE.getId());
                } else if (productPriceSettingDetailEntity.getEnd_date() == null ||
                        productPriceSettingDetailEntity.getEnd_date().after(now)) {
                    productPriceSettingDetailEntity.setStatus(SettingStatus.RUNNING.getId());
                } else {
                    productPriceSettingDetailEntity.setStatus(SettingStatus.PENDING.getId());
                }

                ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(
                        productPriceSettingDetailRequest.getProduct_id());
                if (productCache == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                productPriceSettingDetailEntity.setProduct_id(productCache.getId());
                productPriceSettingDetailEntity.setCreator_id(sessionData.getId());

                productPriceSettingDetailEntity.setCreated_date(DateTimeUtils.getNow());
                productPriceSettingDetailEntity.setPrice_setting_type(
                        productPriceSettingDetailRequest.getPrice_setting_type()
                );
                productPriceSettingDetailEntity.setPrice_data_type(
                        productPriceSettingDetailRequest.getPrice_data_type()
                );
                productPriceSettingDetailEntity.setPrice_setting_value(
                        productPriceSettingDetailRequest.getPrice_setting_value()
                );
                productPriceSettingDetailEntity.setMinimum_purchase(
                        productPriceSettingDetailRequest.getMinimum_purchase()
                );
                productPriceSettingDetailEntity.setIs_auto(
                        productPriceSettingDetailRequest.getIs_auto()
                );
                productPriceSettingDetailEntity.setPrice_original(
                        ConvertUtils.toLong(productCache.getPrice()));
                productPriceSettingDetailEntity.setPrice_new(
                        ConvertUtils.toLong(this.convertProductPrice(
                                productCache.getPrice(),
                                productCache.getPrice(),
                                productPriceSettingDetailRequest.getIs_auto(),
                                productPriceSettingDetailRequest.getPrice_setting_type(),
                                productPriceSettingDetailRequest.getPrice_data_type(),
                                productPriceSettingDetailRequest.getPrice_setting_value()
                        ))
                );

                JSONObject jsSettingDetail =
                        JsonUtils.DeSerialize(
                                JsonUtils.Serialize(this.convertProductPriceSettingDetail(productPriceSettingDetailEntity)),
                                JSONObject.class);

                if (isAddNew) {
                    int rsInsertDetail =
                            this.priceDB.insertProductPriceSettingDetail(productPriceSettingDetailEntity);
                    if (rsInsertDetail <= 0) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }
                    productPriceSettingDetailEntity.setId(rsInsertDetail);
                } else {
                    boolean rsUpdateDetail =
                            this.priceDB.updateProductPriceSettingDetail(productPriceSettingDetailEntity);
                    if (!rsUpdateDetail) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }
                }

                /**
                 * Lưu lịch sử
                 */
                this.saveProductPriceSettingDetailHistory(sessionData,
                        jsSettingDetail,
                        productPriceSettingEntity.getStatus(),
                        productPriceSettingDetailEntity.getStart_date(),
                        productPriceSettingDetailEntity.getEnd_date());

            }

            if (SettingStatus.RUNNING.getId() == productPriceSettingEntity.getStatus()) {
                this.dataManager.callReloadProductPriceSetting(
                        request.getId()
                );

                this.syncPriceSettingToBravo(
                        request.getId(),
                        DateTimeUtils.getNow()
                );
            }

            JSONObject data = new JSONObject();
            data.put("id", productPriceSettingEntity.getId());
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ProductPriceSettingDetailEntity convertProductPriceSettingDetail(ProductPriceSettingDetailEntity productPriceSettingDetailEntity) {
        ProductPriceSettingDetailEntity result = new ProductPriceSettingDetailEntity();
//        private Integer id;
        result.setId(
                productPriceSettingDetailEntity.getId());
//        private int creator_id;
        result.setCreator_id(
                productPriceSettingDetailEntity.getCreator_id());
//        private Date created_date;
        result.setCreated_date(
                productPriceSettingDetailEntity.getCreated_date());
//        private Integer modifier_id;
        result.setModifier_id(
                productPriceSettingDetailEntity.getModifier_id());
//        private Date modified_date;
        result.setModified_date(
                productPriceSettingDetailEntity.getModified_date());
//        private Integer product_id;
        result.setProduct_id(
                productPriceSettingDetailEntity.getProduct_id());
//        private int status = ProductVisibilitySettingStatus.DRAFT.getId();
        result.setStatus(
                productPriceSettingDetailEntity.getStatus());
//        private Date start_date;
        result.setStart_date(
                productPriceSettingDetailEntity.getStart_date());
//        private Date end_date;
        result.setEnd_date(
                productPriceSettingDetailEntity.getEnd_date());
//        private int product_price_setting_id;
        result.setProduct_price_setting_id(
                productPriceSettingDetailEntity.getProduct_price_setting_id());
//        private String price_setting_type;
        result.setPrice_setting_type(
                productPriceSettingDetailEntity.getPrice_setting_type());
//        private String price_data_type;
        result.setPrice_data_type(
                productPriceSettingDetailEntity.getPrice_data_type());
//        private double price_setting_value;
        result.setPrice_setting_value(
                productPriceSettingDetailEntity.getPrice_setting_value());
//        private int is_auto = 1;
        result.setIs_auto(
                productPriceSettingDetailEntity.getIs_auto());
//        private int minimum_purchase = 0;
        result.setMinimum_purchase(
                productPriceSettingDetailEntity.getMinimum_purchase());
        return result;
    }

    public ClientResponse activeProductPriceSetting(SessionData sessionData, ActiveProductPriceSettingRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }
            String message = "";

            for (int iSetting = 0; iSetting < request.getIds().size(); iSetting++) {
                Integer id = request.getIds().get(iSetting);
                if (id == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                ProductPriceSettingEntity setting = this.priceDB.getProductPriceSettingEntity(id);
                if (setting == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SETTING_NOT_FOUND);
                }

                int status = setting.getStatus();
                if (!(SettingStatus.DRAFT.getId() == status ||
                        SettingStatus.PENDING.getId() == status)) {
                    ClientResponse crStatus = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
                    message += "\n [Thiết lập thứ " + (iSetting + 1) + "] " + crStatus.getMessage();
                    continue;
                }

                ClientResponse crActiveSetting = this.activeProductPriceSettingOne(sessionData, setting);
                if (crActiveSetting.failed()) {
                    clientResponse = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    message += "\n [Thiết lập thứ " + (iSetting + 1) + "] " + crActiveSetting.getMessage();
                }
            }

            if (clientResponse.failed()) {
                clientResponse.setMessage(message);
                return clientResponse;
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse activeProductPriceSettingOne(SessionData sessionData, ProductPriceSettingEntity setting) {
        try {
            if (SettingStatus.DRAFT.getId() == setting.getStatus()
                    && setting.getEnd_date() != null
                    && setting.getEnd_date().before(DateTimeUtils.getNow())) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.TIME_END_INVALID);
            }

            Date now = DateTimeUtils.getNow();

            if (setting.getStatus() == SettingStatus.DRAFT.getId() &&
                    setting.getStart_date().getTime() < now.getTime()) {
                setting.setStart_date(now);
            }

            List<JSONObject> settingDetailList = this.priceDB.getAllProductPriceSettingDetail(
                    setting.getId()
            );

            if (setting.getStart_date().getTime() > now.getTime()) {
                /**
                 * Kích hoạt và chờ chạy
                 */
                if (SettingStatus.PENDING.getId() == setting.getStatus() &&
                        setting.getEnd_date() != null &&
                        setting.getEnd_date().getTime() < DateTimeUtils.getMilisecondsNow()) {
                    setting.setEnd_date(
                            null
                    );
                }

                setting.setModified_date(DateTimeUtils.getNow());
                setting.setModifier_id(sessionData.getId());
                setting.setStatus(SettingStatus.ACTIVE.getId());
                boolean rsUpdateSetting = this.priceDB.updateProductPriceSetting(setting);
                if (!rsUpdateSetting) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                for (JSONObject settingDetail : settingDetailList) {
                    /**
                     * Lưu lịch sử
                     */
                    this.saveProductPriceSettingDetailHistory(sessionData,
                            settingDetail,
                            setting.getStatus(),
                            setting.getStart_date(),
                            setting.getEnd_date()
                    );
                }
            } else {
                /**
                 * Run thiết lập
                 */
                if (SettingStatus.PENDING.getId() == setting.getStatus() &&
                        setting.getEnd_date() != null &&
                        setting.getEnd_date().getTime() < DateTimeUtils.getMilisecondsNow()) {
                    setting.setEnd_date(
                            null
                    );
                }

                setting.setStart_date(DateTimeUtils.getNow());
                setting.setModified_date(DateTimeUtils.getNow());
                setting.setModifier_id(sessionData.getId());
                setting.setStatus(SettingStatus.RUNNING.getId());
                boolean rsUpdateSetting = this.priceDB.updateProductPriceSetting(setting);
                if (!rsUpdateSetting) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                for (JSONObject settingDetail : settingDetailList) {
                    ProductPriceSettingDetailEntity productPriceSettingDetailEntity =
                            ProductPriceSettingDetailEntity.from(settingDetail);
                    if (productPriceSettingDetailEntity == null) {
                        continue;
                    }

                    if (productPriceSettingDetailEntity.getStatus() == SettingStatus.RUNNING.getId()) {
                        if (productPriceSettingDetailEntity.getStart_date() != null &&
                                productPriceSettingDetailEntity.getStart_date().before(
                                        setting.getStart_date()
                                )) {
                            productPriceSettingDetailEntity.setStart_date(setting.getStart_date());
                        }
                    }

                    if (productPriceSettingDetailEntity.getStart_date().after(now)) {
                        productPriceSettingDetailEntity.setStatus(SettingStatus.ACTIVE.getId());
                    } else if (productPriceSettingDetailEntity.getEnd_date() == null || productPriceSettingDetailEntity.getEnd_date().after(now)) {
                        productPriceSettingDetailEntity.setStatus(SettingStatus.RUNNING.getId());
                    } else {
                        productPriceSettingDetailEntity.setStatus(SettingStatus.PENDING.getId());
                    }

                    this.priceDB.updateProductPriceSettingDetail(productPriceSettingDetailEntity);

                    /**
                     * Lưu lịch sử
                     */
                    this.saveProductPriceSettingDetailHistory(
                            sessionData,
                            settingDetail,
                            productPriceSettingDetailEntity.getStatus(),
                            productPriceSettingDetailEntity.getStart_date(),
                            productPriceSettingDetailEntity.getEnd_date());
                }
            }

            if (setting.getStatus() == SettingStatus.RUNNING.getId()
                    && !settingDetailList.isEmpty()) {
                this.dataManager.callReloadProductPriceSetting(setting.getId());
                ClientResponse crSyncPriceSetting = this.syncPriceSettingToBravo(
                        setting.getId(),
                        now
                );
                if (crSyncPriceSetting.failed()) {
                    this.priceDB.syncPriceSettingFailed(
                            setting.getId(),
                            crSyncPriceSetting.getMessage()
                    );
                } else {
                    this.priceDB.syncPriceSettingSuccess(
                            setting.getId()
                    );
                }
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse activeProductPriceSettingDetail(SessionData sessionData, ActiveProductPriceSettingRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }
            String message = "";
            List<Object> records = new ArrayList<>();
            for (int iSetting = 0; iSetting < request.getIds().size(); iSetting++) {
                Integer id = request.getIds().get(iSetting);
                if (id == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                ClientResponse crActiveSetting = this.activeProductPriceSettingDetailOne(sessionData, id);
                if (crActiveSetting.failed()) {
                    clientResponse = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    message += "\n [Thiết lập thứ " + (iSetting + 1) + "] " + crActiveSetting.getMessage();
                }

                return crActiveSetting;
            }

            if (clientResponse.failed()) {
                clientResponse.setMessage(message);
                return clientResponse;
            }
            JSONObject data = new JSONObject();
            data.put("records", records);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse activeProductPriceSettingDetailOne(SessionData sessionData, Integer id) {
        try {
            ProductPriceSettingDetailEntity settingDetailEntity = this.priceDB.getProductPriceSettingDetailEntity(id);
            if (settingDetailEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SETTING_NOT_FOUND);
            }

            int status = settingDetailEntity.getStatus();

            if (settingDetailEntity.getStart_date().getTime() > DateTimeUtils.getMilisecondsNow()) {
                /**
                 * Kích hoạt và chờ chạy
                 */
                if (SettingStatus.PENDING.getId() == status) {
                    settingDetailEntity.setEnd_date(null);
                }

                settingDetailEntity.setModified_date(DateTimeUtils.getNow());
                settingDetailEntity.setModifier_id(sessionData.getId());
                settingDetailEntity.setStatus(SettingStatus.ACTIVE.getId());
                boolean rsUpdateDetail = this.priceDB.updateProductPriceSettingDetail(
                        settingDetailEntity);
                if (!rsUpdateDetail) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }


                /**
                 * Lưu lịch sử
                 */
                this.saveProductPriceSettingDetailHistory(
                        sessionData,
                        JsonUtils.DeSerialize(
                                JsonUtils.Serialize(settingDetailEntity), JSONObject.class),
                        settingDetailEntity.getStatus(),
                        settingDetailEntity.getStart_date(),
                        settingDetailEntity.getEnd_date());
            } else {
                /**
                 * Run thiết lập
                 */
                if (SettingStatus.PENDING.getId() == status) {
                    settingDetailEntity.setEnd_date(null);
                }
                settingDetailEntity.setStart_date(DateTimeUtils.getNow());
                settingDetailEntity.setModified_date(DateTimeUtils.getNow());
                settingDetailEntity.setModifier_id(sessionData.getId());
                settingDetailEntity.setStatus(SettingStatus.RUNNING.getId());
                boolean rsUpdateDetail = this.priceDB.updateProductPriceSettingDetail(settingDetailEntity);
                if (settingDetailEntity == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                /**
                 * Lưu lịch sử
                 */
                this.saveProductPriceSettingDetailHistory(
                        sessionData,
                        JsonUtils.DeSerialize(
                                JsonUtils.Serialize(settingDetailEntity), JSONObject.class),
                        settingDetailEntity.getStatus(),
                        settingDetailEntity.getStart_date(),
                        settingDetailEntity.getEnd_date());
            }

            this.dataManager.callReloadProductPriceSetting(
                    settingDetailEntity.getProduct_price_setting_id());
            JSONObject data = new JSONObject();
            data.put("record", settingDetailEntity);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse deactivateProductPriceSetting(SessionData sessionData, ActiveProductPriceSettingRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }
            String message = "";
            List<Object> records = new ArrayList<>();
            for (int iSetting = 0; iSetting < request.getIds().size(); iSetting++) {
                Integer id = request.getIds().get(iSetting);
                if (id == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                ClientResponse crActiveSetting = this.deactivateProductPriceSettingOne(sessionData, id);
                if (crActiveSetting.failed()) {
                    clientResponse = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    message += "\n [Thiết lập thứ " + (iSetting + 1) + "] " + crActiveSetting.getMessage();
                }
                records.add(crActiveSetting.getData());
            }

            if (clientResponse.failed()) {
                clientResponse.setMessage(message);
                return clientResponse;
            }
            JSONObject data = new JSONObject();
            data.put("records", records);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse deactivateProductPriceSettingOne(SessionData sessionData, Integer id) {
        try {
            ProductPriceSettingEntity setting = this.priceDB.getProductPriceSettingEntity(id);
            if (setting == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SETTING_NOT_FOUND);
            }
            int status = setting.getStatus();
            if (!(SettingStatus.RUNNING.getId() == status
                    || SettingStatus.ACTIVE.getId() == status)) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            setting.setEnd_date(DateTimeUtils.getNow());
            if (setting.getStart_date().after(setting.getEnd_date())) {
                setting.setStart_date(setting.getEnd_date());
            }

            setting.setModified_date(DateTimeUtils.getNow());
            setting.setModifier_id(sessionData.getId());
            setting.setStatus(SettingStatus.PENDING.getId());
            boolean rsUpdateSetting = this.priceDB.updateProductPriceSetting(setting);
            if (!rsUpdateSetting) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            List<JSONObject> settingDetailList = this.priceDB.getListProductPriceSettingDetailNotPending(id);
            for (JSONObject settingDetail : settingDetailList) {
                this.priceDB.deactiveProductPriceSettingDetail(
                        ConvertUtils.toInt(settingDetail.get("id"))
                );

                /**
                 * Lưu lịch sử
                 */
                this.saveProductPriceSettingDetailHistory(
                        sessionData,
                        settingDetail,
                        SettingStatus.PENDING.getId(),
                        setting.getStart_date(),
                        setting.getEnd_date());
            }

            List<JSONObject> allSettingDetail = this.priceDB.getAllProductPriceSettingDetail(id);

            if (status == SettingStatus.RUNNING.getId()
                    && !allSettingDetail.isEmpty()) {
                this.dataManager.callReloadProductPriceSetting(id);
                ClientResponse crSyncPriceSetting = this.syncPriceSettingToBravo(
                        setting.getId(),
                        DateTimeUtils.getNow()
                );
                if (crSyncPriceSetting.failed()) {
                    this.priceDB.syncPriceSettingFailed(
                            setting.getId(),
                            crSyncPriceSetting.getMessage()
                    );
                } else {
                    this.priceDB.syncPriceSettingSuccess(
                            setting.getId()
                    );
                }
            }

            JSONObject data = new JSONObject();
            data.put("record", setting);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse deactivateProductPriceSettingDetail(SessionData sessionData, ActiveProductPriceSettingRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }
            String message = "";

            for (int iSetting = 0; iSetting < request.getIds().size(); iSetting++) {
                Integer id = request.getIds().get(iSetting);
                if (id == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                ClientResponse crActiveSetting = this.deactivateProductPriceSettingDetailOne(sessionData, id);
                if (crActiveSetting.failed()) {
                    clientResponse = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    message += "\n [Thiết lập thứ " + (iSetting + 1) + "] " + crActiveSetting.getMessage();
                }

                return crActiveSetting;
            }

            if (clientResponse.failed()) {
                clientResponse.setMessage(message);
                return clientResponse;
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse deactivateProductPriceSettingDetailOne(
            SessionData sessionData,
            Integer id) {
        try {
            ProductPriceSettingDetailEntity settingDetailEntity = this.priceDB.getProductPriceSettingDetailEntity(id);
            if (settingDetailEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SETTING_NOT_FOUND);
            }
            int status = settingDetailEntity.getStatus();
            if (!(SettingStatus.RUNNING.getId() == status
                    || SettingStatus.ACTIVE.getId() == status)) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            settingDetailEntity.setEnd_date(DateTimeUtils.getNow());
            if (settingDetailEntity.getStart_date().after(settingDetailEntity.getEnd_date())) {
                settingDetailEntity.setStart_date(settingDetailEntity.getEnd_date());
            }

            settingDetailEntity.setModified_date(DateTimeUtils.getNow());
            settingDetailEntity.setModifier_id(sessionData.getId());
            settingDetailEntity.setStatus(SettingStatus.PENDING.getId());
            boolean rsUpdateDetail = this.priceDB.updateProductPriceSettingDetail(
                    settingDetailEntity);
            if (!rsUpdateDetail) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            JSONObject jsSettingDetail = this.priceDB.getProductPriceSettingDetail(id);
            if (jsSettingDetail == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            /**
             * Lưu lịch sử
             */
            this.saveProductPriceSettingDetailHistory(
                    sessionData,
                    jsSettingDetail,
                    SettingStatus.PENDING.getId(),
                    settingDetailEntity.getStart_date(),
                    settingDetailEntity.getEnd_date());

            this.dataManager.callReloadProductPriceSetting(
                    settingDetailEntity.getProduct_price_setting_id());
            JSONObject data = new JSONObject();
            data.put("record", settingDetailEntity);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterProductPriceByAgency(FilterListByIdRequest request) {
        try {
            JSONObject agency = this.agencyDB.getAgencyInfoById(request.getId());
            if (agency == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
            }

            String query = this.filterUtils.getQuery(FunctionList.LIST_PRODUCT, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.productDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            for (JSONObject js : records) {
                int id = ConvertUtils.toInt(js.get("id"));
                ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(
                        id);
                ProductCache membership_price = this.getProductPriceByObject(
                        productCache.getPrice(),
                        SettingObjectType.MEMBERSHIP,
                        ConvertUtils.toInt(agency.get("membership_id")),
                        productCache.getId());
                js.put("membership_price",
                        membership_price != null ? membership_price.getPrice() : null
                );
                ProductCache region_price = this.getProductPriceByObject(
                        productCache.getPrice(),
                        SettingObjectType.REGION,
                        ConvertUtils.toInt(agency.get("region_id")),
                        productCache.getId());
                js.put("region_price",
                        region_price != null ? region_price.getPrice() : null);

                ProductCache city_price = this.getProductPriceByObject(
                        productCache.getPrice(),
                        SettingObjectType.CITY,
                        ConvertUtils.toInt(agency.get("city_id")),
                        productCache.getId());
                js.put("city_price",
                        city_price != null ? city_price.getPrice() : null
                );
                ProductCache agency_price = this.getProductPriceByObject(
                        productCache.getPrice(),
                        SettingObjectType.AGENCY,
                        request.getId(),
                        productCache.getId());
                js.put("agency_price",
                        agency_price != null ? agency_price.getPrice() : null);
                js.put("current_price",
                        this.getFinalPriceByAgency(
                                productCache.getId(),
                                request.getId(),
                                ConvertUtils.toInt(agency.get("city_id")),
                                ConvertUtils.toInt(agency.get("region_id")),
                                ConvertUtils.toInt(agency.get("membership_id"))
                        ).getPrice());
                js.put("product_info",
                        productCache);
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

    public ClientResponse filterSettingPrice(FilterListByIdRequest request) {
        try {
            ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(request.getId());
            if (productCache == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PRODUCT_INVALID);
            }
            FilterRequest filterRequest = new FilterRequest();
            filterRequest.setKey("product_id");
            filterRequest.setType("select");
            filterRequest.setValue(ConvertUtils.toString(request.getId()));
            request.getFilters().add(filterRequest);
            String query = this.filterUtils.getQuery(FunctionList.LIST_SETTING_PRICE, request.getFilters(), request.getSorts());
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
                js.put("price_original", productCache.getPrice());

                JSONObject settingDetail = this.priceDB.getProductPriceSettingDetailByProduct(
                        request.getId(),
                        ConvertUtils.toInt(js.get("id")));
                js.put("price_new", this.convertProductPrice(
                        productCache.getPrice(),
                        ConvertUtils.toLong(settingDetail.get("price_original")),
                        ConvertUtils.toInt(settingDetail.get("is_auto")),
                        ConvertUtils.toString(settingDetail.get("price_setting_type")),
                        ConvertUtils.toString(settingDetail.get("price_data_type")),
                        ConvertUtils.toDouble(settingDetail.get("price_setting_value")))
                );
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

    public ClientResponse filterProductPriceSettingDetailHistory(FilterListProductPriceBySettingHistoryRequest request) {
        try {
            ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(
                    request.getProduct_id());
            if (productCache == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PRODUCT_INVALID);
            }

            /**
             * Product
             */
            FilterRequest filterProductRequest = new FilterRequest();
            filterProductRequest.setKey("t.product_id");
            filterProductRequest.setType("select");
            filterProductRequest.setValue(ConvertUtils.toString(request.getProduct_id()));
            request.getFilters().add(filterProductRequest);

            /**
             * setting
             */
            FilterRequest filterSettingRequest = new FilterRequest();
            filterSettingRequest.setKey("t1.agency_id");
            filterSettingRequest.setType("select");
            filterSettingRequest.setValue(ConvertUtils.toString(request.getId()));
            request.getFilters().add(filterSettingRequest);
            String query = this.filterUtils.getQuery(
                    FunctionList.LIST_PRODUCT_PRICE_SETTING_DETAIL_HISTORY, request.getFilters(), request.getSorts());
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
                js.put("price_original", productCache.getPrice());

                JSONObject settingData = JsonUtils.DeSerialize(
                        js.get("data").toString(),
                        JSONObject.class
                );

                js.put("price_new",
                        ConvertUtils.toLong(settingData.get("price_new"))
                );
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

    public ClientResponse runStartProductPriceSettingSchedule() {
        try {
            /**
             * start
             */
            List<JSONObject> settings = this.priceDB.getListProductPriceSettingNeedStart(
                    ConfigInfo.SCHEDULE_RUNNING_LIMIT);
            for (JSONObject setting : settings) {
                int id = ConvertUtils.toInt(setting.get("id"));
                ProductPriceSettingEntity settingEntity = this.priceDB.getProductPriceSettingEntity(id);
                if (settingEntity == null) {
                    continue;
                }

                ClientResponse clientResponse = this.activeProductPriceSettingOne(
                        this.dataManager.getStaffManager().getSessionStaffBot(),
                        settingEntity);
                if (clientResponse.failed()) {
                    this.alertToTelegram("[setting: " + id + "]" + clientResponse.getMessage(),
                            ResponseStatus.FAIL);
                } else {
                    LogUtil.printDebug("[setting: " + id + "]" + clientResponse.getMessage());
                }
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse runStopProductPriceSettingSchedule() {
        try {
            /**
             * Stop
             */
            List<JSONObject> settings = this.priceDB.getListProductPriceSettingNeedStop(
                    ConfigInfo.SCHEDULE_RUNNING_LIMIT);
            for (JSONObject setting : settings) {
                int id = ConvertUtils.toInt(setting.get("id"));
                ClientResponse clientResponse = this.deactivateProductPriceSettingOne(this.dataManager.getStaffManager().getSessionStaffBot(), id);
                if (clientResponse.failed()) {
                    this.alertToTelegram("[setting: " + id + "]" + clientResponse.getMessage(),
                            ResponseStatus.FAIL);
                } else {
                    LogUtil.printDebug("[setting: " + id + "]" + clientResponse.getMessage());
                }
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse importPriceSetting() {
        try {
            String filePath =
                    "/Users/lephuoc/Documents/SHT/source/BECmsServer/CODE/CmsServer/conf/price_setting.xlsx";
            FileInputStream file = new FileInputStream(filePath);
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            XSSFSheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            AgencyEntity agencyEntity = this.agencyDB.getAgencyEntityByPhone("0947221155");
            if (agencyEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            JSONObject productPriceSetting = this.priceDB.getProductPriceSettingInfoByAgencyId(
                    agencyEntity.getId()
            );
            if (productPriceSetting == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            short index = 0;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (index > 0) {
                    Iterator<Cell> cellIterator = row.cellIterator();
                    List<Cell> ltCell = new ArrayList<>();
                    cellIterator.forEachRemaining(ltCell::add);
                    if (!ltCell.isEmpty()) {
                        DeptAgencyInfoEntity deptAgencyInfoEntity = new DeptAgencyInfoEntity();

                        DeptSettingEntity deptSettingEntity = new DeptSettingEntity();

                        Cell cell = ltCell.get(1);
                        String product_code = getStringData(cell);
                        cell = ltCell.get(3);
                        String setting_type = getStringData(cell);
                        cell = ltCell.get(4);
                        String setting_data_type = getStringData(cell);
                        cell = ltCell.get(5);
                        String setting_data_value = getStringData(cell);

                        Date start_date = new Date();

                        ProductEntity product = this.productDB.getProductByCode(product_code);
                        JSONObject old = this.priceDB.getProductPriceSettingDetailByProduct(
                                product.getId(),
                                ConvertUtils.toInt(productPriceSetting.get("id"))
                        );

                        if (old != null) {
                            continue;
                        }
                        ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(
                                product.getId()
                        );
                        if (productCache == null) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PRODUCT_INVALID);
                        }
                        ProductPriceSettingDetailEntity productPriceSettingDetailEntity
                                = new ProductPriceSettingDetailEntity();
                        productPriceSettingDetailEntity.setProduct_price_setting_id(
                                ConvertUtils.toInt(productPriceSetting.get("id"))
                        );

                        productPriceSettingDetailEntity.setStart_date(start_date);

                        PriceDataType priceDataType = PriceDataType.MONEY;
                        if (priceDataType == null) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }

                        productPriceSettingDetailEntity.setCreator_id(0);

                        productPriceSettingDetailEntity.setStatus(
                                SettingStatus.RUNNING.getId()
                        );
                        productPriceSettingDetailEntity.setCreated_date(DateTimeUtils.getNow());
                        productPriceSettingDetailEntity.setPrice_data_type(
                                priceDataType.getCode()
                        );
                        productPriceSettingDetailEntity.setPrice_setting_type(
                                PriceSettingType.INCREASE.getCode()
                        );
                        productPriceSettingDetailEntity.setPrice_setting_value(
                                ConvertUtils.toDouble(setting_data_value)
                        );
                        productPriceSettingDetailEntity.setProduct_id(
                                product.getId()
                        );
                        productPriceSettingDetailEntity.setMinimum_purchase(
                                product.getMinimum_purchase()
                        );
                        productPriceSettingDetailEntity.setIs_auto(
                                YesNoStatus.YES.getValue()
                        );

                        productPriceSettingDetailEntity.setPrice_original(
                                ConvertUtils.toLong(productCache.getPrice()));
                        productPriceSettingDetailEntity.setPrice_new(
                                ConvertUtils.toLong(this.convertProductPrice(
                                        productCache.getPrice(),
                                        productCache.getPrice(),
                                        productPriceSettingDetailEntity.getIs_auto(),
                                        productPriceSettingDetailEntity.getPrice_setting_type(),
                                        productPriceSettingDetailEntity.getPrice_data_type(),
                                        productPriceSettingDetailEntity.getPrice_setting_value()
                                )));

                        JSONObject jsSettingDetail = JsonUtils.DeSerialize(
                                JsonUtils.Serialize(productPriceSettingDetailEntity),
                                JSONObject.class);

                        int rsInsertDetail =
                                this.priceDB.insertProductPriceSettingDetail(
                                        productPriceSettingDetailEntity);
                        if (rsInsertDetail <= 0) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }
                        productPriceSettingDetailEntity.setId(rsInsertDetail);

                        /**
                         * Lưu lịch sử
                         */
                        this.saveProductPriceSettingDetailHistory(new SessionData(),
                                jsSettingDetail,
                                productPriceSettingDetailEntity.getStatus(),
                                productPriceSettingDetailEntity.getStart_date(),
                                productPriceSettingDetailEntity.getEnd_date());
                    }
                }
                index++;
            }
            file.close();

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterProductPriceTimer(FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.LIST_PRODUCT_PRICE_TIMER, request.getFilters(), request.getSorts());
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

    public ClientResponse createProductPriceTimer(
            SessionData sessionData,
            CreateProductPriceTimerRequest request) {
        try {
            ClientResponse crValidate = request.validate();
            if (crValidate.failed()) {
                return crValidate;
            }

            ProductPriceTimerEntity entity = new ProductPriceTimerEntity();
            entity.setName(request.getName());
            entity.setNote(request.getNote());
            entity.setCreator_id(sessionData.getId());
            entity.setCreated_date(DateTimeUtils.getNow());
            entity.setModifier_id(sessionData.getId());
            entity.setModified_date(DateTimeUtils.getNow());
            entity.setStart_date(DateTimeUtils.getDateTime(request.getStart_date_millisecond()));
            entity.setStatus(ProductPriceTimerStatus.DRAFT.getId());

            int rsInsertSetting = this.priceDB.insertProductPriceTimer(entity);
            if (rsInsertSetting <= 0) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            entity.setId(rsInsertSetting);

            for (ProductPriceTimerRequest productPriceTimerRequest : request.getProducts()) {
                ProductPriceTimerDetailEntity productPriceTimerDetailEntity = new ProductPriceTimerDetailEntity();
                productPriceTimerDetailEntity.setProduct_price_timer_id(rsInsertSetting);
                productPriceTimerDetailEntity.setProduct_id(productPriceTimerRequest.getId());
                productPriceTimerDetailEntity.setPrice(productPriceTimerRequest.getPrice());
                productPriceTimerDetailEntity.setNote(productPriceTimerRequest.getNote());
                int rsInsertDetail = this.priceDB.insertProductPriceTimerDetail(productPriceTimerDetailEntity);
                if (rsInsertDetail <= 0) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
            }

            JSONObject data = new JSONObject();
            data.put("id", rsInsertSetting);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse editProductPriceTimer(
            SessionData sessionData,
            EditProductPriceTimerRequest request) {
        try {
            ClientResponse crValidate = request.validate();
            if (crValidate.failed()) {
                return crValidate;
            }

            ProductPriceTimerEntity entity = this.priceDB.getProductPriceTimerEntity(request.getId());
            if (entity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SETTING_NOT_FOUND);
            }

            if (entity.getStatus() != ProductPriceTimerStatus.WAITING.getId() &&
                    entity.getStatus() != ProductPriceTimerStatus.DRAFT.getId()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            entity.setName(request.getName());
            entity.setNote(request.getNote());
            entity.setModifier_id(sessionData.getId());
            entity.setModified_date(DateTimeUtils.getNow());
            entity.setStart_date(DateTimeUtils.getDateTime(request.getStart_date_millisecond()));

            boolean rsUpdate = this.priceDB.updateProductPriceTimer(entity);
            if (!rsUpdate) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            boolean rsDeleteDetail = this.priceDB.deleteProductPriceTimerDetail(request.getId());

            for (int iProduct = 0; iProduct < request.getProducts().size(); iProduct++) {
                ProductPriceTimerRequest productPriceTimerRequest = request.getProducts().get(iProduct);
                ProductPriceTimerDetailEntity productPriceTimerDetailEntity = new ProductPriceTimerDetailEntity();
                productPriceTimerDetailEntity.setProduct_price_timer_id(entity.getId());
                productPriceTimerDetailEntity.setProduct_id(productPriceTimerRequest.getId());
                productPriceTimerDetailEntity.setPrice(productPriceTimerRequest.getPrice());
                productPriceTimerDetailEntity.setNote(productPriceTimerRequest.getNote());
                int rsInsertDetail = this.priceDB.insertProductPriceTimerDetail(productPriceTimerDetailEntity);
                if (rsInsertDetail <= 0) {
                    ClientResponse clientResponse = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    clientResponse.setMessage("[Thứ " + (iProduct + 1) + "]" + clientResponse.getMessage());
                    return clientResponse;
                }
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterProductPriceTimerDetail(FilterListByIdRequest request) {
        try {
            FilterRequest filterSettingRequest = new FilterRequest();
            filterSettingRequest.setKey("t.product_price_timer_id");
            filterSettingRequest.setType("select");
            filterSettingRequest.setValue(ConvertUtils.toString(request.getId()));
            request.getFilters().add(filterSettingRequest);
            String query = this.filterUtils.getQuery(FunctionList.LIST_PRODUCT_PRICE_TIMER_DETAIL, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.productDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            for (JSONObject js : records) {
                int id = ConvertUtils.toInt(js.get("product_id"));
                ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(
                        id);
                js.put("product_info",
                        productCache);
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

    public ClientResponse getProductPriceTimerDetail(BasicRequest request) {
        try {
            JSONObject setting =
                    this.priceDB.getProductPriceTimerInfo(request.getId());
            if (setting == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SETTING_NOT_FOUND);
            }

            setting.put("creator_info", this.dataManager.getStaffManager().getStaff(
                    ConvertUtils.toInt(setting.get("creator_id"))
            ));
            JSONObject data = new JSONObject();
            data.put("record", setting);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse cancelProductPriceTimer(SessionData sessionData, BasicRequest request) {
        try {
            ProductPriceTimerEntity entity =
                    this.priceDB.getProductPriceTimerEntity(request.getId());
            if (entity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SETTING_NOT_FOUND);
            }

            boolean rsUpdate = this.priceDB.cancelProductPriceTimer(request.getId(),
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

    public ClientResponse runProductPriceTimer() {
        try {
            ProductPriceTimerEntity entity = this.priceDB.getOneProductPriceTimerWaiting();
            if (entity == null) {
                return ClientResponse.success(null);
            }

            Date now = DateTimeUtils.getNow();

            List<String> strProductIdList = new ArrayList<>();

            List<JSONObject> products = this.priceDB.getListProductPriceTimerDetail(entity.getId());
            for (JSONObject product : products) {
                ProductPriceTimerDetailEntity productPriceTimerDetailEntity =
                        ProductPriceTimerDetailEntity.from(product);
                if (product == null) {
                    continue;
                }

                this.productDB.updateProductPrice(
                        productPriceTimerDetailEntity.getProduct_id(),
                        productPriceTimerDetailEntity.getPrice());

                this.dataManager.reloadProduct(
                        CacheType.PRODUCT,
                        productPriceTimerDetailEntity.getProduct_id());

                strProductIdList.add(ConvertUtils.toString(productPriceTimerDetailEntity.getProduct_id()));
            }

            this.priceDB.activeProductPriceTimer(entity.getId());

            ClientResponse crSyncPriceTimer = this.syncPriceTimerToBravo(
                    entity,
                    now,
                    products
            );
            if (crSyncPriceTimer.success()) {
                List<JSONObject> priceSettingList =
                        this.priceDB.getListProductPriceSettingByProductId(
                                JsonUtils.Serialize(strProductIdList)
                        );
                if (!priceSettingList.isEmpty()) {
                    ClientResponse crSyncPriceSettingTotal = ClientResponse.success(null);
                    for (JSONObject jsPriceSetting : priceSettingList) {
                        /**
                         * Cập nhật lại giá mới cho sản phẩm
                         */
                        this.updatePriceNewForSetting(
                                strProductIdList,
                                ConvertUtils.toInt(jsPriceSetting.get("id"))
                        );

                        ClientResponse crSyncPriceSetting = this.syncPriceSettingToBravo(
                                ConvertUtils.toInt(jsPriceSetting.get("id")),
                                now
                        );
                        if (crSyncPriceSetting.failed()) {
                            this.priceDB.syncPriceSettingFailed(
                                    ConvertUtils.toInt(jsPriceSetting.get("id")),
                                    crSyncPriceSetting.getMessage()
                            );

                            crSyncPriceSettingTotal.fail(crSyncPriceSettingTotal.getMessage() + "[" + crSyncPriceSetting.getMessage() + "]");
                        } else {
                            this.priceDB.syncPriceSettingSuccess(
                                    ConvertUtils.toInt(jsPriceSetting.get("id"))
                            );
                        }
                    }
                    if (crSyncPriceSettingTotal.failed()) {
                        this.priceDB.syncPriceTimerFailed(
                                entity.getId(),
                                crSyncPriceSettingTotal.getMessage()
                        );
                    }
                }
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private void updatePriceNewForSetting(
            List<String> strProductIdList,
            int product_price_setting_id) {
        for (String strId : strProductIdList) {
            ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(
                    ConvertUtils.toInt(strId)
            );
            if (productCache == null) {
                continue;
            }
            ProductPriceSettingDetailEntity productPriceSettingDetailEntity
                    = this.priceDB.getProductPriceSettingDetailEntityByProductIdAndSettingId(
                    ConvertUtils.toInt(strId),
                    product_price_setting_id
            );

            productPriceSettingDetailEntity.setPrice_new(
                    ConvertUtils.toLong(this.convertProductPrice(
                            productCache.getPrice(),
                            productCache.getPrice(),
                            productPriceSettingDetailEntity.getIs_auto(),
                            productPriceSettingDetailEntity.getPrice_setting_type(),
                            productPriceSettingDetailEntity.getPrice_data_type(),
                            productPriceSettingDetailEntity.getPrice_setting_value()
                    ))
            );

            this.priceDB.updateProductPriceSettingDetail(
                    productPriceSettingDetailEntity
            );
        }
    }

    public ClientResponse approveProductPriceTimer(
            SessionData sessionData,
            BasicRequest request) {
        try {
            ClientResponse crValidate = request.validate();
            if (crValidate.failed()) {
                return crValidate;
            }

            ProductPriceTimerEntity entity = this.priceDB.getProductPriceTimerEntity(request.getId());
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
                boolean rsUpdate = this.priceDB.updateProductPriceTimer(entity);
                if (!rsUpdate) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
            } else {
                entity.setStatus(ProductPriceTimerStatus.RUNNING.getId());
                boolean rsUpdate = this.priceDB.updateProductPriceTimer(entity);
                if (!rsUpdate) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
                List<String> strProductIdList = new ArrayList<>();
                List<JSONObject> products = this.priceDB.getListProductPriceTimerDetail(entity.getId());
                for (JSONObject product : products) {
                    ProductPriceTimerDetailEntity productPriceTimerDetailEntity =
                            ProductPriceTimerDetailEntity.from(product);
                    if (product == null) {
                        continue;
                    }

                    this.productDB.updateProductPrice(
                            productPriceTimerDetailEntity.getProduct_id(),
                            productPriceTimerDetailEntity.getPrice());

                    this.dataManager.reloadProduct(
                            CacheType.PRODUCT,
                            productPriceTimerDetailEntity.getProduct_id());

                    strProductIdList.add(ConvertUtils.toString(productPriceTimerDetailEntity.getProduct_id()));
                }

                ClientResponse crSyncPriceTimer = this.syncPriceTimerToBravo(entity, now, products);
                if (crSyncPriceTimer.success()) {
                    List<JSONObject> priceSettingList =
                            this.priceDB.getListProductPriceSettingByProductId(
                                    JsonUtils.Serialize(strProductIdList)
                            );
                    if (!priceSettingList.isEmpty()) {
                        ClientResponse crSyncPriceSettingTotal = ClientResponse.success(null);
                        for (JSONObject jsPriceSetting : priceSettingList) {
                            ClientResponse crSyncPriceSetting = this.syncPriceSettingToBravo(
                                    ConvertUtils.toInt(jsPriceSetting.get("id")),
                                    now
                            );
                            if (crSyncPriceSetting.failed()) {
                                this.priceDB.syncPriceSettingFailed(
                                        ConvertUtils.toInt(jsPriceSetting.get("id")),
                                        crSyncPriceSetting.getMessage()
                                );

                                crSyncPriceSettingTotal.fail(crSyncPriceSettingTotal.getMessage() + "[" + crSyncPriceSetting.getMessage() + "]");
                            } else {
                                this.priceDB.syncPriceSettingSuccess(
                                        ConvertUtils.toInt(jsPriceSetting.get("id"))
                                );
                            }
                        }
                        if (crSyncPriceSettingTotal.failed()) {
                            this.priceDB.syncPriceTimerFailed(
                                    entity.getId(),
                                    crSyncPriceSettingTotal.getMessage()
                            );
                        }
                    }
                }
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse runStopProductPriceSettingDetailSchedule() {
        try {
            /**
             * Stop
             */
            List<JSONObject> rsSetting = this.priceDB.getListPriceSettingDetailOverEndDate();
            if (rsSetting.size() > 0) {
                this.priceDB.stopPriceSettingDetailOverEndDate();

                Date now = new Date();
                for (JSONObject setting : rsSetting) {
                    ClientResponse crSyncPriceSetting = this.syncPriceSettingToBravo(
                            ConvertUtils.toInt(setting.get("id")),
                            now
                    );
                    if (crSyncPriceSetting.failed()) {
                        this.priceDB.syncPriceSettingFailed(
                                ConvertUtils.toInt(setting.get("id")),
                                crSyncPriceSetting.getMessage()
                        );
                    } else {
                        this.priceDB.syncPriceSettingSuccess(
                                ConvertUtils.toInt(setting.get("id"))
                        );
                    }
                }
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
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

            settingDate = addTime(settingDate, 10);

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


                    item.put("standardPrice",
                            ConvertUtils.toLong(settingDetail.get("price_new")) > 0 ?
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

    public ClientResponse runStartProductPriceSettingDetailSchedule() {
        try {
            Map<Integer, ProductPriceSettingEntity> mp = new HashMap<>();
            /**
             * Start
             */
            List<JSONObject> rsSettingDetailList = this.priceDB.getListProductPriceSettingDetailNeedStart(
                    ConfigInfo.SCHEDULE_RUNNING_LIMIT
            );

            for (JSONObject rsSettingDetail : rsSettingDetailList) {
                this.priceDB.startPriceSettingDetail(ConvertUtils.toInt(rsSettingDetail.get("id")));

                ProductPriceSettingEntity productPriceSettingEntity
                        = this.priceDB.getProductPriceSettingEntity(
                        ConvertUtils.toInt(rsSettingDetail.get("product_price_setting_id")));
                if (productPriceSettingEntity != null &&
                        productPriceSettingEntity.getStatus() == SettingStatus.RUNNING.getId()) {
                    this.dataManager.callReloadProductPriceSetting(
                            productPriceSettingEntity.getId()
                    );

                    mp.put(productPriceSettingEntity.getId(), productPriceSettingEntity);
                }
            }

            Date now = new Date();
            for (ProductPriceSettingEntity productPriceSettingEntity : mp.values()) {
                ClientResponse crSyncPriceSetting = this.syncPriceSettingToBravo(
                        productPriceSettingEntity.getId(),
                        now
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
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse syncPriceTimerToBravo(
            ProductPriceTimerEntity setting,
            Date settingDate,
            List<JSONObject> products) {
        try {
            if (setting == null) {
                this.priceDB.syncPriceTimerFailed(
                        setting.getId(),
                        ResponseMessage.STATUS_NOT_MATCH.getValue()
                );
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            settingDate = addTime(settingDate, 10);

            List<JSONObject> agencyList = new ArrayList<>();
            ClientResponse crSync = ClientResponse.success(null);
//                {
            JSONObject priceData = new JSONObject();
//                    "docDate": "2023-07-31",
            priceData.put("docDate",
                    DateTimeUtils.toString(
                            settingDate,
                            "yyyy-MM-dd"));
//                        "effectiveDate": "2023-07-31 10:00:00",
            priceData.put("effectiveDate",
                    DateTimeUtils.toString(
                            settingDate,
                            "yyyy-MM-dd HH:mm:ss"));
//                        "finishDate": null,
            priceData.put("finishDate", null);
//                        "docNo": "BGB00003",
            priceData.put("docNo",
                    "BG01-APP-" + String.format("%04d", ConvertUtils.toInt(setting.getId())) +
                            DateTimeUtils.toString(
                                    settingDate,
                                    "yyyyMMddHHmmss"
                            ));
//                        "description": "Tên bản giá",
            priceData.put("description", setting.getName());
//                        "priceListType": "01",
            priceData.put("priceListType", "01");
//                        "customFieldCode1": "SI",
            priceData.put("customFieldCode1", "SI");
//                        "note": "Ghi chú",
            priceData.put("note", setting.getNote());
//                        "customerAppId":2,
            priceData.put("customerAppId", null);
//                        "customerCode":"BRAVO",
            priceData.put("customerCode", null);
//                        "customerName":"Công ty cổ phần phần mềm BRAVO",
            priceData.put("customerName", null);
//                        "customerLevelAppId":null,
            priceData.put("customerLevelAppId", null);
//                        "customerLevelCode":"",
            priceData.put("customerLevelCode", "");

            //                        "detailData": [
            List<JSONObject> detailData = new ArrayList<>();

            for (JSONObject product : products) {
                JSONObject item = new JSONObject();
//                        "bravoItemId": 7981,
                ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(
                        ConvertUtils.toInt(product.get("product_id"))
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
                item.put("standardPrice",
                        ConvertUtils.toLong(product.get("price")) < 0 ? 0 :
                                ConvertUtils.toLong(product.get("price")));
                detailData.add(item);
            }
            priceData.put("detailData", detailData);
            crSync = this.bravoService.syncProductPrice(priceData);
            if (crSync.failed()) {
                this.priceDB.syncPriceTimerFailed(
                        setting.getId(),
                        crSync.getMessage()
                );
                return crSync;
            }

            this.priceDB.syncPriceTimerSuccess(
                    setting.getId()
            );
            return crSync;
        } catch (Exception ex) {
            this.priceDB.syncPriceTimerFailed(
                    setting.getId(),
                    ex.toString()
            );
            LogUtil.printDebug("PRICE: ", ex);
            ClientResponse crException = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            crException.setMessage(ex.getMessage());
            return crException;
        }
    }

    public Date addTime(Date date, int time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.SECOND, time);
        return calendar.getTime();
    }

    public ClientResponse reSyncProductPriceStandToBravo(BasicRequest request) {
        try {
            ProductPriceTimerEntity entity = this.priceDB.getProductPriceTimerEntity(
                    request.getId()
            );
            if (entity == null) {
                return ClientResponse.success(null);
            }

            Date now = DateTimeUtils.getNow();
            List<JSONObject> products = this.priceDB.getListProductPriceTimerDetail(entity.getId());

            return this.syncPriceTimerToBravo(
                    entity,
                    now,
                    products
            );
        } catch (Exception ex) {
            LogUtil.printDebug("PRICE: ", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse reSyncProductPriceSettingToBravo(BasicRequest request) {
        try {
            return this.syncPriceSettingToBravo(
                    request.getId(),
                    DateTimeUtils.getNow()
            );
        } catch (Exception ex) {
            LogUtil.printDebug("PRICE: ", ex);
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