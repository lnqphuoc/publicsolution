package com.app.server.service;

import com.app.server.config.ConfigInfo;
import com.app.server.constants.ResponseMessage;
import com.app.server.data.SessionData;
import com.app.server.data.dto.agency.AgencyBasicData;
import com.app.server.data.dto.agency.Membership;
import com.app.server.data.dto.location.City;
import com.app.server.data.dto.location.Region;
import com.app.server.data.dto.product.ProductCache;
import com.app.server.data.entity.BannerEntity;
import com.app.server.data.entity.NotifyHistoryEntity;
import com.app.server.data.entity.NotifySettingEntity;
import com.app.server.data.request.BasicRequest;
import com.app.server.data.request.BasicResponse;
import com.app.server.data.request.FilterListRequest;
import com.app.server.data.request.banner.ActivateBannerRequest;
import com.app.server.data.request.banner.CreateBannerRequest;
import com.app.server.data.request.banner.EditBannerRequest;
import com.app.server.data.request.notify.CreateNotifyRequest;
import com.app.server.data.request.notify.EditNotifyRequest;
import com.app.server.database.NotifyDB;
import com.app.server.enums.*;
import com.app.server.response.ClientResponse;
import com.app.server.utils.JsonUtils;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class NotifyService extends ProgramService {
    public ClientResponse filterNotifySetting(FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.FILTER_NOTIFY_SETTING, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.notifyDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            for (JSONObject jsonObject : records) {
                jsonObject.put("creator_info", this.dataManager.getStaffManager().getStaff(
                        ConvertUtils.toInt(jsonObject.get("creator_id"))
                ));
            }
            int total = this.notifyDB.getTotal(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.NOTIFY.name());
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterNotifyAutoConfig(FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.FILTER_NOTIFY_AUTO_CONFIG, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.notifyDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            for (JSONObject jsonObject : records) {
                jsonObject.put("creator_info", this.dataManager.getStaffManager().getStaff(
                        ConvertUtils.toInt(jsonObject.get("creator_id"))
                ));
            }
            int total = this.notifyDB.getTotal(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.NOTIFY.name());
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse getNotifyDetail(SessionData sessionData, BasicRequest request) {
        try {
            JSONObject notifySetting = this.notifyDB.getNotifySetting(
                    request.getId()
            );
            if (notifySetting == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            List<AgencyBasicData> agencyDataList = new ArrayList<>();
            List<String> agencyList = this.appUtils.convertStringToArray(
                    notifySetting.get("agency_ids").toString()
            );
            for (String agency : agencyList) {
                agencyDataList.add(
                        this.dataManager.getAgencyManager().getAgencyBasicData(ConvertUtils.toInt(agency))
                );
            }
            notifySetting.put("agency_data", agencyDataList);

            List<BasicResponse> cityDataList = new ArrayList<>();
            List<String> cityList = this.appUtils.convertStringToArray(
                    notifySetting.get("city_ids").toString()
            );
            for (String city : cityList) {
                City cityData = this.dataManager.getProductManager().getMpCity().get(
                        ConvertUtils.toInt(city)
                );
                if (cityData != null) {
                    BasicResponse cityResponse = new BasicResponse(cityData.getId(), cityData.getName());
                    cityResponse.setRegion_id(
                            cityData.getRegion().getId()
                    );
                    cityDataList.add(cityResponse
                    );
                }
            }
            notifySetting.put("city_data", cityDataList);

            List<BasicResponse> regionDataList = new ArrayList<>();
            List<String> regionList = this.appUtils.convertStringToArray(
                    notifySetting.get("region_ids").toString()
            );
            for (String region : regionList) {
                Region regionData = this.dataManager.getProductManager().getMpRegion().get(
                        ConvertUtils.toInt(region)
                );
                if (regionDataList != null) {
                    regionDataList.add(
                            new BasicResponse(regionData.getId(), regionData.getName()));
                }
            }
            notifySetting.put("region_data", regionDataList);

            List<BasicResponse> membershipDataList = new ArrayList<>();
            List<String> membershipList = this.appUtils.convertStringToArray(
                    notifySetting.get("membership_ids").toString()
            );
            for (String membership : membershipList) {
                Membership membershipData = this.dataManager.getProductManager().getMpMembership().get(
                        ConvertUtils.toInt(membership)
                );
                if (membershipData != null) {
                    membershipDataList.add(
                            new BasicResponse(membershipData.getId(), membershipData.getName()));
                }
            }
            notifySetting.put("membership_data", membershipDataList);

            List<BasicResponse> setting_data = this.convertSettingData(
                    ConvertUtils.toString(notifySetting.get("setting_type")),
                    ConvertUtils.toString(notifySetting.get("setting_value"))
            );
            notifySetting.put("setting_data", setting_data);

            JSONObject data = new JSONObject();
            data.put("notify", notifySetting);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.NOTIFY.name());
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse createNotifySetting(SessionData sessionData,
                                              CreateNotifyRequest request) {
        try {
            ClientResponse crValidate = request.validate();
            if (crValidate.failed()) {
                return crValidate;
            }

            /**
             * Không sử dụng banner cho sản phẩm không kinh doanh qua APP
             */
            SettingType settingType = SettingType.from(request.getSetting_type());
            if (settingType != null) {
                if (settingType.getId() == SettingType.SAN_PHAM.getId() ||
                        settingType.getId() == SettingType.DANH_SACH_SAN_PHAM.getId()) {

                    String message = "";
                    for (int iP = 0; iP < request.getSetting_data().size(); iP++) {
                        int id = ConvertUtils.toInt(request.getSetting_data().get(iP));
                        ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(
                                id
                        );
                        if (productCache == null) {
                            message = "[Thứ " + (iP + 1) + "] " + ResponseMessage.PRODUCT_NOT_FOUND.getValue();
                        } else if (productCache.getApp_active() == YesNoStatus.NO.getValue()) {
                            message = "[Thứ " + (iP + 1) + "] " + ResponseMessage.PRODUCT_NOT_APP_ACTIVE.getValue();
                        }
                    }

                    if (!message.isEmpty()) {
                        ClientResponse crValidateActiveApp = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        crValidateActiveApp.setMessage(message);
                        return crValidateActiveApp;
                    }
                } else if (settingType.getId() == SettingType.CSBH_CTKM.getId()) {
                    for (String promo : request.getSetting_data()) {
                        JSONObject jsPromo = this.promoDB.getPromoJs(ConvertUtils.toInt(promo));
                        if (jsPromo == null || !this.checkPromoReady(ConvertUtils.toInt(jsPromo.get("status")))) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_NOT_FOUND);
                        }
                    }
                }
            }

            NotifySettingEntity notifySettingEntity = this.createNotifySettingEntity(request);
            notifySettingEntity.setCreator_id(sessionData.getId());
            notifySettingEntity.setCreated_date(DateTimeUtils.getNow());
            if (notifySettingEntity.getStart_date().getTime() <= DateTimeUtils.getNow().getTime()) {
                notifySettingEntity.setStart_date(DateTimeUtils.getNow());
                notifySettingEntity.setStatus(NotifyStatus.SENT.getId());
            } else {
                notifySettingEntity.setStatus(NotifyStatus.WAITING.getId());
            }

            int rsInsertSetting = this.notifyDB.insertNotifySetting(notifySettingEntity);
            if (rsInsertSetting <= 0) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            notifySettingEntity.setId(rsInsertSetting);

            if (NotifyStatus.SENT.getId() == notifySettingEntity.getStatus()) {
                runNotifySetting(
                        notifySettingEntity,
                        sessionData.getId(),
                        this.notifyDB,
                        this);
            }
            JSONObject data = new JSONObject();
            data.put("id", rsInsertSetting);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.NOTIFY.name());
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private boolean checkPromoReady(int status) {
        if (status == PromoActiveStatus.RUNNING.getId() ||
                status == PromoActiveStatus.WAITING.getId()) {
            return true;
        }

        return false;
    }

    private NotifySettingEntity createNotifySettingEntity(CreateNotifyRequest request) {
        NotifySettingEntity notifySettingEntity = JsonUtils.DeSerialize(
                JsonUtils.Serialize(request), NotifySettingEntity.class
        );
        notifySettingEntity.setStart_date(DateTimeUtils.getDateTime(request.getStart_date_millisecond()));
        notifySettingEntity.setEnd_date(request.getEnd_date_millisecond() == null ?
                null : DateTimeUtils.getDateTime(request.getEnd_date_millisecond()));

        notifySettingEntity.setAgency_ids(JsonUtils.Serialize(request.getAgency_data()));
        notifySettingEntity.setCity_ids(JsonUtils.Serialize(request.getCity_data()));
        notifySettingEntity.setRegion_ids(JsonUtils.Serialize(request.getRegion_data()));
        notifySettingEntity.setMembership_ids(JsonUtils.Serialize(request.getMembership_data()));
        notifySettingEntity.setSetting_value(JsonUtils.Serialize(request.getSetting_data()));
        return notifySettingEntity;
    }

    public ClientResponse activateNotify(SessionData sessionData, ActivateBannerRequest request) {
        try {
            ClientResponse crValidate = request.validate();
            if (crValidate.failed()) {
                return crValidate;
            }

            for (int iNotify = 0; iNotify < request.getIds().size(); iNotify++) {
                int id = request.getIds().get(iNotify);
                ClientResponse clientResponse = this.activeNotifyOne(sessionData.getId(), id);
                if (clientResponse.failed()) {
                    clientResponse.setMessage("[" + (iNotify + 1) + "] " + clientResponse.getMessage());
                    return clientResponse;
                }

                this.notifyDB.saveStartDate(id);
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.NOTIFY.name());
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    protected ClientResponse activeNotifyOne(int staff_id, int notify_id) {
        try {
            NotifySettingEntity notifySettingEntity = this.notifyDB.getNotifySettingEntity(
                    notify_id);
            if (notifySettingEntity == null) {
                return ClientResponse.fail(
                        ResponseStatus.FAIL,
                        ResponseMessage.NOTIFY_NOT_FOUND);
            }

            if (NotifyStatus.SENT.getId() == notifySettingEntity.getStatus()) {
                return ClientResponse.fail(
                        ResponseStatus.FAIL,
                        ResponseMessage.STATUS_NOT_MATCH);
            }

            ClientResponse rsRunNotifySetting = this.runNotifySetting(
                    notifySettingEntity,
                    staff_id,
                    this.notifyDB,
                    this
            );
            if (rsRunNotifySetting.failed()) {
                return rsRunNotifySetting;
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.NOTIFY.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse runNotifySetting(NotifySettingEntity notifySettingEntity, int staff_id, NotifyDB notifyDB, NotifyService notifyService) {
        try {
            Thread thread = new Thread() {
                public void run() {
                    try {
                        boolean rsActive = notifyDB.activateNotifySetting(
                                notifySettingEntity.getId(), staff_id);
                        if (!rsActive) {
                            return;
                        }

                        List<String> agencyList = notifyService.getListAgencyPushNotify(
                                notifySettingEntity.getAgency_ids(),
                                notifySettingEntity.getCity_ids(),
                                notifySettingEntity.getRegion_ids(),
                                notifySettingEntity.getMembership_ids(),
                                notifySettingEntity.getSetting_type(),
                                notifySettingEntity.getSetting_value()
                        );
                        boolean rsSaveHistory = notifyService.saveNotifyHistory(
                                notifySettingEntity.getName(),
                                notifySettingEntity.getImage(),
                                notifySettingEntity.getDescription(),
                                1,
                                DateTimeUtils.getNow(),
                                staff_id,
                                JsonUtils.Serialize(agencyList),
                                "[]",
                                "[]",
                                "[]",
                                notifySettingEntity.getSetting_type(),
                                notifySettingEntity.getSetting_value()
                        );
                        if (!rsSaveHistory) {
                            return;
                        }

                        List<String> fbAgencyList = notifyService.getListFirebaseAgency(
                                JsonUtils.Serialize(agencyList),
                                "[]",
                                "[]",
                                "[]"
                        );
                        if (!fbAgencyList.isEmpty()) {
                            ClientResponse crSaveNotifyWaitingPush = notifyService.saveNotifyWaitingPush(
                                    fbAgencyList,
                                    notifySettingEntity.getName(),
                                    notifySettingEntity.getDescription(),
                                    notifySettingEntity.getImage(), 1);
                            if (crSaveNotifyWaitingPush.failed()) {
                                return;
                            }
                        }
                    } catch (Exception ex) {
                        LogUtil.printDebug(Module.NOTIFY.name(), ex);
                    }
                }
            };
            thread.start();
        } catch (Exception ex) {
            LogUtil.printDebug("CACHE", ex);
        }
        return ClientResponse.success(null);
    }

    private List<String> getListAgencyPushNotify(
            String agency_ids,
            String city_ids,
            String region_ids,
            String membership_ids,
            String setting_type,
            String setting_value) {
        SettingType settingType = SettingType.from(setting_type);
        List<String> result = new ArrayList<>();
        if (settingType == null) {
            return result;
        }
        List<JSONObject> agencyList = this.agencyDB.getListAgency(
                agency_ids, city_ids, region_ids, membership_ids
        );
        switch (settingType) {
            case SAN_PHAM:
                for (JSONObject agency : agencyList) {
                    List<String> values = this.appUtils.convertStringToArray(setting_value);
                    for (String value : values) {
                        if (this.getProductVisibilityByAgency(
                                ConvertUtils.toInt(agency.get("id")),
                                ConvertUtils.toInt(value)
                        ) == VisibilityType.SHOW.getId()) {
                            result.add(ConvertUtils.toString(agency.get("id")));
                            break;
                        }
                    }
                }
                break;
            case DANH_SACH_SAN_PHAM:
                for (JSONObject agency : agencyList) {
                    List<String> values = this.appUtils.convertStringToArray(setting_value);
                    for (String value : values) {
                        if (this.getProductVisibilityByAgency(
                                ConvertUtils.toInt(agency.get("id")),
                                ConvertUtils.toInt(value)
                        ) == VisibilityType.SHOW.getId()) {
                            result.add(ConvertUtils.toString(agency.get("id")));
                            break;
                        }
                    }
                }
                break;
            case MAT_HANG:
                for (JSONObject agency : agencyList) {
                    List<String> values = this.appUtils.convertStringToArray(setting_value);
                    for (String value : values) {
                        if (this.getMatHangVisibilityByAgency(
                                ConvertUtils.toInt(agency.get("id")),
                                ConvertUtils.toInt(value)
                        ) == VisibilityType.SHOW.getId()) {
                            result.add(ConvertUtils.toString(agency.get("id")));
                            break;
                        }
                    }
                }
                break;
            case PHAN_LOAI_THEO_SP:
                for (JSONObject agency : agencyList) {
                    List<String> values = this.appUtils.convertStringToArray(setting_value);
                    for (String value : values) {
                        if (this.getPLSPVisibilityByAgency(
                                ConvertUtils.toInt(agency.get("id")),
                                ConvertUtils.toInt(value)
                        ) == VisibilityType.SHOW.getId()) {
                            result.add(ConvertUtils.toString(agency.get("id")));
                            break;
                        }
                    }
                }
                break;
            case PHAN_LOAI_THEO_THUONG_HIEU:
                for (JSONObject agency : agencyList) {
                    List<String> values = this.appUtils.convertStringToArray(setting_value);
                    for (String value : values) {
                        if (this.getPLTTHVisibilityByAgency(
                                ConvertUtils.toInt(agency.get("id")),
                                ConvertUtils.toInt(value)
                        ) == VisibilityType.SHOW.getId()) {
                            result.add(ConvertUtils.toString(agency.get("id")));
                            break;
                        }
                    }
                }
                break;
            case LOAI_SAN_PHAM:
                for (JSONObject agency : agencyList) {
                    List<String> values = this.appUtils.convertStringToArray(setting_value);
                    for (String value : values) {
                        if (this.getLoaiSPVisibilityByAgency(
                                ConvertUtils.toInt(agency.get("id")),
                                ConvertUtils.toInt(value)
                        ) == VisibilityType.SHOW.getId()) {
                            result.add(ConvertUtils.toString(agency.get("id")));
                            break;
                        }
                    }
                }
                break;
            case NHOM_HANG:
                for (JSONObject agency : agencyList) {
                    List<String> values = this.appUtils.convertStringToArray(setting_value);
                    for (String value : values) {
                        if (this.getProductGroupVisibilityByAgency(
                                ConvertUtils.toInt(agency.get("id")),
                                ConvertUtils.toInt(value)
                        ) == VisibilityType.SHOW.getId()) {
                            result.add(ConvertUtils.toString(agency.get("id")));
                            break;
                        }
                    }
                }
                break;
            case QUANG_BA:
                result = agencyList.stream().map(
                        e -> e.get("id").toString()
                ).collect(Collectors.toList());
                break;
            case CSBH_CTKM:
                for (JSONObject agency : agencyList) {
                    List<String> values = this.appUtils.convertStringToArray(setting_value);
                    for (String value : values) {
                        JSONObject promo = this.promoDB.getPromoJs(ConvertUtils.toInt(value));
                        if (promo == null) {
                            break;
                        }
                        String condition_type = ConvertUtils.toString(promo.get("condition_type"));
                        String promo_type = ConvertUtils.toString(promo.get("promo_type"));
                        if (this.getPromoVisibilityByAgency(
                                ConvertUtils.toInt(agency.get("id")),
                                ConvertUtils.toInt(value)
                        ) == VisibilityType.SHOW.getId()) {
                            if (condition_type.equals(PromoConditionType.PRODUCT_QUANTITY.getKey()) ||
                                    condition_type.equals(PromoConditionType.PRODUCT_PRICE.getKey())) {
                                List<JSONObject> productList = this.promoDB.getListProductInPromo(
                                        ConvertUtils.toInt(value)
                                );

                                boolean isShow = false;
                                if (!PromoType.DAMME.getKey().equals(promo_type)) {
                                    for (JSONObject product : productList) {
                                        if (this.getProductVisibilityByAgency(
                                                ConvertUtils.toInt(agency.get("id")),
                                                ConvertUtils.toInt(product.get("item_id"))
                                        ) == VisibilityType.SHOW.getId()) {
                                            isShow = true;
                                            break;
                                        }
                                    }
                                } else {
                                    for (JSONObject product : productList) {
                                        String item_type = ConvertUtils.toString(product.get("item_type"));
                                        if (DamMeProductType.PRODUCT.getKey().equals(item_type)) {
                                            if (this.getProductVisibilityByAgency(
                                                    ConvertUtils.toInt(agency.get("id")),
                                                    ConvertUtils.toInt(product.get("item_id"))
                                            ) == VisibilityType.SHOW.getId()) {
                                                isShow = true;
                                                break;
                                            }
                                        } else if (DamMeProductType.PRODUCT_GROUP.getKey().equals(item_type)) {
                                            if (this.getProductGroupVisibilityByAgency(
                                                    ConvertUtils.toInt(agency.get("id")),
                                                    ConvertUtils.toInt(product.get("item_id"))
                                            ) == VisibilityType.SHOW.getId()) {
                                                isShow = true;
                                                break;
                                            }
                                        } else if (DamMeProductType.CATEGORY.getKey().equals(item_type)) {
                                            int category_level = ConvertUtils.toInt(product.get("category_level"));
                                            if (CategoryLevel.MAT_HANG.getKey() == category_level) {
                                                if (this.getMatHangVisibilityByAgency(
                                                        ConvertUtils.toInt(agency.get("id")),
                                                        ConvertUtils.toInt(product.get("item_id"))
                                                ) == VisibilityType.SHOW.getId()) {
                                                    isShow = true;
                                                    break;
                                                }
                                            } else if (CategoryLevel.PHAN_LOAI_HANG.getKey() == category_level) {
                                                if (this.getPLSPVisibilityByAgency(
                                                        ConvertUtils.toInt(agency.get("id")),
                                                        ConvertUtils.toInt(product.get("item_id"))
                                                ) == VisibilityType.SHOW.getId()) {
                                                    isShow = true;
                                                    break;
                                                }
                                            } else if (CategoryLevel.PHAN_LOAI_HANG_THEO_THUONG_HIEU.getKey() == category_level) {
                                                if (this.getPLTTHVisibilityByAgency(
                                                        ConvertUtils.toInt(agency.get("id")),
                                                        ConvertUtils.toInt(product.get("item_id"))
                                                ) == VisibilityType.SHOW.getId()) {
                                                    isShow = true;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                                if (isShow) {
                                    result.add(ConvertUtils.toString(agency.get("id")));
                                }
                                break;
                            } else {
                                result.add(ConvertUtils.toString(agency.get("id")));
                                break;
                            }
                        }
                    }
                }
                break;
            default:
                result = agencyList.stream().map(
                        e -> e.get("id").toString()
                ).collect(Collectors.toList());
                break;
        }
        return result;
    }

    public ClientResponse cancelNotify(SessionData sessionData,
                                       ActivateBannerRequest request) {
        try {
            ClientResponse crValidate = request.validate();
            if (crValidate.failed()) {
                return crValidate;
            }


            for (int iNotify = 0; iNotify < request.getIds().size(); iNotify++) {
                int id = request.getIds().get(iNotify);
                ClientResponse clientResponse = this.cancelNotifyOne(sessionData.getId(), id);
                clientResponse.setMessage("[" + (iNotify + 1) + "] " + clientResponse.getMessage());
                if (clientResponse.failed()) {
                    return clientResponse;
                }
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.NOTIFY.name());
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse cancelNotifyOne(int staff_id, int notify_id) {
        try {
            NotifySettingEntity notifySettingEntity = this.notifyDB.getNotifySettingEntity(notify_id);
            if (notifySettingEntity == null) {
                return ClientResponse.fail(
                        ResponseStatus.FAIL,
                        ResponseMessage.BANNER_NOT_FOUND);
            }

            if (NotifyStatus.SENT.getId() == notifySettingEntity.getStatus()) {
                return ClientResponse.fail(
                        ResponseStatus.FAIL,
                        ResponseMessage.STATUS_NOT_MATCH);
            }

            boolean rsCancel = this.notifyDB.cancelNotify(notify_id, staff_id);
            if (!rsCancel) {
                return ClientResponse.fail(
                        ResponseStatus.FAIL,
                        ResponseMessage.FAIL);
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.NOTIFY.name());
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse editNotify(SessionData sessionData,
                                     EditNotifyRequest request) {
        try {
            ClientResponse crValidate = request.validate();
            if (crValidate.failed()) {
                return crValidate;
            }

            NotifySettingEntity oldNotify = this.notifyDB.getNotifySettingEntity(request.getId());
            if (oldNotify == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.NOTIFY_NOT_FOUND);
            }

            if (oldNotify.getStatus() != NotifyStatus.WAITING.getId()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            NotifySettingEntity notifySettingEntity = this.createNotifySettingEntity(
                    JsonUtils.DeSerialize(JsonUtils.Serialize(request), CreateNotifyRequest.class)
            );
            notifySettingEntity.setId(oldNotify.getId());
            notifySettingEntity.setCreator_id(oldNotify.getCreator_id());
            notifySettingEntity.setCreated_date(oldNotify.getCreated_date());
            notifySettingEntity.setModifier_id(sessionData.getId());
            notifySettingEntity.setModified_date(DateTimeUtils.getNow());

            if (notifySettingEntity.getStart_date().getTime() <= DateTimeUtils.getNow().getTime()) {
                notifySettingEntity.setStart_date(DateTimeUtils.getNow());
                notifySettingEntity.setStatus(NotifyStatus.SENT.getId());
            }

            boolean rsUpdate = this.notifyDB.updateNotifySetting(
                    notifySettingEntity
            );
            if (!rsUpdate) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }


            /**
             * nếu gửi ngay thì gửi thông báo
             */
            if (NotifyStatus.SENT.getId() == notifySettingEntity.getStatus()) {
                List<String> agencyList = this.getListAgencyPushNotify(
                        notifySettingEntity.getAgency_ids(),
                        notifySettingEntity.getCity_ids(),
                        notifySettingEntity.getRegion_ids(),
                        notifySettingEntity.getMembership_ids(),
                        notifySettingEntity.getSetting_type(),
                        notifySettingEntity.getSetting_value()
                );
                /**
                 * Lưu lịch sử
                 */
                boolean rsSaveHistory = this.saveNotifyHistory(
                        notifySettingEntity.getName(),
                        notifySettingEntity.getImage(),
                        notifySettingEntity.getDescription(),
                        1,
                        DateTimeUtils.getNow(),
                        sessionData.getId(),
                        JsonUtils.Serialize(agencyList),
                        "[]",
                        "[]",
                        "[]",
                        notifySettingEntity.getSetting_type(),
                        notifySettingEntity.getSetting_value()
                );
                if (!rsSaveHistory) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                List<String> fbAgencyList = this.getListFirebaseAgency(
                        notifySettingEntity.getAgency_ids(),
                        notifySettingEntity.getCity_ids(),
                        notifySettingEntity.getRegion_ids(),
                        notifySettingEntity.getMembership_ids()
                );
                if (!fbAgencyList.isEmpty()) {
                    ClientResponse crSaveNotifyWaitingPush = this.saveNotifyWaitingPush(
                            fbAgencyList,
                            notifySettingEntity.getName(),
                            notifySettingEntity.getDescription(),
                            notifySettingEntity.getImage(), 1);
                }
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.NOTIFY.name());
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse deleteNotify(SessionData sessionData,
                                       ActivateBannerRequest request) {
        try {
            ClientResponse crValidate = request.validate();
            if (crValidate.failed()) {
                return crValidate;
            }

            for (int iNotify = 0; iNotify < request.getIds().size(); iNotify++) {
                int id = request.getIds().get(iNotify);
                JSONObject notifySetting = this.notifyDB.getNotifySetting(id);
                if (notifySetting == null
                        || ConvertUtils.toInt(notifySetting.get("status")) == NotifyStatus.SENT.getId()) {
                    ClientResponse crDelete = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
                    crDelete.setMessage("[Thứ " + (iNotify + 1) + "] " + crDelete.getMessage());
                    return crDelete;
                }

                boolean rsDelete = this.notifyDB.deleteNotifySetting(id, sessionData.getId());
                if (!rsDelete) {
                    ClientResponse crDelete = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    crDelete.setMessage("[Thứ " + (iNotify + 1) + "] " + crDelete.getMessage());
                    return crDelete;
                }
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.NOTIFY.name());
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse runStartNotifyWaitingPushSchedule() {
        try {
            Date now = new Date();
            List<JSONObject> rsNotifyList = this.notifyDB.getListNotifyWaitingPush(ConfigInfo.SCHEDULE_RUNNING_LIMIT);
            for (JSONObject notify : rsNotifyList) {
                int id = ConvertUtils.toInt(notify.get("id"));
                NotifyDisplayType notifyDisplayType = NotifyDisplayType.from(
                        ConvertUtils.toInt(notify.get("type")));
                switch (notifyDisplayType) {
                    case POPUP:
                        this.callPushPopupToAgency(
                                ConvertUtils.toString(notify.get("firebase_token_data")),
                                ConvertUtils.toString(notify.get("name")),
                                ConvertUtils.toString(notify.get("description")),
                                ConvertUtils.toString(notify.get("image")),
                                ConvertUtils.toString(notify.get("data")),
                                now.getTime()
                        );
                        break;
                    default:
                        this.callPushNotifyToAgency(
                                ConvertUtils.toString(notify.get("firebase_token_data")),
                                ConvertUtils.toString(notify.get("name")),
                                ConvertUtils.toString(notify.get("description")),
                                ConvertUtils.toString(notify.get("image"))
                        );
                        break;
                }

                this.notifyDB.setNotifyWaitingPush(id, NotifyWaitingPushStatus.SENT.getId());
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.NOTIFY.name());
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse runStartNotifySettingSchedule() {
        try {
            List<JSONObject> rsNotifyList = this.notifyDB.getListNotifySettingNeetStart(ConfigInfo.SCHEDULE_RUNNING_LIMIT);
            for (JSONObject notify : rsNotifyList) {
                int id = ConvertUtils.toInt(notify.get("id"));
                ClientResponse crActiveNotify = this.activeNotifyOne(
                        0,
                        id
                );

                if (crActiveNotify.failed()) {
                    this.alertToTelegram(crActiveNotify.getMessage(), ResponseStatus.FAIL);
                }
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.NOTIFY.name());
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse runPushNotifyNQH() {
        try {
            if (this.dataManager.getConfigManager().getPushNotifyNQH() != YesNoStatus.YES.getValue()) {
                return ClientResponse.success(null);
            }

            List<JSONObject> agencyList = this.deptDB.getListAgencyHasNQH();
            for (JSONObject agency : agencyList) {
                int agency_id = ConvertUtils.toInt(agency.get("agency_id"));
                this.pushNotifyToAgency(
                        0,
                        NotifyAutoContentType.DEPT_AGENCY_NQH,
                        "",
                        NotifyAutoContentType.DEPT_AGENCY_NQH.getType(),
                        "[]",
                        "Quý khách đang có nợ quá hạn " + this.appUtils.priceFormat(ConvertUtils.toLong(agency.get("nqh"))) + "." +
                                " Vui lòng thanh toán trong thời gian sớm nhất",
                        agency_id
                );
            }

            this.reportToTelegram(
                    "Push thông báo nợ quá hạn: FINISH",
                    ResponseStatus.SUCCESS
            );

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.NOTIFY.name());
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    /**
     * Push thông báo hẹn giờ
     *
     * @return
     */
    public ClientResponse runStartNotifyWaitingSchedule() {
        try {
            List<JSONObject> rsNotifyList = this.notifyDB.getListNotifyWaitingScheduleNeedStart(ConfigInfo.SCHEDULE_RUNNING_LIMIT);
            for (JSONObject notify : rsNotifyList) {
                int id = ConvertUtils.toInt(notify.get("id"));
                int agency_id = ConvertUtils.toInt(notify.get("agency_id"));
                int type = ConvertUtils.toInt(notify.get("type"));
                if (NotifyDisplayType.NOTIFY.getId() == type) {
                    this.saveNotifyHistory(
                            ConvertUtils.toString(notify.get("name")),
                            ConvertUtils.toString(notify.get("image")),
                            ConvertUtils.toString(notify.get("description")),
                            1,
                            DateTimeUtils.getNow(),
                            0,
                            "[\"" + agency_id + "\"]",
                            "[]",
                            "[]",
                            "[]",
                            SettingType.QUANG_BA.getCode(),
                            "[]"
                    );

                    List<String> fbList = this.getListFirebaseAgency(
                            "[\"" + agency_id + "\"]",
                            "[]", "[]", "[]");
                    if (fbList.size() > 0) {
                        this.callPushNotifyToAgency(
                                JsonUtils.Serialize(fbList),
                                ConvertUtils.toString(notify.get("name")),
                                ConvertUtils.toString(notify.get("description")),
                                ConvertUtils.toString(notify.get("image"))
                        );
                    }
                }
                if (NotifyDisplayType.POPUP.getId() == type) {
                    String setting_type = ConvertUtils.toString(notify.get("setting_type"));
                    ClientResponse crPushNotifyToastToAgency = this.pushPopupToAgency(
                            0,
                            null,
                            ConvertUtils.toString(notify.get("image")),
                            "",
                            setting_type,
                            "[]",
                            "",
                            ConvertUtils.toString(notify.get("name")),
                            ConvertUtils.toString(notify.get("description")),
                            agency_id,
                            ConvertUtils.toString(notify.get("data"))
                    );
                }
                this.notifyDB.setNotifyWaitingSchedulePush(id, NotifyWaitingPushStatus.SENT.getId());
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.NOTIFY.name());
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }
}