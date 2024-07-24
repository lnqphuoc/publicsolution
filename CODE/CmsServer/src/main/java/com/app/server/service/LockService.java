package com.app.server.service;

import com.app.server.config.ConfigInfo;
import com.app.server.constants.ResponseMessage;
import com.app.server.data.SessionData;
import com.app.server.data.dto.agency.AgencyBasicData;
import com.app.server.data.dto.agency.Membership;
import com.app.server.data.dto.location.City;
import com.app.server.data.dto.location.Region;
import com.app.server.data.dto.product.ProductData;
import com.app.server.data.entity.AgencyLockSettingEntity;
import com.app.server.data.entity.NotifyHistoryEntity;
import com.app.server.data.extra.TypeFilter;
import com.app.server.data.request.BasicRequest;
import com.app.server.data.request.BasicResponse;
import com.app.server.data.request.FilterListRequest;
import com.app.server.data.request.FilterRequest;
import com.app.server.data.request.lock.CreateSettingLockAgencyRequest;
import com.app.server.data.request.lock.FilterLockListRequest;
import com.app.server.database.AgencyDB;
import com.app.server.database.NotifyDB;
import com.app.server.enums.*;
import com.app.server.manager.DataManager;
import com.app.server.response.ClientResponse;
import com.app.server.utils.AppUtils;
import com.app.server.utils.FilterUtils;
import com.app.server.utils.JsonUtils;
import com.google.gson.reflect.TypeToken;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import springfox.documentation.spring.web.json.Json;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class LockService {
    private FilterUtils filterUtils;

    @Autowired
    public void setFilterUtils(FilterUtils filterUtils) {
        this.filterUtils = filterUtils;
    }

    private AgencyDB agencyDB;

    @Autowired
    public void setAgencyDB(AgencyDB agencyDB) {
        this.agencyDB = agencyDB;
    }

    private AppUtils appUtils;

    @Autowired
    public void setAppUtils(AppUtils appUtils) {
        this.appUtils = appUtils;
    }

    private DataManager dataManager;

    @Autowired
    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    private NotifyDB notifyDB;

    @Autowired
    public void setNotifyDB(NotifyDB notifyDB) {
        this.notifyDB = notifyDB;
    }

    public ClientResponse createSettingLockAgency(SessionData sessionData, CreateSettingLockAgencyRequest request) {
        try {
            ClientResponse cr = request.validate();
            if (cr.failed()) {
                return cr;
            }
            AgencyLockSettingEntity entity = new AgencyLockSettingEntity();
            entity.setSetting_object_data(JsonUtils.Serialize(request.getSetting_object_data()));
            entity.setSetting_object_type(request.getSetting_object_type());
            entity.setOption_lock(request.getOption_lock());
            entity.setDay_lock(request.getDay_lock());
            entity.setStatus(ProductPriceTimerStatus.DRAFT.getId());
            entity.setStart_date(
                    request.getStart_date_millisecond() == 0 ? DateTimeUtils.getNow() :
                            DateTimeUtils.getDateTime(request.getStart_date_millisecond())
            );
            entity.setCreator_id(sessionData.getId());
            int rsInsert = this.agencyDB.insertAgencyLockSetting(entity);
            if (rsInsert <= 0) {
                entity.setId(rsInsert);
            }

            this.agencyDB.updateCodeForAgencyLockSetting(
                    rsInsert,
                    "ALS" + rsInsert);

            JSONObject data = new JSONObject();
            data.put("entity", entity);
            cr.setData(data);
            return cr;
        } catch (Exception ex) {
            LogUtil.printDebug("LOCK", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse approveSettingLockAgency(SessionData sessionData, BasicRequest request) {
        try {
            JSONObject setting = this.agencyDB.getSettingLockAgency(
                    request.getId()
            );
            if (setting == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            AgencyLockSettingEntity entity = AgencyLockSettingEntity.from(
                    setting
            );

            if (entity.getStatus() != ProductPriceTimerStatus.DRAFT.getId()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            Date now = DateTimeUtils.getNow();

            if (entity.getOption_lock() == LockOptionType.KHOA_NGAY.getId()) {
                entity.setStart_date(now);
                boolean rsApprove = this.agencyDB.approveAgencyLockSetting(
                        entity.getId(),
                        entity.getStart_date(),
                        ProductPriceTimerStatus.RUNNING.getId(),
                        sessionData.getId()
                );
                if (!rsApprove) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                this.saveAgencyLockData(entity);

                /**
                 * Thực hiện lệnh khóa
                 */
                this.callLockNowAgency(entity);
            } else if (entity.getStart_date().before(now)) {
                entity.setStart_date(now);
                boolean rsApprove = this.agencyDB.approveAgencyLockSetting(
                        entity.getId(),
                        entity.getStart_date(),
                        ProductPriceTimerStatus.RUNNING.getId(),
                        sessionData.getId()
                );

                this.saveAgencyLockData(entity);

                resetLockCheckDate(entity);
            } else {
                boolean rsApprove = this.agencyDB.approveAgencyLockSetting(
                        entity.getId(),
                        entity.getStart_date(),
                        ProductPriceTimerStatus.WAITING.getId(),
                        sessionData.getId()
                );
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("LOCK", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private void saveAgencyLockData(
            AgencyLockSettingEntity entity) {
        try {
            int status = LockDataStatus.RUNNING.getId();
            List<Integer> idList = JsonUtils.DeSerialize(
                    entity.getSetting_object_data().toString(),
                    new TypeToken<List<Integer>>() {
                    }.getType());
            for (Integer id : idList) {
                JSONObject jsData = this.agencyDB.getAgencyLockData(entity.getSetting_object_type(), id);
                if (jsData == null) {
                    this.agencyDB.insertAgencyLockData(
                            entity.getSetting_object_type(),
                            id,
                            entity.getOption_lock(),
                            entity.getDay_lock(),
                            entity.getCode(),
                            status
                    );
                } else {
                    this.agencyDB.updateAgencyLockData(
                            ConvertUtils.toInt(jsData.get("id")),
                            entity.getSetting_object_type(),
                            id,
                            entity.getOption_lock(),
                            entity.getDay_lock(),
                            entity.getCode(),
                            status
                    );
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("LOCK", ex);
        }
    }

    private void callLockNowAgency(AgencyLockSettingEntity entity) {
        try {
            List<JSONObject> agencyList = null;
            if (entity.getSetting_object_type().equals(SettingObjectType.AGENCY.getCode())) {
                agencyList = this.agencyDB.getListAgencyInAgency(
                        entity.getSetting_object_data()
                );
            } else if (entity.getSetting_object_type().equals(SettingObjectType.CITY.getCode())) {
                agencyList = this.agencyDB.getListAgencyInCity(
                        entity.getSetting_object_data()
                );
            } else if (entity.getSetting_object_type().equals(SettingObjectType.REGION.getCode())) {
                agencyList = this.agencyDB.getListAgencyInRegion(
                        entity.getSetting_object_data()
                );
            } else if (entity.getSetting_object_type().equals(SettingObjectType.MEMBERSHIP.getCode())) {
                agencyList = this.agencyDB.getListAgencyInMembership(
                        entity.getSetting_object_data()
                );
            }

            if (agencyList != null && !agencyList.isEmpty()) {
                for (JSONObject agency : agencyList) {
                    JSONObject agencyLock = this.agencyDB.getAgencyLockData(
                            SettingObjectType.AGENCY.getCode(),
                            ConvertUtils.toInt(agency.get("id"))
                    );
                    if (agencyLock != null &&
                            ConvertUtils.toInt(agencyLock.get("status")) == LockDataStatus.RUNNING.getId() &&
                            ConvertUtils.toInt(agencyLock.get("option_lock")) == LockOptionType.KHOA_NGAY.getId()
                    ) {
                        lockAgencyList(entity.getCode(), Arrays.asList(agency));
                        continue;
                    }

                    JSONObject cityLock = this.agencyDB.getAgencyLockData(
                            SettingObjectType.CITY.getCode(),
                            ConvertUtils.toInt(agency.get("city_id"))
                    );
                    if (cityLock != null &&
                            ConvertUtils.toInt(cityLock.get("status")) == LockDataStatus.RUNNING.getId() &&
                            ConvertUtils.toInt(cityLock.get("option_lock")) == LockOptionType.KHOA_NGAY.getId()
                    ) {
                        lockAgencyList(entity.getCode(), Arrays.asList(agency));
                        continue;
                    }

                    JSONObject regionLock = this.agencyDB.getAgencyLockData(
                            SettingObjectType.REGION.getCode(),
                            ConvertUtils.toInt(agency.get("region_id"))
                    );
                    if (regionLock != null &&
                            ConvertUtils.toInt(regionLock.get("status")) == LockDataStatus.RUNNING.getId() &&
                            ConvertUtils.toInt(regionLock.get("option_lock")) == LockOptionType.KHOA_NGAY.getId()) {
                        lockAgencyList(entity.getCode(), Arrays.asList(agency));
                        continue;
                    }

                    JSONObject membershipLock = this.agencyDB.getAgencyLockData(
                            SettingObjectType.MEMBERSHIP.getCode(),
                            ConvertUtils.toInt(agency.get("membership_id"))
                    );
                    if (membershipLock != null &&
                            ConvertUtils.toInt(membershipLock.get("status")) == LockDataStatus.RUNNING.getId() &&
                            ConvertUtils.toInt(membershipLock.get("option_lock")) == LockOptionType.KHOA_NGAY.getId()) {
                        lockAgencyList(entity.getCode(), Arrays.asList(agency));
                        continue;
                    }
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("LOCK", ex);
        }
    }

    private void resetLockCheckDate(AgencyLockSettingEntity entity) {
        try {
            List<JSONObject> agencyList = null;
            if (entity.getSetting_object_type().equals(SettingObjectType.AGENCY.getCode())) {
                agencyList = this.agencyDB.getListAgencyInAgency(
                        entity.getSetting_object_data()
                );
            } else if (entity.getSetting_object_type().equals(SettingObjectType.CITY.getCode())) {
                agencyList = this.agencyDB.getListAgencyInCity(
                        entity.getSetting_object_data()
                );
            } else if (entity.getSetting_object_type().equals(SettingObjectType.REGION.getCode())) {
                agencyList = this.agencyDB.getListAgencyInRegion(
                        entity.getSetting_object_data()
                );
            } else if (entity.getSetting_object_type().equals(SettingObjectType.MEMBERSHIP.getCode())) {
                agencyList = this.agencyDB.getListAgencyInMembership(
                        entity.getSetting_object_data()
                );
            }

            SettingObjectType main = SettingObjectType.from(entity.getSetting_object_type());

            if (agencyList != null && !agencyList.isEmpty()) {
                for (JSONObject agency : agencyList) {
                    boolean skip = false;
                    for (SettingObjectType settingObjectType : SettingObjectType.values()) {
                        if (settingObjectType.getId() < main.getId()) {
                            //int setting_object_data = this.getSettin
                            JSONObject agencyLock = this.agencyDB.getAgencyLockData(
                                    settingObjectType.getCode(),
                                    ConvertUtils.toInt(agency.get("id"))
                            );
                            if (agencyLock == null &&
                                    ConvertUtils.toInt(agencyLock.get("status")) == LockDataStatus.RUNNING.getId()
                            ) {
                                skip = true;
                                break;
                            }
                        }
                    }
                    if (!skip) {
                        this.resetLockCheckDateForAgency(
                                agency);
                    }
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("LOCK", ex);
        }
    }

    private void resetLockCheckDateForAgency(JSONObject agency) {
        try {
            Date dept_order_date = AppUtils.convertJsonToDate(
                    agency.get("dept_order_date"));
            Date lock_check_date = AppUtils.convertJsonToDate(
                    agency.get("lock_check_date"));
            if (dept_order_date == null || lock_check_date.after(dept_order_date)) {
                this.agencyDB.resetLockCheckDate(
                        ConvertUtils.toInt(agency.get("id"))
                );
            }
        } catch (Exception ex) {
            LogUtil.printDebug("LOCK", ex);
        }
    }

    private void lockAgencyList(String code, List<JSONObject> agencyList) {
        try {
            for (JSONObject agency : agencyList) {
                /**
                 * validate update status
                 */
                if (ConvertUtils.toInt(agency.get("status")) != AgencyStatus.APPROVED.getValue()) {
                    continue;
                }

                boolean rsLock = this.agencyDB.lockAgency(
                        ConvertUtils.toInt(agency.get("id")),
                        AgencyStatus.LOCK.getValue());
                if (!rsLock) {
                    continue;
                }

                /**
                 * Lưu lịch sử
                 */
                this.agencyDB.saveAgencyLockHistory(
                        ConvertUtils.toInt(agency.get("id")),
                        0,
                        "Mã thiết lập khóa: " + code,
                        1
                );
            }
        } catch (Exception ex) {
            LogUtil.printDebug("LOCK", ex);
        }
    }

    public ClientResponse cancelSettingLockAgency(SessionData sessionData, BasicRequest request) {
        try {
            JSONObject setting = this.agencyDB.getSettingLockAgency(
                    request.getId()
            );
            if (setting == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            AgencyLockSettingEntity entity = AgencyLockSettingEntity.from(
                    setting
            );

            if (entity.getStatus() == ProductPriceTimerStatus.RUNNING.getId()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            boolean rs = this.agencyDB.cancelSettingLockAgency(request.getId(), sessionData.getId());
            if (!rs) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("LOCK", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterSettingLockAgency(SessionData sessionData, FilterLockListRequest request) {
        try {
            for (FilterRequest filterRequest : request.getFilters()) {
                if (filterRequest.getKey().equals("agency_id")) {
                    filterRequest.setType(TypeFilter.SQL);
                    filterRequest.setValue(
                            "( setting_object_type = 'AGENCY'" +
                                    " AND setting_object_data " + " LIKE CONCAT('%\"'," + filterRequest.getValue() + " ,'\"%')" +
                                    ")"
                    );
                } else if (filterRequest.getKey().equals("city_id")) {
                    filterRequest.setType(TypeFilter.SQL);
                    filterRequest.setValue(
                            "( setting_object_type = 'CITY'" +
                                    " AND setting_object_data " + " LIKE CONCAT('%\"'," + filterRequest.getValue() + " ,'\"%')" +
                                    ")"
                    );
                } else if (filterRequest.getKey().equals("region_id")) {
                    filterRequest.setType(TypeFilter.SQL);
                    filterRequest.setValue(
                            "( setting_object_type = 'REGION'" +
                                    " AND setting_object_data " + " LIKE CONCAT('%\"'," + filterRequest.getValue() + " ,'\"%')" +
                                    ")"
                    );
                } else if (filterRequest.getKey().equals("membership_id")) {
                    filterRequest.setType(TypeFilter.SQL);
                    filterRequest.setValue(
                            "( setting_object_type = 'MEMBERSHIP'" +
                                    " AND setting_object_data " + " LIKE CONCAT('%\"'," + filterRequest.getValue() + " ,'\"%')" +
                                    ")"
                    );
                }
            }
            String query = this.filterUtils.getQuery(FunctionList.FILTER_AGENCY_LOCK_SETTING, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.agencyDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, 1);
            for (JSONObject js : records) {
                js.put("creator_info", this.dataManager.getStaffManager().getStaff(
                        ConvertUtils.toInt(js.get("creator_id"))
                ));

                String setting_object_data = ConvertUtils.toString(
                        js.get("setting_object_data")
                );

                String setting_object_type = ConvertUtils.toString(
                        js.get("setting_object_type")
                );
                if (setting_object_type.equals(SettingObjectType.AGENCY.getCode())) {
                    List<String> strIdList = JsonUtils.DeSerialize(
                            setting_object_data,
                            new TypeToken<List<String>>() {
                            }.getType()
                    );
                    List<AgencyBasicData> agency_list = new ArrayList<>();
                    for (String strId : strIdList) {
                        agency_list.add(
                                this.dataManager.getAgencyManager().getAgencyBasicData(
                                        ConvertUtils.toInt(strId)
                                )
                        );
                    }
                    js.put("agency_list", agency_list);
                } else if (setting_object_type.equals(SettingObjectType.CITY.getCode())) {
                    List<String> strIdList = JsonUtils.DeSerialize(
                            setting_object_data,
                            new TypeToken<List<String>>() {
                            }.getType()
                    );
                    List<BasicResponse> objectList = new ArrayList<>();
                    for (String strId : strIdList) {
                        City cityData = this.dataManager.getProductManager().getMpCity().get(
                                ConvertUtils.toInt(strId)
                        );
                        if (cityData != null) {
                            BasicResponse cityResponse = new BasicResponse(cityData.getId(), cityData.getName());
                            cityResponse.setRegion_id(cityData.getRegion().getId());
                            objectList.add(cityResponse
                            );
                        }
                    }
                    js.put("setting_object_data", objectList);
                } else if (setting_object_type.equals(SettingObjectType.REGION.getCode())) {
                    List<String> strIdList = JsonUtils.DeSerialize(
                            setting_object_data,
                            new TypeToken<List<String>>() {
                            }.getType()
                    );
                    List<BasicResponse> objectList = new ArrayList<>();
                    for (String strId : strIdList) {
                        Region cityData = this.dataManager.getProductManager().getMpRegion().get(
                                ConvertUtils.toInt(strId)
                        );
                        if (cityData != null) {
                            BasicResponse cityResponse = new BasicResponse(cityData.getId(), cityData.getName());
                            objectList.add(cityResponse
                            );
                        }
                    }
                    js.put("setting_object_data", objectList);
                } else if (setting_object_type.equals(SettingObjectType.MEMBERSHIP.getCode())) {
                    List<String> strIdList = JsonUtils.DeSerialize(
                            setting_object_data,
                            new TypeToken<List<String>>() {
                            }.getType()
                    );
                    List<BasicResponse> objectList = new ArrayList<>();
                    for (String strId : strIdList) {
                        Membership cityData = this.dataManager.getProductManager().getMpMembership().get(
                                ConvertUtils.toInt(strId)
                        );
                        if (cityData != null) {
                            BasicResponse cityResponse = new BasicResponse(cityData.getId(), cityData.getName());
                            objectList.add(cityResponse
                            );
                        }
                    }
                    js.put("setting_object_data", objectList);
                }
            }
            int total = this.agencyDB.getTotal(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("LOCK", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse getSettingLockAgencyDetail(SessionData sessionData, BasicRequest request) {
        try {
            JSONObject setting = this.agencyDB.getSettingLockAgency(
                    request.getId()
            );
            if (setting == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            String setting_object_data = ConvertUtils.toString(
                    setting.get("setting_object_data")
            );

            String setting_object_type = ConvertUtils.toString(
                    setting.get("setting_object_type")
            );
            if (setting_object_type.equals(SettingObjectType.AGENCY.getCode())) {
                List<String> strIdList = JsonUtils.DeSerialize(
                        setting_object_data,
                        new TypeToken<List<String>>() {
                        }.getType()
                );
                List<AgencyBasicData> agency_list = new ArrayList<>();
                for (String strId : strIdList) {
                    agency_list.add(
                            this.dataManager.getAgencyManager().getAgencyBasicData(
                                    ConvertUtils.toInt(strId)
                            )
                    );
                }
                setting.put("agency_list", agency_list);
            } else if (setting_object_type.equals(SettingObjectType.CITY.getCode())) {
                List<String> strIdList = JsonUtils.DeSerialize(
                        setting_object_data,
                        new TypeToken<List<String>>() {
                        }.getType()
                );
                List<BasicResponse> objectList = new ArrayList<>();
                for (String strId : strIdList) {
                    City cityData = this.dataManager.getProductManager().getMpCity().get(
                            ConvertUtils.toInt(strId)
                    );
                    if (cityData != null) {
                        BasicResponse cityResponse = new BasicResponse(cityData.getId(), cityData.getName());
                        cityResponse.setRegion_id(cityData.getRegion().getId());
                        objectList.add(cityResponse
                        );
                    }
                }
                setting.put("setting_object_data", objectList);
            } else if (setting_object_type.equals(SettingObjectType.REGION.getCode())) {
                List<String> strIdList = JsonUtils.DeSerialize(
                        setting_object_data,
                        new TypeToken<List<String>>() {
                        }.getType()
                );
                List<BasicResponse> objectList = new ArrayList<>();
                for (String strId : strIdList) {
                    Region cityData = this.dataManager.getProductManager().getMpRegion().get(
                            ConvertUtils.toInt(strId)
                    );
                    if (cityData != null) {
                        BasicResponse cityResponse = new BasicResponse(cityData.getId(), cityData.getName());
                        objectList.add(cityResponse
                        );
                    }
                }
                setting.put("setting_object_data", objectList);
            } else if (setting_object_type.equals(SettingObjectType.MEMBERSHIP.getCode())) {
                List<String> strIdList = JsonUtils.DeSerialize(
                        setting_object_data,
                        new TypeToken<List<String>>() {
                        }.getType()
                );
                List<BasicResponse> objectList = new ArrayList<>();
                for (String strId : strIdList) {
                    Membership cityData = this.dataManager.getProductManager().getMpMembership().get(
                            ConvertUtils.toInt(strId)
                    );
                    if (cityData != null) {
                        BasicResponse cityResponse = new BasicResponse(cityData.getId(), cityData.getName());
                        objectList.add(cityResponse
                        );
                    }
                }
                setting.put("setting_object_data", objectList);
            }

            JSONObject data = new JSONObject();
            data.put("setting", setting);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("LOCK", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterAgencyLockData(SessionData sessionData, FilterLockListRequest request) {
        try {
            for (FilterRequest filterRequest : request.getFilters()) {
                if (filterRequest.getKey().equals("agency_id")) {
                    filterRequest.setType(TypeFilter.SQL);
                    filterRequest.setValue(
                            "( setting_object_type = 'AGENCY'" +
                                    " AND setting_object_data = " + filterRequest.getValue() +
                                    ")"
                    );
                } else if (filterRequest.getKey().equals("city_id")) {
                    filterRequest.setType(TypeFilter.SQL);
                    filterRequest.setValue(
                            "( setting_object_type = 'CITY'" +
                                    " AND setting_object_data = " + filterRequest.getValue() +
                                    ")"
                    );
                } else if (filterRequest.getKey().equals("region_id")) {
                    filterRequest.setType(TypeFilter.SQL);
                    filterRequest.setValue(
                            "( setting_object_type = 'REGION'" +
                                    " AND setting_object_data = " + filterRequest.getValue() +
                                    ")"
                    );
                } else if (filterRequest.getKey().equals("membership_id")) {
                    filterRequest.setType(TypeFilter.SQL);
                    filterRequest.setValue(
                            "( setting_object_type = 'MEMBERSHIP'" +
                                    " AND setting_object_data = " + filterRequest.getValue() +
                                    ")"
                    );
                }
            }

            String query = this.filterUtils.getQuery(FunctionList.FILTER_AGENCY_LOCK_DATA, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.agencyDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, 1);
            for (JSONObject js : records) {
                SettingObjectType settingObjectType = SettingObjectType.from(
                        ConvertUtils.toString(js.get("setting_object_type"))
                );
                int setting_object_data = ConvertUtils.toInt(js.get("setting_object_data"));
                switch (settingObjectType) {
                    case AGENCY:
                        js.put("agency_info",
                                this.dataManager.getAgencyManager().getAgencyBasicData(
                                        setting_object_data
                                ));
                        break;
                }
            }

            data.put("day_number_push_notify_lock", this.dataManager.getConfigManager().getDayNumberPushNotifyLock());

            int total = this.agencyDB.getTotal(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("LOCK", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterAgencyLockHistory(SessionData sessionData, FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.FILTER_AGENCY_LOCK_HISTORY, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.agencyDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, 1);
            for (JSONObject js : records) {
                js.put("agency_info", this.dataManager.getAgencyManager().getAgencyBasicData(
                        ConvertUtils.toInt(js.get("agency_id"))
                ));
                js.put("creator_info", this.dataManager.getStaffManager().getStaff(
                        ConvertUtils.toInt(js.get("creator_id"))
                ));
            }
            int total = this.agencyDB.getTotal(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse setDayNumberPushNotifyLock(SessionData sessionData, BasicRequest request) {
        try {
            if (request.getId() < 0) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.DATA_INVALID);
            }
            boolean rs = this.dataManager.getConfigManager().updateDayNumberPushNotifyLock(
                    request.getId()
            );
            if (!rs) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse runLockSchedule() {
        try {
            Date now = DateTimeUtils.getNow();
            List<JSONObject> agencyActiveList =
                    this.agencyDB.getListAgencyActive();
            for (JSONObject agency : agencyActiveList) {
                JSONObject agencyLock = this.agencyDB.getAgencyLockData(
                        SettingObjectType.AGENCY.getCode(),
                        ConvertUtils.toInt(agency.get("id"))
                );
                if (agencyLock != null &&
                        ConvertUtils.toInt(agencyLock.get("status")) == LockDataStatus.RUNNING.getId()) {
                    this.excuteLock(agency, agencyLock, now);
                    continue;
                }

                JSONObject cityLock = this.agencyDB.getAgencyLockData(
                        SettingObjectType.CITY.getCode(),
                        ConvertUtils.toInt(agency.get("city_id"))
                );
                if (cityLock != null &&
                        ConvertUtils.toInt(cityLock.get("status")) == LockDataStatus.RUNNING.getId()) {
                    this.excuteLock(agency, cityLock, now);
                    continue;
                }

                JSONObject regionLock = this.agencyDB.getAgencyLockData(
                        SettingObjectType.REGION.getCode(),
                        ConvertUtils.toInt(agency.get("region_id"))
                );
                if (regionLock != null &&
                        ConvertUtils.toInt(regionLock.get("status")) == LockDataStatus.RUNNING.getId()) {
                    this.excuteLock(agency, regionLock, now);
                    continue;
                }

                JSONObject membershipLock = this.agencyDB.getAgencyLockData(
                        SettingObjectType.MEMBERSHIP.getCode(),
                        ConvertUtils.toInt(agency.get("membership_id"))
                );
                if (membershipLock != null &&
                        ConvertUtils.toInt(membershipLock.get("status")) == LockDataStatus.RUNNING.getId()) {
                    this.excuteLock(agency, membershipLock, now);
                    continue;
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private void excuteLock(JSONObject agency, JSONObject setting, Date now) {
        try {
            if (LockOptionType.KHOA_CUOI_NGAY.getId() == ConvertUtils.toInt(setting.get("option_lock"))) {
                if (AppUtils.convertJsonToDate(setting.get("effect_date")).before(now)) {
                    this.lockAgencyList(
                            ConvertUtils.toString(setting.get("agency_lock_setting_code")),
                            Arrays.asList(agency)
                    );
                }
            } else if (LockOptionType.KHOA_N_NGAY.getId() == ConvertUtils.toInt(setting.get("option_lock"))) {
                int day_lock = ConvertUtils.toInt(setting.get("day_lock"));
                Date lock_check_date = AppUtils.convertJsonToDate(agency.get("lock_check_date"));
                Date dateLock = this.appUtils.getDateAfterDay(
                        lock_check_date, day_lock
                );
                if (dateLock.getTime() <= now.getTime()) {
                    this.lockAgencyList(
                            ConvertUtils.toString(setting.get("agency_lock_setting_code")),
                            Arrays.asList(agency)
                    );
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
    }

    public ClientResponse runPushNotifyWarningLock() {
        try {
            /**
             * Tài khoản của Quý khách sẽ bị vô hiệu hóa sau n ngày do quá [số ngày thiết lập] ngày không có phát sinh giao dịch. Chúng tôi xin lỗi vì sự bất tiện nà
             */
            int dayNumberPushNotifyLock = this.dataManager.getConfigManager().getDayNumberPushNotifyLock();
            if (dayNumberPushNotifyLock == 0) {
                return ClientResponse.success(null);
            }

            Date now = DateTimeUtils.getNow();
            List<JSONObject> agencyActiveList =
                    this.agencyDB.getListAgencyActive();
            for (JSONObject agency : agencyActiveList) {
                JSONObject agencyLock = this.agencyDB.getAgencyLockData(
                        SettingObjectType.AGENCY.getCode(),
                        ConvertUtils.toInt(agency.get("id"))
                );
                if (agencyLock != null &&
                        ConvertUtils.toInt(agencyLock.get("status")) == LockDataStatus.RUNNING.getId()) {
                    if (this.checkPushNotifyWarningLock(
                            agency,
                            agencyLock,
                            now,
                            dayNumberPushNotifyLock)) {
                        this.pushNotifyWarningLock(
                                ConvertUtils.toInt(
                                        agency.get("id")
                                ),
                                dayNumberPushNotifyLock,
                                ConvertUtils.toInt(agencyLock.get("day_lock"))
                        );
                    }
                }

                JSONObject cityLock = this.agencyDB.getAgencyLockData(
                        SettingObjectType.CITY.getCode(),
                        ConvertUtils.toInt(agency.get("city_id"))
                );
                if (cityLock != null &&
                        ConvertUtils.toInt(cityLock.get("status")) == LockDataStatus.RUNNING.getId()) {
                    if (this.checkPushNotifyWarningLock(
                            agency,
                            cityLock,
                            now,
                            dayNumberPushNotifyLock)) {
                        this.pushNotifyWarningLock(
                                ConvertUtils.toInt(
                                        agency.get("id")
                                ),
                                dayNumberPushNotifyLock,
                                ConvertUtils.toInt(cityLock.get("day_lock"))
                        );
                    }
                    continue;
                }

                JSONObject regionLock = this.agencyDB.getAgencyLockData(
                        SettingObjectType.REGION.getCode(),
                        ConvertUtils.toInt(agency.get("city_id"))
                );
                if (regionLock != null &&
                        ConvertUtils.toInt(regionLock.get("status")) == LockDataStatus.RUNNING.getId()) {
                    if (this.checkPushNotifyWarningLock(
                            agency,
                            regionLock,
                            now,
                            dayNumberPushNotifyLock)) {
                        this.pushNotifyWarningLock(
                                ConvertUtils.toInt(
                                        agency.get("id")
                                ),
                                dayNumberPushNotifyLock,
                                ConvertUtils.toInt(regionLock.get("day_lock"))
                        );
                    }
                    continue;
                }

                JSONObject membershipLock = this.agencyDB.getAgencyLockData(
                        SettingObjectType.MEMBERSHIP.getCode(),
                        ConvertUtils.toInt(agency.get("city_id"))
                );
                if (membershipLock != null &&
                        ConvertUtils.toInt(membershipLock.get("status")) == LockDataStatus.RUNNING.getId()) {
                    if (this.checkPushNotifyWarningLock(
                            agency,
                            membershipLock,
                            now,
                            dayNumberPushNotifyLock)) {
                        this.pushNotifyWarningLock(
                                ConvertUtils.toInt(
                                        agency.get("id")
                                ),
                                dayNumberPushNotifyLock,
                                ConvertUtils.toInt(membershipLock.get("day_lock"))
                        );
                    }
                    continue;
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private boolean checkPushNotifyWarningLock(
            JSONObject agency,
            JSONObject setting,
            Date now,
            int dayNumberPushNotifyLock) {
        try {
            if (LockOptionType.KHOA_N_NGAY.getId() == ConvertUtils.toInt(setting.get("option_lock"))) {
                int day_lock = ConvertUtils.toInt(setting.get("day_lock"));
                Date lock_check_date = AppUtils.convertJsonToDate(agency.get("lock_check_date"));
                Date dateLock = this.appUtils.getDateAfterDay(
                        lock_check_date, day_lock
                );
                if (ConvertUtils.toInt(TimeUnit.MILLISECONDS.toDays(dateLock.getTime() - now.getTime())) == dayNumberPushNotifyLock) {
                    return true;
                }
            }
            return false;
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }

        return false;
    }

    private ClientResponse pushNotifyWarningLock(
            int agency_id,
            int n_ngay,
            int day_lock
    ) {
        try {

            /**
             * Lưu lịch sử
             */
            String title = "Thông báo";
            String description = "Tài khoản của Quý khách sẽ bị vô hiệu hóa sau " + n_ngay + " ngày do quá " +
                    day_lock + " ngày không có phát sinh giao dịch. Chúng tôi xin lỗi vì sự bất tiện này";
            boolean rsSaveHistory = this.saveNotifyHistory(
                    "Thông báo",
                    "",
                    description,
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
            if (!rsSaveHistory) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            List<String> fbAgencyList = this.getListFirebaseAgency(
                    "[\"" + agency_id + "\"]",
                    "[]",
                    "[]",
                    "[]"
            );
            if (!fbAgencyList.isEmpty()) {
                this.notifyDB.insertNotifyWaitingPush(
                        JsonUtils.Serialize(fbAgencyList),
                        title,
                        description,
                        "",
                        NotifyWaitingPushStatus.WAITING.getId());
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("LOCK", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    protected List<String> getListFirebaseAgency(
            String agency_ids,
            String city_ids,
            String region_ids,
            String membership_ids) {
        List<JSONObject> accountList = this.agencyDB.getListFirebaseAgencyAccount(
                agency_ids,
                city_ids,
                region_ids,
                membership_ids
        );

        List<String> fbList = accountList.stream().map(
                e -> e.get("firebase_token").toString()
        ).collect(Collectors.toList());

        return fbList;
    }

    protected boolean saveNotifyHistory(
            String name,
            String image,
            String description,
            Integer status,
            Date created_date,
            Integer creator_id,
            String agency_ids,
            String city_ids,
            String region_ids,
            String membership_ids,
            String setting_type,
            String setting_value) {
        try {
            NotifyHistoryEntity notifyHistoryEntity = new NotifyHistoryEntity();

            /*
            private String name;
             */
            notifyHistoryEntity.setName(name);
            /*
            private String image;
             */
            notifyHistoryEntity.setImage(image);
            /*
             description;
             */
            notifyHistoryEntity.setDescription(description);
            /*
            private int status = 1;
             */
            notifyHistoryEntity.setStatus(1);
            /*
            private Date created_date;
             */
            notifyHistoryEntity.setCreated_date(created_date);
            /*
            private Integer creator_id;
             */
            notifyHistoryEntity.setCreator_id(creator_id);

            /*
            private String agency_ids = "[]";
             */
            notifyHistoryEntity.setAgency_ids(agency_ids);

            /*
            private String city_ids = "[]";
            */
            notifyHistoryEntity.setCity_ids(city_ids);
            /*
            private String region_ids = "[]";
             */
            notifyHistoryEntity.setRegion_ids(region_ids);
            /*
            private String membership_ids = "[]";
             */
            notifyHistoryEntity.setMembership_ids(membership_ids);
            /*
            private String setting_type;
             */
            notifyHistoryEntity.setSetting_type(setting_type);
            /*
            private String setting_value;
             */
            notifyHistoryEntity.setSetting_value(setting_value);
            int rsInsert = this.notifyDB.insertNotifyHistory(notifyHistoryEntity);
            if (rsInsert <= 0) {
                return false;
            }
            return true;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.NOTIFY.name(), ex);
        }
        return false;
    }

    public ClientResponse runAgencyLockSettingSchedule() {
        try {
            Date now = DateTimeUtils.getNow();
            List<JSONObject> settingList = this.agencyDB.getAgencyLockSettingWaiting();
            for (JSONObject setting : settingList) {
                boolean rsActive = this.agencyDB.activeAgencyLockSetting(
                        ConvertUtils.toInt(setting.get("id"))
                );

                AgencyLockSettingEntity entity = AgencyLockSettingEntity.from(
                        setting
                );
                if (entity.getOption_lock() == LockOptionType.KHOA_NGAY.getId()) {
                    this.saveAgencyLockData(entity);
                    /**
                     * Thực hiện lệnh khóa
                     */
                    this.callLockNowAgency(entity);
                } else {
                    this.saveAgencyLockData(entity);

                    resetLockCheckDate(entity);
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse stopAgencyLockData(SessionData sessionData, BasicRequest request) {
        try {
            JSONObject agencyLockData = this.agencyDB.getAgencyLockDataById(
                    request.getId()
            );
            if (agencyLockData == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            boolean rs = this.agencyDB.stopAgencyLockData(
                    request.getId());
            if (!rs) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("LOCK", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }
}