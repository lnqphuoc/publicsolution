package com.app.server.service;

import com.app.server.config.ConfigInfo;
import com.app.server.constants.ResponseMessage;
import com.app.server.data.SessionData;
import com.app.server.data.dto.agency.AgencyBasicData;
import com.app.server.data.dto.agency.Membership;
import com.app.server.data.dto.location.City;
import com.app.server.data.dto.location.Region;
import com.app.server.data.dto.product.*;
import com.app.server.data.entity.BannerEntity;
import com.app.server.data.entity.BrandEntity;
import com.app.server.data.entity.PromoEntity;
import com.app.server.data.request.BasicRequest;
import com.app.server.data.request.BasicResponse;
import com.app.server.data.request.FilterListRequest;
import com.app.server.data.request.banner.ActivateBannerRequest;
import com.app.server.data.request.banner.CreateBannerRequest;
import com.app.server.data.request.banner.EditBannerRequest;
import com.app.server.data.request.product.SortBrandRequest;
import com.app.server.database.BannerDB;
import com.app.server.enums.*;
import com.app.server.response.ClientResponse;
import com.app.server.utils.JsonUtils;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.Banner;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import springfox.documentation.spring.web.json.Json;

import java.util.*;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class BannerService extends BaseService {
    public ClientResponse filterBanner(FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.FILTER_BANNER, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.bannerDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            for (JSONObject jsonObject : records) {
                int id = ConvertUtils.toInt(jsonObject.get("id"));
                jsonObject.put("creator_info", this.dataManager.getStaffManager().getStaff(
                        ConvertUtils.toInt(jsonObject.get("creator_id"))
                ));


                jsonObject.put("priority", this.dataManager.getBannerManager().getPriority(id));
            }
            int total = this.bannerDB.getTotal(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.NOTIFY.name());
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private int getPriorityOfBanner(int id, List<JSONObject> bannerPriority) {
        for (int i = 0; i < bannerPriority.size(); i++) {
            if (id == ConvertUtils.toInt(bannerPriority.get(i).get("id"))) {
                return i + 1;
            }
        }
        return 0;
    }

    public ClientResponse createBanner(SessionData sessionData, CreateBannerRequest request) {
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

            BannerEntity bannerEntity = this.createBannerEntity(request);
            bannerEntity.setCreator_id(sessionData.getId());
            bannerEntity.setCreated_date(DateTimeUtils.getNow());
            bannerEntity.setStatus(BannerStatus.WAITING.getId());
            int rs = this.bannerDB.insertBanner(bannerEntity);
            if (rs <= 0) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            return ClientResponse.success(null);
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

    private BannerEntity createBannerEntity(CreateBannerRequest request) {
        BannerEntity bannerEntity = JsonUtils.DeSerialize(
                JsonUtils.Serialize(request), BannerEntity.class
        );
        bannerEntity.setStart_date(DateTimeUtils.getDateTime(request.getStart_date_millisecond()));
        bannerEntity.setEnd_date(request.getEnd_date_millisecond() == null ?
                null : DateTimeUtils.getDateTime(request.getEnd_date_millisecond()));

        bannerEntity.setAgency_ids(JsonUtils.Serialize(request.getAgency_data()));
        bannerEntity.setCity_ids(JsonUtils.Serialize(request.getCity_data()));
        bannerEntity.setRegion_ids(JsonUtils.Serialize(request.getRegion_data()));
        bannerEntity.setMembership_ids(JsonUtils.Serialize(request.getMembership_data()));
        bannerEntity.setSetting_value(JsonUtils.Serialize(request.getSetting_data()));

        if (bannerEntity.getType().equals("CLIP")) {
            bannerEntity.setSetting_type(SettingType.QUANG_BA.getCode());
            bannerEntity.setImage(request.getSetting_data().get(0));
            bannerEntity.setSetting_value(JSONArray.toJSONString(request.getSetting_data()));
        }
        return bannerEntity;
    }

    public ClientResponse editBanner(SessionData sessionData, EditBannerRequest request) {
        try {
            ClientResponse crValidate = request.validate();
            if (crValidate.failed()) {
                return crValidate;
            }

            BannerEntity oldBanner = this.bannerDB.getBannerEntity(request.getId());
            if (oldBanner == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            if (oldBanner.getStatus() == BannerStatus.ACTIVATED.getId()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            BannerEntity bannerEntity = this.createBannerEntity(
                    JsonUtils.DeSerialize(JsonUtils.Serialize(request), CreateBannerRequest.class)
            );
            bannerEntity.setId(oldBanner.getId());
            bannerEntity.setCreator_id(oldBanner.getCreator_id());
            bannerEntity.setCreated_date(oldBanner.getCreated_date());
            bannerEntity.setModifier_id(sessionData.getId());
            bannerEntity.setModified_date(DateTimeUtils.getNow());
            bannerEntity.setStatus(oldBanner.getStatus());
            bannerEntity.setPriority(oldBanner.getPriority());
            if (BannerStatus.ACTIVATED.getId() == oldBanner.getStatus()) {
                if (bannerEntity.getStart_date().getTime() <= DateTimeUtils.getNow().getTime()
                        && (bannerEntity.getEnd_date() == null ||
                        bannerEntity.getEnd_date().getTime() >= DateTimeUtils.getNow().getTime())
                ) {
                    bannerEntity.setStatus(BannerStatus.ACTIVATED.getId());
                } else if ((bannerEntity.getEnd_date() != null &&
                        bannerEntity.getEnd_date().getTime() < DateTimeUtils.getNow().getTime())) {
                    bannerEntity.setStatus(BannerStatus.PENDING.getId());
                } else if (bannerEntity.getStart_date().getTime() > DateTimeUtils.getNow().getTime()) {
                    bannerEntity.setStatus(BannerStatus.WAITING.getId());
                }
            } else if (BannerStatus.WAITING.getId() == oldBanner.getId()) {
                if (bannerEntity.getEnd_date() != null &&
                        bannerEntity.getEnd_date().getTime() < DateTimeUtils.getNow().getTime()) {
                    bannerEntity.setStatus(BannerStatus.PENDING.getId());
                    bannerEntity.setPriority(0);
                } else {
                    if (oldBanner.getPriority() != 0) {
                        if (bannerEntity.getStart_date().getTime() <= DateTimeUtils.getNow().getTime()
                                && (bannerEntity.getEnd_date() == null ||
                                bannerEntity.getEnd_date().getTime() >= DateTimeUtils.getNow().getTime())
                        ) {
                            bannerEntity.setStatus(BannerStatus.ACTIVATED.getId());
                        }
                    }
                }
            } else {
                if (bannerEntity.getStart_date().getTime() <= DateTimeUtils.getNow().getTime()
                        && (bannerEntity.getEnd_date() == null ||
                        bannerEntity.getEnd_date().getTime() >= DateTimeUtils.getNow().getTime())
                ) {
                    bannerEntity.setStatus(BannerStatus.WAITING.getId());
                }
            }


            boolean rsUpdate = this.bannerDB.updateBanner(bannerEntity);
            if (!rsUpdate) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            if (BannerStatus.ACTIVATED.getId() == oldBanner.getStatus()
                    || BannerStatus.ACTIVATED.getId() == bannerEntity.getStatus()) {
                this.dataManager.reloadBanner(
                        request.getId());
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.NOTIFY.name());
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse activateBanner(SessionData sessionData, ActivateBannerRequest request) {
        try {
            ClientResponse crValidate = request.validate();
            if (crValidate.failed()) {
                return crValidate;
            }


            for (int iBanner = 0; iBanner < request.getIds().size(); iBanner++) {
                int id = request.getIds().get(iBanner);
                ClientResponse clientResponse = this.activeBannerOne(sessionData.getId(), id);
                if (clientResponse.failed()) {
                    clientResponse.setMessage("[" + (iBanner + 1) + "] " + clientResponse.getMessage());
                    return clientResponse;
                }
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.NOTIFY.name());
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse activeBannerOne(int staff_id, int banner_id) {
        try {
            BannerEntity bannerEntity = this.bannerDB.getBannerEntity(banner_id);
            if (bannerEntity == null) {
                return ClientResponse.fail(
                        ResponseStatus.FAIL,
                        ResponseMessage.BANNER_NOT_FOUND);
            }

            if (BannerStatus.ACTIVATED.getId() == bannerEntity.getStatus()) {
                return ClientResponse.fail(
                        ResponseStatus.FAIL,
                        ResponseMessage.STATUS_NOT_MATCH);
            }

            int priority = bannerEntity.getPriority();
            priority = priority == 0 ? this.dataManager.getBannerManager().getLastPriority() + 1 : priority;

            boolean rsActivateBanner = this.bannerDB.activateBanner(
                    banner_id,
                    priority);
            if (!rsActivateBanner) {
                return ClientResponse.fail(
                        ResponseStatus.FAIL,
                        ResponseMessage.FAIL);
            }
            this.dataManager.reloadBanner(banner_id);

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.NOTIFY.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse deactivateBanner(SessionData sessionData, ActivateBannerRequest request) {
        try {
            ClientResponse crValidate = request.validate();
            if (crValidate.failed()) {
                return crValidate;
            }


            for (int iBanner = 0; iBanner < request.getIds().size(); iBanner++) {
                int id = request.getIds().get(iBanner);
                ClientResponse clientResponse = this.deactivateBannerOne(sessionData.getId(), id);
                clientResponse.setMessage("[" + (iBanner + 1) + "] " + clientResponse.getMessage());
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

    private ClientResponse deactivateBannerOne(int staff_id, int banner_id) {
        try {
            BannerEntity bannerEntity = this.bannerDB.getBannerEntity(banner_id);
            if (bannerEntity == null) {
                return ClientResponse.fail(
                        ResponseStatus.FAIL,
                        ResponseMessage.BANNER_NOT_FOUND);
            }

            if (BannerStatus.PENDING.getId() == bannerEntity.getStatus()) {
                return ClientResponse.fail(
                        ResponseStatus.FAIL,
                        ResponseMessage.STATUS_NOT_MATCH);
            }

            boolean rsRemoveBanner = this.bannerDB.removePriority(banner_id);
            if (!rsRemoveBanner) {
                return ClientResponse.fail(
                        ResponseStatus.FAIL,
                        ResponseMessage.FAIL);
            }

            if (BannerStatus.ACTIVATED.getId() == bannerEntity.getStatus()) {
                this.dataManager.reloadBanner(banner_id);
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.NOTIFY.name());
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse deleteBanner(SessionData sessionData, ActivateBannerRequest request) {
        try {
            ClientResponse crValidate = request.validate();
            if (crValidate.failed()) {
                return crValidate;
            }

            for (int iBanner = 0; iBanner < request.getIds().size(); iBanner++) {
                int id = request.getIds().get(iBanner);
                JSONObject banner = this.bannerDB.getBanner(id);
                if (banner == null
                        || ConvertUtils.toInt(banner.get("status")) == BannerStatus.ACTIVATED.getId()) {
                    ClientResponse crDelete = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
                    crDelete.setMessage("[Thứ " + (iBanner + 1) + "] " + crDelete.getMessage());
                    return crDelete;
                }

                boolean rsDelete = this.bannerDB.deleteBanner(id, request.getNote());
                if (!rsDelete) {
                    ClientResponse crDelete = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    crDelete.setMessage("[Thứ " + (iBanner + 1) + "] " + crDelete.getMessage());
                    return crDelete;
                }
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.NOTIFY.name());
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse getBannerDetail(SessionData sessionData, BasicRequest request) {
        try {
            JSONObject banner = this.bannerDB.getBanner(
                    request.getId()
            );
            if (banner == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            List<AgencyBasicData> agencyDataList = new ArrayList<>();
            List<String> agencyList = this.appUtils.convertStringToArray(
                    banner.get("agency_ids").toString()
            );
            for (String agency : agencyList) {
                agencyDataList.add(
                        this.dataManager.getAgencyManager().getAgencyBasicData(ConvertUtils.toInt(agency))
                );
            }
            banner.put("agency_data", agencyDataList);

            List<BasicResponse> cityDataList = new ArrayList<>();
            List<String> cityList = this.appUtils.convertStringToArray(
                    banner.get("city_ids").toString()
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
            banner.put("city_data", cityDataList);

            List<BasicResponse> regionDataList = new ArrayList<>();
            List<String> regionList = this.appUtils.convertStringToArray(
                    banner.get("region_ids").toString()
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
            banner.put("region_data", regionDataList);

            List<BasicResponse> membershipDataList = new ArrayList<>();
            List<String> membershipList = this.appUtils.convertStringToArray(
                    banner.get("membership_ids").toString()
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
            banner.put("membership_data", membershipDataList);

            List<BasicResponse> setting_data = this.convertSettingData(
                    ConvertUtils.toString(banner.get("setting_type")),
                    ConvertUtils.toString(banner.get("setting_value"))
            );
            banner.put("setting_data", setting_data);

            JSONObject data = new JSONObject();
            data.put("banner", banner);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.NOTIFY.name());
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse sortBanner(SessionData sessionData, SortBrandRequest request) {
        try {
            List<JSONObject> bannerPriorityList = this.bannerDB.getListBannerPriority();
            for (JSONObject banner : bannerPriorityList) {
                int id = ConvertUtils.toInt(banner.get("id"));
                if (!request.getIds().contains(id)) {
                    boolean rsRemovePriority = this.bannerDB.removePriority(id);
                    if (!rsRemovePriority) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }
                    this.dataManager.reloadBanner(id);
                }
            }

            for (int iBanner = 0; iBanner < request.getIds().size(); iBanner++) {
                int id = request.getIds().get(iBanner);
                JSONObject banner = this.bannerDB.getBanner(id);
                if (banner == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
                int priority = iBanner + 1;
                if (priority != ConvertUtils.toInt(banner.get("priority"))) {
                    boolean rsSetPriority = this.bannerDB.setPriority(id, priority);
                    if (!rsSetPriority) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }

                    this.dataManager.reloadBanner(id);
                }
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PRODUCT.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterBannerPriority(SessionData sessionData, FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.FILTER_BANNER_PRIORITY, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.bannerDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            data.put("records", records);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.NOTIFY.name());
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse runStartBannerSchedule() {
        try {
            List<JSONObject> rsBannerList = this.bannerDB.getListBannerNeedStart(
                    ConfigInfo.SCHEDULE_RUNNING_LIMIT
            );
            for (JSONObject banner : rsBannerList) {
                int id = ConvertUtils.toInt(banner.get("id"));
                ClientResponse rsActiveBanner = this.activeBannerOne(
                        0,
                        id
                );
                if (rsActiveBanner.failed()) {
                    this.alertToTelegram("runStartBannerSchedule: " + id + " FAILED", ResponseStatus.FAIL);
                }
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.NOTIFY.name());
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse runStopBannerSchedule() {
        try {
            List<JSONObject> rsBannerList = this.bannerDB.getListBannerNeedStop(
                    ConfigInfo.SCHEDULE_RUNNING_LIMIT
            );
            for (JSONObject banner : rsBannerList) {
                int id = ConvertUtils.toInt(banner.get("id"));
                ClientResponse rsStop = this.deactivateBannerOne(
                        0,
                        id
                );
                if (rsStop.failed()) {
                    this.alertToTelegram("runStopBannerSchedule: " + id + " FAILED", ResponseStatus.FAIL);
                }
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.NOTIFY.name());
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }
}