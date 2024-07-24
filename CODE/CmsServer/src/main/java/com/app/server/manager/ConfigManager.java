package com.app.server.manager;

import com.app.server.config.ConfigInfo;
import com.app.server.constants.ConfigConstants;
import com.app.server.constants.DeptConstants;
import com.app.server.constants.MissionConstants;
import com.app.server.data.dto.mission.TimePartData;
import com.app.server.data.dto.mission.TimeToWorkData;
import com.app.server.data.dto.notify.NotifyData;
import com.app.server.data.entity.DeptTransactionSubTypeEntity;
import com.app.server.database.MasterDB;
import com.app.server.enums.ConfigType;
import com.app.server.enums.NotifyAutoContentType;
import com.app.server.utils.JsonUtils;
import com.google.common.reflect.TypeToken;
import com.mysql.cj.log.Log;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import io.swagger.models.auth.In;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ConfigManager {
    private Map<String, String> mpConfig = new LinkedHashMap<>();
    private Map<Integer, DeptTransactionSubTypeEntity> mpHangMucCongViec = new LinkedHashMap<>();
    private Map<Integer, JSONObject> mpProductHotType = new LinkedHashMap<>();
    private Map<String, String> mpMissionConfig = new LinkedHashMap<>();
    private Map<Integer, TimeToWorkData> mpTimeToWork = new LinkedHashMap<>();
    private Map<Integer, JSONObject> mpAgencyStatus = new LinkedHashMap<>();
    private Map<Integer, JSONObject> mpOrderStatus = new LinkedHashMap<>();
    private Map<Integer, JSONObject> mpPromoStatus = new LinkedHashMap<>();

    private MasterDB masterDB;

    @Autowired
    public void setMasterDB(MasterDB masterDB) {
        this.masterDB = masterDB;
    }


    public void loadData() {
        this.reloadConfig();
        this.loadHangMucCongViec();
        this.loadProductHotType();
        this.loadMissionConfig();
        this.loadTimeToWork();
        this.loadAgencyStatus();
        this.loadOrderStatus();
        this.loadPromoStatus();

        ConfigInfo.READY = true;
    }

    private void loadAgencyStatus() {
        this.mpAgencyStatus.clear();
        List<JSONObject> rs = this.masterDB.find("select * from agency_status");
        for (JSONObject js : rs) {
            mpAgencyStatus.put(ConvertUtils.toInt(js.get("id")), js);
        }
    }

    private void loadOrderStatus() {
        this.mpOrderStatus.clear();
        List<JSONObject> rs = this.masterDB.find("select * from order_status");
        for (JSONObject js : rs) {
            mpOrderStatus.put(ConvertUtils.toInt(js.get("id")), js);
        }
    }

    private void loadPromoStatus() {
        this.mpPromoStatus.clear();
        List<JSONObject> rs = this.masterDB.find("select * from promo_status");
        for (JSONObject js : rs) {
            mpPromoStatus.put(ConvertUtils.toInt(js.get("id")), js);
        }
    }

    private void loadTimeToWork() {
        this.mpTimeToWork.clear();
        if (this.mpConfig.get(MissionConstants.TIME_TO_WORK) != null) {
            List<JSONObject> data = JsonUtils.DeSerialize(
                    this.mpConfig.get(MissionConstants.TIME_TO_WORK),
                    new TypeToken<List<JSONObject>>() {
                    }.getType());
            data.forEach(
                    d -> {

                        TimeToWorkData timeToWorkData = new TimeToWorkData();

                        String morning_data = d.get("morning") == null ?
                                null :
                                d.get("morning").toString();
                        if (morning_data != null) {
                            TimePartData morning = new TimePartData();
                            String[] time = morning_data.split("-");
                            morning.setDate_from(DateTimeUtils.getDateTime(time[0], "HH:mm"));
                            morning.setDate_to(DateTimeUtils.getDateTime(time[1], "HH:mm"));
                            timeToWorkData.setMorning(morning);
                        }

                        String afternoon_data = d.get("afternoon") == null ?
                                null :
                                d.get("afternoon").toString();
                        if (afternoon_data != null) {
                            TimePartData afternoon = new TimePartData();
                            String[] time = afternoon_data.split("-");
                            afternoon.setDate_from(DateTimeUtils.getDateTime(time[0], "HH:mm"));
                            afternoon.setDate_to(DateTimeUtils.getDateTime(time[1], "HH:mm"));
                            timeToWorkData.setAfternoon(afternoon);
                        }

                        mpTimeToWork.put(
                                ConvertUtils.toInt(d.get("day_of_week")),
                                timeToWorkData);
                    }
            );
        }
    }

    public void loadMissionConfig() {
        this.mpMissionConfig.clear();
        List<JSONObject> rs = this.masterDB.find("select * from mission_config");
        for (JSONObject js : rs) {
            mpMissionConfig.put(ConvertUtils.toString(js.get("code")), ConvertUtils.toString(js.get("data")));
            if (ConvertUtils.toString(js.get("code")).equals(MissionConstants.THOI_GIAN_XAC_NHAN_HOAN_THANH_NHIEM_VU_MUA_HANG)) {
                MissionConstants.THOI_GIAN_DON_SOAN_HANG_TICH_LUY = this.convertThoiGian(ConvertUtils.toString(js.get("data")));
            }
        }
    }

    private int convertThoiGian(String data) {
        try {
            return ConvertUtils.toInt(data.split(":")[0]);
        } catch (Exception e) {
            LogUtil.printDebug("", e);
        }
        return 3;
    }

    public void loadProductHotType() {
        this.mpProductHotType.clear();
        List<JSONObject> rs = this.masterDB.find("select * from product_hot_type");
        for (JSONObject js : rs) {
            mpProductHotType.put(ConvertUtils.toInt(js.get("id")), js);
        }
    }

    private void loadHangMucCongViec() {
        this.mpHangMucCongViec.clear();
        List<JSONObject> rs = this.masterDB.find("select * from dept_transaction_sub_type");
        for (JSONObject js : rs) {
            mpHangMucCongViec.put(ConvertUtils.toInt(js.get("id")), JsonUtils.DeSerialize(JsonUtils.Serialize(js), DeptTransactionSubTypeEntity.class));
        }
    }

    private void reloadConfig() {
        this.mpConfig.clear();

        List<JSONObject> rs = this.masterDB.find("select * from config");
        for (JSONObject js : rs) {
            mpConfig.put(js.get("code").toString(), js.get("data").toString());
        }
    }

    public DeptTransactionSubTypeEntity getDeptTransactionByOrder() {
        JSONObject js = this.masterDB.getOne("SELECT * FROM dept_transaction_sub_type WHERE id=1");
        if (js != null) {
            return JsonUtils.DeSerialize(JsonUtils.Serialize(js), DeptTransactionSubTypeEntity.class);
        }
        return null;
    }

    public int getAcoinRateDefault() {
        return ConvertUtils.toInt(this.mpConfig.get("ACOIN_RATE_DEFAULT"));
    }

    public DeptTransactionSubTypeEntity getHanMucHuyDonHangHenGiao() {
        for (DeptTransactionSubTypeEntity deptTransactionSubTypeEntity : mpHangMucCongViec.values()) {
            if (deptTransactionSubTypeEntity.getCode() != null &&
                    deptTransactionSubTypeEntity.getCode().equals("CANCEL_ORDER_SCHEDULE")) {
                return deptTransactionSubTypeEntity;
            }
        }
        return null;
    }

    public DeptTransactionSubTypeEntity getHanMucDonHangHenGiao() {
        for (DeptTransactionSubTypeEntity deptTransactionSubTypeEntity : mpHangMucCongViec.values()) {
            if (deptTransactionSubTypeEntity.getCode() != null && deptTransactionSubTypeEntity.getCode().equals("ORDER_SCHEDULE")) {
                return deptTransactionSubTypeEntity;
            }
        }
        return null;
    }

    public DeptTransactionSubTypeEntity getHanMucDonHangHopDong() {
        for (DeptTransactionSubTypeEntity deptTransactionSubTypeEntity : mpHangMucCongViec.values()) {
            if (deptTransactionSubTypeEntity.getCode() != null &&
                    deptTransactionSubTypeEntity.getCode().equals("ORDER_CONTRACT")) {
                return deptTransactionSubTypeEntity;
            }
        }
        return null;
    }

    public DeptTransactionSubTypeEntity getHanMuc(Integer dept_transaction_sub_type_id) {
        return this.mpHangMucCongViec.get(dept_transaction_sub_type_id);
    }

    public int getNumberDateToNoXau() {
        return ConvertUtils.toInt(mpConfig.get("NO_XAU_NUMBER_DATE"));
    }

    public int getHotCommon() {
        return ConvertUtils.toInt(this.mpConfig.get("HOT_COMMON"));
    }

    public int getHotAgency() {
        return ConvertUtils.toInt(this.mpConfig.get("HOT_AGENCY"));
    }

    public boolean updateHotConfig(int number_common, int number_agency) {
        if (this.getHotCommon() != number_common) {
            this.mpConfig.put("HOT_COMMON", ConvertUtils.toString(number_common));
            boolean rsHotCommon = this.masterDB.update(
                    "UPDATE config SET data = '" + number_common + "'" +
                            " WHERE code = 'HOT_COMMON'"
            );
            if (!rsHotCommon) {

            }
        }

        if (this.getHotAgency() != number_agency) {
            this.mpConfig.put("HOT_AGENCY", ConvertUtils.toString(number_agency));
            boolean rsHotAgency = this.masterDB.update(
                    "UPDATE config SET data = '" + number_agency + "'" +
                            " WHERE code = 'HOT_AGENCY'"
            );
            if (!rsHotAgency) {

            }
        }
        return true;
    }

    public Integer getCommitLimit() {
        return ConvertUtils.toInt(mpConfig.get("COMMIT_LIMIT"));
    }

    public int getNumberDateScheduleDelivery() {
        return ConvertUtils.toInt(mpConfig.get("NUMBER_DATE_SCHEDULE_DELIVERY"));
    }

    public NotifyData getNotifyDataByContentType(
            NotifyAutoContentType notifyAutoContentType,
            String setting_data) {
        try {
            NotifyData notifyData = new NotifyData();
            if (notifyAutoContentType == null) {
                notifyData.setTitle("Thông báo");
                notifyData.setDescription(
                        setting_data
                );
                return notifyData;
            }
            switch (notifyAutoContentType) {
                case CANCEL_ORDER_WAITING_CONFIRM:
                    notifyData.setTitle("Thông báo");
                    notifyData.setDescription(
                            notifyAutoContentType.getLabel().replace(
                                    "<Mã ĐH>", setting_data
                            )
                    );
                    break;
                default:
                    notifyData.setTitle("Thông báo");
                    notifyData.setDescription(
                            setting_data
                    );
                    break;
            }

            return notifyData;
        } catch (Exception ex) {

        }

        return null;
    }

    public boolean isActiveSaveRequestLog() {
        return ConvertUtils.toInt(mpConfig.get("CMS_ACTIVE_REQUEST_LOG")) == 1;
    }

    public int getPushNotifyNQH() {
        return ConvertUtils.toInt(mpConfig.get(ConfigType.PUSH_NOTIFY_NQH.getCode()));
    }

    public int getTimeLock() {
        return 30 * 60 * 1000;
    }

    public Map<Integer, JSONObject> getMpProductHotType() {
        return mpProductHotType;
    }

    public JSONObject getProductHotTypeByCode(String code) {
        for (JSONObject type : mpProductHotType.values()) {
            if (code.equals(ConvertUtils.toString(type.get("code")))) {
                return type;
            }
        }
        return null;
    }

    public boolean isHanMucHenGiao(int dept_transaction_sub_type_id) {
        if (dept_transaction_sub_type_id == DeptConstants.HAN_MUC_HEN_GIAO) {
            return true;
        }
        return false;
    }

    public boolean isHanMucHopDong(int dept_transaction_sub_type_id) {
        if (dept_transaction_sub_type_id == DeptConstants.HAN_MUC_HOP_DONG) {
            return true;
        }
        return false;
    }

    public boolean updateDayNumberPushNotifyLock(int day) {
        this.mpConfig.put(ConfigType.PUSH_NOTIFY_LOCK.getCode(), ConvertUtils.toString(day));
        return this.masterDB.update(
                "UPDATE config SET data = '" + day + "'" +
                        " WHERE code = '" + ConfigType.PUSH_NOTIFY_LOCK.getCode() + "'"
        );
    }

    public int getDayNumberPushNotifyLock() {
        return ConvertUtils.toInt(mpConfig.get(ConfigType.PUSH_NOTIFY_LOCK.getCode()));
    }

    public boolean updateNCatalog(int n_register, int n_add) {
        this.mpConfig.put(ConfigType.CATALOG_N_REGISTER.getCode(), ConvertUtils.toString(n_register));
        this.mpConfig.put(ConfigType.CATALOG_N_ADD.getCode(), ConvertUtils.toString(n_add));
        this.masterDB.update(
                "UPDATE config SET data = '" + n_register + "'" +
                        " WHERE code = '" + ConfigType.CATALOG_N_REGISTER.getCode() + "'"
        );
        this.masterDB.update(
                "UPDATE config SET data = '" + n_add + "'" +
                        " WHERE code = '" + ConfigType.CATALOG_N_ADD.getCode() + "'"
        );
        return true;
    }

    public int getCatalogNRegister() {
        return ConvertUtils.toInt(mpConfig.get(ConfigType.CATALOG_N_REGISTER.getCode()));
    }

    public int getCatalogNAdd() {
        return ConvertUtils.toInt(mpConfig.get(ConfigType.CATALOG_N_ADD.getCode()));
    }

    public boolean isHBTL(Integer deptTransactionSubTypeId) {
        if (deptTransactionSubTypeId == DeptConstants.GHI_NHAN_TRA_HANG) {
            return true;
        }
        return false;
    }

    public boolean updateConfigData(String type, String value) {
        this.mpConfig.put(type, value);
        return this.masterDB.update(
                "UPDATE config SET data = '" + value + "'" +
                        " WHERE code = '" + type + "'"
        );
    }

    public Map<String, String> getMPConfigData() {
        return this.mpConfig;
    }

    public Map<String, String> getMPMissionConfig() {
        return this.mpMissionConfig;
    }

    public TimeToWorkData getGioHanhChinhByDayOfWeek(int dayOfWeek) {
        try {
            return this.mpTimeToWork.get(dayOfWeek);
        } catch (Exception e
        ) {
        }
        return null;
    }

    public int getKyDai() {
        List<Integer> mission_period_running_list = JsonUtils.DeSerialize(
                this.getMPMissionConfig().get(MissionConstants.KY_NHIEM_VU),
                new TypeToken<List<Integer>>() {
                }.getType());

        return Collections.max(mission_period_running_list);
    }

    public int getKyNgan() {
        List<Integer> mission_period_running_list = JsonUtils.DeSerialize(
                this.getMPMissionConfig().get(MissionConstants.KY_NHIEM_VU),
                new TypeToken<List<Integer>>() {
                }.getType());

        return Collections.min(mission_period_running_list);
    }

    public List<Integer> getListMissionPeriodRunning() {
        return JsonUtils.DeSerialize(
                this.getMPMissionConfig().get(MissionConstants.KY_NHIEM_VU),
                new TypeToken<List<Integer>>() {
                }.getType());
    }

    public JSONObject getMissionPeriodRunningTime(int mission_period_id) {
        return this.masterDB.getOne("SELECT * FROM mission_period_running WHERE mission_period_id = " + mission_period_id);
    }

    public Map<Integer, JSONObject> getMPAgencyStatus() {
        return this.mpAgencyStatus;
    }

    public Map<Integer, JSONObject> getMPOrderStatus() {
        return this.mpOrderStatus;
    }

    public Map<Integer, JSONObject> getMPPromoStatus() {
        return this.mpPromoStatus;
    }

    public int getProductQuantityStart() {
        return ConvertUtils.toInt(mpConfig.get(ConfigConstants.PRODUCT_QUANTITY_START));
    }
}