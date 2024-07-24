package com.app.server.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.app.server.config.ConfigInfo;
import com.app.server.data.dto.agency.Agency;
import com.app.server.data.dto.agency.AgencyAccount;
import com.app.server.data.entity.*;
import com.app.server.data.request.agency.*;
import com.app.server.database.sql.AgencySQL;
import com.app.server.enums.*;
import com.app.server.utils.JsonUtils;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.dbconn.ClientManager;
import com.ygame.framework.dbconn.ManagerIF;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AgencyDB extends BaseDB {
    private AgencySQL agencySQL;

    @Autowired
    public void setAgencySQL(AgencySQL agencySQL) {
        this.agencySQL = agencySQL;
    }

    private MasterDB masterDB;

    @Autowired
    public void setMasterDB(MasterDB masterDB) {
        this.masterDB = masterDB;
    }


    public int addNewAgency(AddNewAgencyRequest request, String code, String pin_code) {
        String sql = this.agencySQL.addNewAgency(request, code, pin_code);
        return this.masterDB.insert(sql);
    }

    public List<JSONObject> filterAgency(String query, int offset, int size) {
        query += " LIMIT " + offset + "," + size;
        return this.masterDB.find(query);
    }

    public int getTotalAgency(String query) {
        return this.masterDB.getTotal(query);
    }

    public Agency getAgencyById(int id) {
        try {
            String sql = this.agencySQL.getAgencyById(id);
            JSONObject rs = this.masterDB.getOne(sql);
            if (rs != null) {
                return JsonUtils.DeSerialize(JsonUtils.Serialize(rs), Agency.class);
            }
        } catch (Exception ex) {

        }
        return null;
    }

    public JSONObject getAgencyInfoById(int id) {
        String sql = this.agencySQL.getAgencyById(id);
        return this.masterDB.getOne(sql);
    }

    public AgencyEntity getAgencyEntity(int id) {
        String sql = this.agencySQL.getAgencyById(id);
        JSONObject jsonObject = this.masterDB.getOne(sql);
        if (jsonObject != null) {
            return JsonUtils.DeSerialize(JsonUtils.Serialize(jsonObject), AgencyEntity.class);
        }
        return null;
    }

    public AgencyEntity getAgencyEntityByPhone(String phone) {
        JSONObject jsonObject = this.masterDB.getOne(
                "SELECT * FROM agency WHERE phone = '" + phone + "'"
        );
        if (jsonObject != null) {
            return JsonUtils.DeSerialize(JsonUtils.Serialize(jsonObject), AgencyEntity.class);
        }
        return null;
    }

    public boolean updateCode(int id, String code) {
        String sql = this.agencySQL.updateCode(id, code);
        return this.masterDB.update(sql);
    }

    public boolean approveAgency(int id) {
        String sql = this.agencySQL.approveAgency(id, AgencyStatus.APPROVED.getValue());
        return this.masterDB.update(sql);
    }

    public int getCurrentIndexAgencyInCity(int cityId) {
        try {
            String sql = this.agencySQL.getCurrentIndexAgencyInCity(cityId);
            JSONObject rs = this.masterDB.getOne(sql);
            if (rs != null) {
                return ConvertUtils.toInt(rs.get("city_index"));
            }
            return 0;
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return -1;
    }

    public int saveCityIndex(int cityId, int nextIndexCity, int agencyId) {
        String sql = this.agencySQL.saveCityIndex(cityId, nextIndexCity, agencyId);
        return this.masterDB.insert(sql);
    }

    public boolean updateCityIndex(int cityId, int nextIndexCity, int agencyId) {
        return this.masterDB.update(
                "UPDATE agency_code_generate SET" +
                        " city_index = " + nextIndexCity +
                        ", agency_id = " + agencyId +
                        " WHERE city_id = " + cityId);
    }

    public boolean updateAgency(Agency agency) {
        String sql = this.agencySQL.updateAgency(agency);
        return this.masterDB.update(sql);
    }

    public boolean updateStatusAgency(int id, int status) {
        String sql = this.agencySQL.updateStatusAgency(id, status);
        return this.masterDB.update(sql);
    }

    public int addAgencyAccount(AgencyAccount account) {
        String sql = this.agencySQL.addNewAgencyAccount(account);
        return this.masterDB.insert(sql);
    }

    public JSONObject getAgencyAccountByPhone(String phone) {
        String sql = this.agencySQL.getAgencyAccountByPhone(phone);
        return this.masterDB.getOne(sql);
    }

    public List<JSONObject> getListAgencyAccount(int agency_id) {
        String sql = this.agencySQL.getListAgencyAccount(agency_id);
        return this.masterDB.find(sql);
    }

    public List<JSONObject> getListAddressDelivery(int agency_id) {
        String sql = this.agencySQL.getListAddressDelivery(agency_id);
        return this.masterDB.find(sql);
    }

    public List<JSONObject> getListAddressExportBilling(int agency_id) {
        String sql = this.agencySQL.getListAddressExportBilling(agency_id);
        return this.masterDB.find(sql);
    }

    public AgencyAddressDeliveryEntity getAddressDeliveryDetail(int id) {
        String sql = this.agencySQL.getAddressDeliveryDetail(id);
        JSONObject jsonObject = this.masterDB.getOne(sql);
        if (jsonObject != null) {
            return JsonUtils.DeSerialize(JsonUtils.Serialize(jsonObject), AgencyAddressDeliveryEntity.class);
        }
        return null;
    }

    public AddressExportBillingEntity getAddressExportBillingDetail(int id) {
        String sql = this.agencySQL.getAddressExportBillingDetail(id);
        JSONObject jsonObject = this.masterDB.getOne(sql);
        if (jsonObject != null) {
            return JsonUtils.DeSerialize(JsonUtils.Serialize(jsonObject), AddressExportBillingEntity.class);
        }
        return null;
    }

    public JSONObject getAgencyAccountDetail(int id) {
        String sql = this.agencySQL.getAgencyAccountDetail(id);
        return this.masterDB.getOne(sql);
    }

    public boolean editAgencyAccount(AgencyAccount account) {
        String sql = this.agencySQL.editAgencyAccount(account);
        return this.masterDB.update(sql);
    }

    public boolean updateStatusAddressDelivery(int id, int status) {
        String sql = this.agencySQL.updateStatusAddressDelivery(id, status);
        return this.masterDB.update(sql);
    }

    public int addAddressDelivery(AddAddressDeliveryRequest request) {
        String sql = this.agencySQL.addAddressDelivery(request);
        return this.masterDB.insert(sql);
    }

    public boolean editAddressDelivery(EditAddressDeliveryRequest request) {
        String sql = this.agencySQL.editAddressDelivery(request);
        return this.masterDB.update(sql);
    }

    public int addAddressExportBilling(AddAddressExportBillingRequest request) {
        String sql = this.agencySQL.addAddressExportBilling(request);
        return this.masterDB.insert(sql);
    }

    public boolean editAddressExportBilling(EditAddressExportBillingRequest request) {
        String sql = this.agencySQL.editAddressExportBilling(request);
        return this.masterDB.update(sql);
    }

    public boolean updateStatusAddressExportDelivery(int id, int status) {
        String sql = this.agencySQL.updateStatusAddressExportDelivery(id, status);
        return this.masterDB.update(sql);
    }

    public boolean setAgencyAccountMain(int id) {
        String sql = this.agencySQL.setAgencyAccountMain(id);
        return this.masterDB.update(sql);
    }

    public JSONObject getAgencyAccountMain(int agency_id) {
        String sql = this.agencySQL.getAgencyAccountMain(agency_id);
        return this.masterDB.getOne(sql);
    }

    public boolean setAgencyAccountSub(int id) {
        String sql = this.agencySQL.setAgencyAccountSub(id);
        return this.masterDB.update(sql);
    }

    public boolean updateStatusAgencyAccount(int id, int status) {
        String sql = this.agencySQL.updateStatusAgencyAccount(id, status);
        return this.masterDB.update(sql);
    }

    public AddressExportBillingEntity getAgencyAddressExportDefault(int agency_id) {
        String sql = this.agencySQL.getAgencyAddressExportDefault(agency_id);
        JSONObject jsonObject = this.masterDB.getOne(sql);
        if (jsonObject != null) {
            return JsonUtils.DeSerialize(JsonUtils.Serialize(jsonObject), AddressExportBillingEntity.class);
        }
        return null;
    }

    public boolean setAgencyAddressExportDefault(int id) {
        String sql = this.agencySQL.setAgencyAddressExportDefault(id);
        return this.masterDB.update(sql);
    }

    public boolean setAgencyAddressExportNotDefault(int id) {
        String sql = this.agencySQL.setAgencyAddressExportNotDefault(id);
        return this.masterDB.update(sql);
    }

    public AgencyAddressDeliveryEntity getAgencyDeliveryDefault(int agency_id) {
        String sql = this.agencySQL.getAgencyAddressDeliveryDefault(agency_id);
        JSONObject jsonObject = this.masterDB.getOne(sql);
        if (jsonObject != null) {
            return JsonUtils.DeSerialize(JsonUtils.Serialize(jsonObject), AgencyAddressDeliveryEntity.class);
        }
        return null;
    }

    public boolean setAgencyAddressDeliveryNotDefault(int id) {
        String sql = this.agencySQL.setAgencyAddressDeliveryNotDefault(id);
        return this.masterDB.update(sql);
    }

    public boolean setAgencyAddressDeliveryDefault(int id) {
        String sql = this.agencySQL.setAgencyAddressDeliveryDefault(id);
        return this.masterDB.update(sql);
    }

    public AgencyContractInfoEntity getAgencyContractInfoByAgencyId(int agency_id) {
        String sql = this.agencySQL.getAgencyContractInfoByAgencyId(agency_id);
        try {
            JSONObject rs = this.masterDB.getOne(sql);
            if (rs != null) {
                return JsonUtils.DeSerialize(JsonUtils.Serialize(rs), AgencyContractInfoEntity.class);
            }
        } catch (Exception ex) {

        }
        return null;
    }

    public int createAgencyContractInfo(UpdateAgencyContractInfoRequest request) {
        String sql = this.agencySQL.createAgencyContractInfo(request);
        return this.masterDB.insert(sql);
    }

    public boolean updateAgencyContractInfo(UpdateAgencyContractInfoRequest request) {
        String sql = this.agencySQL.updateAgencyContractInfo(request);
        return this.masterDB.update(sql);
    }

    public Integer getBusinessDepartmentIdByRegionId(int regionId) {
        String sql = this.agencySQL.getBusinessDepartmentIdByRegionId(regionId);
        JSONObject js = this.masterDB.getOne(sql);

        if (js != null) {
            return ConvertUtils.toInt(js.get("business_department_id"));
        }

        return null;
    }

    public boolean setBusinessDepartment(int id, int businessDepartmentId) {
        String sql = this.agencySQL.setBusinessDepartment(id, businessDepartmentId);
        return this.masterDB.update(sql);
    }

    public List<JSONObject> searchAgency(String query, int offset, int pageSize, int isLimit) {
        if (isLimit == 1) {
            query += " LIMIT " + offset + "," + pageSize;
        }
        return this.masterDB.find(query);
    }

    public List<JSONObject> searchAgencyAddressDelivery(String query, int offset, int pageSize, int isLimit) {
        if (isLimit == 1) {
            query += " LIMIT " + offset + "," + pageSize;
        }
        return this.masterDB.find(query);
    }

    public List<JSONObject> searchAgencyAddressExportBilling(String query, int offset, int pageSize, int isLimit) {
        if (isLimit == 1) {
            query += " LIMIT " + offset + "," + pageSize;
        }
        return this.masterDB.find(query);
    }

    public void loadAllAgency(Map<Integer, AgencyEntity> mpAgency) {
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String sql = "SELECT * FROM agency";
            try (Statement stmt = con.createStatement()) {
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        AgencyEntity agencyEntity = JsonUtils.DeSerialize(JsonUtils.Serialize(JsonUtils.convertToJSON(rs)), AgencyEntity.class);
                        mpAgency.put(agencyEntity.getId(), agencyEntity);
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

    public AgencyEntity loadOneAgency(int id, Map<Integer, AgencyEntity> mpAgency) {
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String sql = "SELECT * FROM agency WHERE id =" + id;
            try (Statement stmt = con.createStatement()) {
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    if (rs.next()) {
                        AgencyEntity agencyEntity = JsonUtils.DeSerialize(JsonUtils.Serialize(JsonUtils.convertToJSON(rs)), AgencyEntity.class);
                        mpAgency.put(agencyEntity.getId(), agencyEntity);
                        return agencyEntity;
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
        return null;
    }

    public List<JSONObject> getAllAgencyActive() {
        return this.masterDB.find(
                "SELECT id,code,shop_name,membership_id,business_department_id" +
                        " FROM agency WHERE status != " + AgencyStatus.WAITING_APPROVE.getValue()
                        + " ORDER BY id ASC"
        );
    }

    public List<JSONObject> filter(String query, int offset, int pageSize, int isLimit) {
        if (isLimit == 1) {
            query += " LIMIT " + offset + "," + pageSize;
        }
        return this.masterDB.find(query);
    }

    public int getTotal(String query) {
        return this.masterDB.getTotal(query);
    }

    public int insertAgencyAcoinHistory(AgencyAcoinHistoryEntity entity) {
        return this.masterDB.insert(
                "INSERT INTO agency_acoin_history(" +
                        "agency_id," +
                        "created_date," +
                        "point," +
                        "old_point," +
                        "current_point," +
                        "note" +
                        ")" +
                        " VALUES(" +
                        "'" + entity.getAgency_id() + "'," +
                        "'" + DateTimeUtils.toString(entity.getCreated_date()) + "'," +
                        "'" + entity.getPoint() + "'," +
                        "'" + entity.getOld_point() + "'," +
                        "'" + entity.getCurrent_point() + "'," +
                        "'" + entity.getNote() + "'" +
                        ");"
        );
    }

    public List<JSONObject> getAllAgency() {
        return this.masterDB.find(
                "SELECT * FROM agency"
        );
    }

    public boolean updateAgencyMembership(
            int agency_id,
            int new_membership_id,
            String new_agency_code,
            int modifier_id) {
        return this.masterDB.update(
                "UPDATE agency SET " +
                        " membership_id = " + new_membership_id +
                        ", code = '" + new_agency_code + "'" +
                        " WHERE id = " + agency_id
        );
    }

    public int saveAgencyMembershipHistory(
            int agency_id,
            int new_membership_id,
            int old_membership,
            String new_agency_code,
            String old_agency_code,
            int creator_id) {
        return this.masterDB.insert(
                "INSERT INTO agency_membership_history(" +
                        "agency_id," +
                        "membership_id," +
                        "old_membership_id," +
                        "agency_code," +
                        "old_agency_code," +
                        "creator_id" +
                        ")" +
                        " VALUES(" +
                        "'" + agency_id + "'," +
                        "'" + new_membership_id + "'," +
                        "'" + old_membership + "'," +
                        "'" + new_agency_code + "'," +
                        "'" + old_agency_code + "'," +
                        "'" + creator_id + "'" +
                        ");"
        );
    }

    public int saveAgencyMembershipHistoryBySource(
            int agency_id,
            int new_membership_id,
            int old_membership,
            String new_agency_code,
            String old_agency_code,
            int creator_id,
            String source) {
        return this.masterDB.insert(
                "INSERT INTO agency_membership_history(" +
                        "agency_id," +
                        "membership_id," +
                        "old_membership_id," +
                        "agency_code," +
                        "old_agency_code," +
                        "creator_id," +
                        "source" +
                        ")" +
                        " VALUES(" +
                        "'" + agency_id + "'," +
                        "'" + new_membership_id + "'," +
                        "'" + old_membership + "'," +
                        "'" + new_agency_code + "'," +
                        "'" + old_agency_code + "'," +
                        "'" + creator_id + "'," +
                        "'" + source + "'" +
                        ");"
        );
    }

    public List<JSONObject> getListFirebaseAgencyAccount(
            String agency_ids,
            String city_ids,
            String region_ids,
            String membership_ids) {
        return this.masterDB.find(
                "SELECT t.firebase_token,t.agency_id FROM agency_account t" +
                        " LEFT JOIN agency t1 ON t1.id = t.agency_id" +
                        " WHERE t.firebase_token IS NOT NULL" +
                        " AND t.firebase_token != ''" +
                        " AND (" +
                        " ('agency_ids' != '[]' AND " + "'" + agency_ids + "' LIKE CONCAT('%\"',t.agency_id,'\"%'))" +
                        " OR " +
                        " (" +
                        " ('" + city_ids + "' != '[]' AND '" + city_ids + "' LIKE CONCAT('%\"',t1.city_id,'\"%'))" +
                        " AND ('" + region_ids + "' != '[]' AND '" + region_ids + "' LIKE CONCAT('%\"',t1.region_id,'\"%'))" +
                        " AND ('" + membership_ids + "' != '[]' AND '" + membership_ids + "' LIKE CONCAT('%\"',t1.membership_id,'\"%'))" +
                        " )" +
                        " ) GROUP BY t.firebase_token,t.agency_id"
        );
    }

    public List<JSONObject> getListAgency(
            String agency_ids,
            String city_ids,
            String region_ids,
            String membership_ids) {
        return this.masterDB.find(
                "SELECT t1.id,t1.status FROM agency t1" +
                        " WHERE (" +
                        " ('agency_ids' != '[]' AND " + "'" + agency_ids + "' LIKE CONCAT('%\"',t1.id,'\"%'))" +
                        " OR " +
                        " (" +
                        " ('" + city_ids + "' != '[]' AND '" + city_ids + "' LIKE CONCAT('%\"',t1.city_id,'\"%'))" +
                        " AND ('" + region_ids + "' != '[]' AND '" + region_ids + "' LIKE CONCAT('%\"',t1.region_id,'\"%'))" +
                        " AND ('" + membership_ids + "' != '[]' AND '" + membership_ids + "' LIKE CONCAT('%\"',t1.membership_id,'\"%'))" +
                        " )" +
                        " )"
        );
    }

    public boolean blockCSBH(int agency_id, int block) {
        return this.masterDB.update(
                "UPDATE agency SET block_csbh = " + block +
                        " WHERE id = " + agency_id
        );
    }

    public boolean blockCTKM(int agency_id, int block) {
        return this.masterDB.update(
                "UPDATE agency SET block_ctkm = " + block +
                        " WHERE id = " + agency_id
        );
    }

    public boolean blockCTSN(int agency_id, int block) {
        return this.masterDB.update(
                "UPDATE agency SET block_ctsn = " + block +
                        " WHERE id = " + agency_id
        );
    }

    public boolean blockPrice(int agency_id, int block) {
        return this.masterDB.update(
                "UPDATE agency SET block_price = " + block +
                        " WHERE id = " + agency_id
        );
    }

    public int getTotalRank() {
        return this.getTotal(
                "SELECT * FROM agency WHERE rank_value > 0"
        );
    }

    public boolean syncAgencyInfoSuccess(int agency_id) {
        return this.masterDB.update(
                "UPDATE agency SET sync_status = " + SyncStatus.SUCCESS.getValue() +
                        " WHERE id = " + agency_id
        );
    }

    public boolean syncAgencyInfoFail(int agency_id, String note, int sync_type) {
        return this.masterDB.update(
                "UPDATE agency SET sync_status = " + SyncStatus.FAIL.getValue() + "," +
                        " sync_note = '" + "Đồng bộ thất bại: " + note + "'," +
                        " sync_type = " + sync_type +
                        " WHERE id = " + agency_id
        );
    }

    public JSONObject getAgencyInfo(int id) {
        return this.masterDB.getOne(
                "SELECT * FROM agency WHERE id = " + id
        );
    }

    public JSONObject getDeliveryInfo(int id) {
        return this.masterDB.getOne(
                "SELECT * FROM agency_address_delivery WHERE id = " + id
        );
    }

    public JSONObject getBillingInfo(int id) {
        return this.masterDB.getOne(
                "SELECT * FROM agency_address_export_billing WHERE id = " + id
        );
    }

    public boolean blockCTTL(int agency_id, int block) {
        return this.masterDB.update(
                "UPDATE agency SET block_cttl = " + block +
                        " WHERE id = " + agency_id
        );
    }

    public List<JSONObject> filterAgency(
            String agency_include_ids,
            String agency_ignore_ids,
            String city_ids,
            String region_ids,
            String membership_ids,
            int has_filter) {
        return this.masterDB.find(
                "SELECT t1.id," +
                        " t1.shop_name," +
                        " t1.avatar," +
                        " t1.phone," +
                        " t1.code," +
                        " t1.membership_id," +
                        " t1.business_department_id" +
                        " FROM agency t1" +
                        " WHERE " +
                        " ('" + agency_ignore_ids + "' = '[]' OR " + "'" + agency_ignore_ids + "' NOT LIKE CONCAT('%\"',t1.id,'\"%'))" +
                        " AND " +
                        " (" +
                        " ('" + agency_include_ids + "' != '[]' AND " + "'" + agency_include_ids + "' LIKE CONCAT('%\"',t1.id,'\"%'))" +
                        " OR (" + has_filter + " = 1" +
                        " AND ('" + city_ids + "' = '[]' OR '" + city_ids + "' LIKE CONCAT('%\"',t1.city_id,'\"%'))" +
                        " AND ('" + region_ids + "' = '[]' OR '" + region_ids + "' LIKE CONCAT('%\"',t1.region_id,'\"%'))" +
                        " AND ('" + membership_ids + "' = '[]' OR '" + membership_ids + "' LIKE CONCAT('%\"',t1.membership_id,'\"%'))" +
                        " )" +
                        ")"
        );
    }

    public int insertDeptCommitSetting(
            int agency_id,
            int number_day_nqh_miss_commit,
            int creator_id) {
        return this.masterDB.insert(
                "INSERT INTO dept_commit_setting (" +
                        "agency_id," +
                        "number_day_nqh_miss_commit," +
                        "creator_id" +
                        ") " +
                        " VALUES(" +
                        agency_id + "," +
                        number_day_nqh_miss_commit + "," +
                        creator_id +
                        ")"
        );
    }

    public JSONObject getSettingLockAgency(int id) {
        return this.masterDB.getOne(
                "SELECT * FROM agency_lock_setting WHERE id = " + id
        );
    }

    public boolean cancelSettingLockAgency(int id, int confirmer_id) {
        return this.masterDB.update(
                "UPDATE agency_lock_setting SET" +
                        " status = " + ProductPriceTimerStatus.CANCEL.getId() + "," +
                        " confirmer_id = " + confirmer_id +
                        " WHERE id = " + id
        );
    }

    public int insertAgencyLockSetting(AgencyLockSettingEntity entity) {
        return this.masterDB.insert(
                "INSERT INTO agency_lock_setting(" +
                        "setting_object_type," +
                        "setting_object_data," +
                        "option_lock," +
                        "day_lock," +
                        "status," +
                        "start_date," +
                        "creator_id" +
                        ") VALUES(" +
                        "'" + entity.getSetting_object_type() + "'," +
                        "'" + entity.getSetting_object_data() + "'," +
                        entity.getOption_lock() + "," +
                        entity.getDay_lock() + "," +
                        entity.getStatus() + "," +
                        parseDateToSql(entity.getStart_date()) + "," +
                        parseIntegerToSql(entity.getCreator_id()) +
                        ")"
        );
    }

    public int saveAgencyMissCommitHistory(
            int agency_id,
            int commit_limit,
            int before_value,
            int after_value,
            int link_id,
            String link_code,
            int type,
            int number_day_nqh_miss_commit) {
        return this.masterDB.insert(
                "INSERT INTO agency_miss_commit_history (" +
                        "agency_id," +
                        "commit_limit," +
                        "before_value," +
                        "after_value," +
                        "link_id," +
                        "link_code," +
                        "type," +
                        "number_day_nqh_miss_commit" +
                        ") " +
                        " VALUES(" +
                        agency_id + "," +
                        commit_limit + "," +
                        before_value + "," +
                        after_value + "," +
                        link_id + "," +
                        "'" + link_code + "'" + "," +
                        type + "," +
                        number_day_nqh_miss_commit +
                        ")"
        );
    }

    public boolean updateDeptCommitSetting(int agency_id, int number_day_nqh_miss_commit) {
        return this.masterDB.update(
                "UPDATE agency " +
                        " SET number_day_nqh_miss_commit = " + number_day_nqh_miss_commit +
                        " WHERE id = " + agency_id
        );
    }

    public boolean approveAgencyLockSetting(int id, Date start_date, int status, int confirmer_id) {
        return this.masterDB.update(
                "UPDATE agency_lock_setting" +
                        " SET start_date = " + parseDateToSql(start_date) + "," +
                        " status = " + status + "," +
                        " confirmer_id = " + confirmer_id +
                        " WHERE id = " + id
        );
    }

    public int insertAgencyLockData(
            String setting_object_type,
            int setting_object_data,
            int option_lock,
            int day_lock,
            String agency_lock_setting_code,
            int status
    ) {
        return this.masterDB.insert(
                "INSERT INTO agency_lock_data(" +
                        "setting_object_type," +
                        "setting_object_data," +
                        "option_lock," +
                        "day_lock," +
                        "agency_lock_setting_code," +
                        "modified_date," +
                        "effect_date," +
                        "status" +
                        ") VALUES(" +
                        "'" + setting_object_type + "'," +
                        "'" + setting_object_data + "'," +
                        option_lock + "," +
                        day_lock + "," +
                        "'" + agency_lock_setting_code + "'" + "," +
                        "NOW()" + "," +
                        "NOW()" + "," +
                        status +
                        ")"
        );
    }

    public boolean updateAgencyLockData(
            int id,
            String setting_object_type,
            int setting_object_data,
            int option_lock,
            int day_lock,
            String agency_lock_setting_code,
            int status
    ) {
        return this.masterDB.update(
                "UPDATE agency_lock_data SET " +
                        "setting_object_type = " + parseStringToSql(setting_object_type) + "," +
                        "setting_object_data = " + parseIntegerToSql(setting_object_data) + "," +
                        "option_lock = " + parseIntegerToSql(option_lock) + "," +
                        "day_lock = " + parseIntegerToSql(day_lock) + "," +
                        "agency_lock_setting_code = " + parseStringToSql(agency_lock_setting_code) + "," +
                        "modified_date = NOW()," +
                        "effect_date = NOW()," +
                        "status = " + status +
                        " WHERE id = " + id
        );
    }

    public JSONObject getAgencyLockData(String setting_object_type, Integer setting_object_data) {
        return this.masterDB.getOne(
                "SELECT * FROM agency_lock_data" +
                        " WHERE setting_object_type = " + parseStringToSql(setting_object_type) +
                        " AND setting_object_data = " + setting_object_data
        );
    }

    public boolean updateCodeForAgencyLockSetting(int id, String code) {
        return this.masterDB.update(
                "UPDATE agency_lock_setting" +
                        " SET code = " + parseStringToSql(code) +
                        " WHERE id = " + id
        );
    }

    public boolean saveLockCheckDate(
            int agency_id,
            Date date) {
        return this.masterDB.update(
                "UPDATE agency" +
                        " SET lock_check_date = " + parseDateToSql(date) +
                        " WHERE id = " + agency_id
        );
    }

    public boolean saveNgayGhiNhanCongNo(
            int agency_id) {
        return this.masterDB.update(
                "UPDATE agency" +
                        " SET lock_check_date = NOW()," +
                        " dept_order_date = NOW()" +
                        " WHERE id = " + agency_id
        );
    }

    public boolean initNgayGhiNhanCongNo(
            int agency_id,
            Date date) {
        return this.masterDB.update(
                "UPDATE agency" +
                        " SET dept_order_date = " + parseDateToSql(date) +
                        " WHERE id = " + agency_id
        );
    }

    public int saveAgencyLockHistory(int agency_id,
                                     int staff_id,
                                     String note,
                                     int type) {
        return this.masterDB.insert(
                "INSERT INTO agency_lock_history(" +
                        "agency_id," +
                        "creator_id," +
                        "note," +
                        "type" +
                        ") VALUES(" +
                        "'" + agency_id + "'," +
                        "'" + staff_id + "'," +
                        "'" + note + "'," +
                        type +
                        ")"
        );
    }

    public List<JSONObject> getListAgencyActive() {
        return this.masterDB.find(
                "SELECT id,code,shop_name,membership_id,business_department_id," +
                        " city_id,region_id,dept_order_date,status,lock_check_date" +
                        " FROM agency WHERE status != " + AgencyStatus.WAITING_APPROVE.getValue()
                        + " ORDER BY id ASC"
        );
    }

    public List<JSONObject> getListAgencyInAgency(
            String data) {
        return this.masterDB.find(
                "SELECT t1.* FROM agency t1" +
                        " WHERE '" + data + "' LIKE CONCAT('%\"',t1.id,'\"%')"
        );
    }

    public List<JSONObject> getListAgencyInCity(
            String data) {
        return this.masterDB.find(
                "SELECT t1.* FROM agency t1" +
                        " WHERE '" + data + "' LIKE CONCAT('%\"',t1.city_id,'\"%')"
        );
    }

    public List<JSONObject> getListAgencyInRegion(
            String data) {
        return this.masterDB.find(
                "SELECT t1.* FROM agency t1" +
                        " WHERE '" + data + "' LIKE CONCAT('%\"',t1.region_id,'\"%')"
        );
    }

    public List<JSONObject> getListAgencyInMembership(
            String data) {
        return this.masterDB.find(
                "SELECT t1.* FROM agency t1" +
                        " WHERE '" + data + "' LIKE CONCAT('%\"',t1.membership_id,'\"%')"
        );
    }

    public List<JSONObject> getAgencyLockSettingWaiting() {
        return this.masterDB.find(
                "SELECT * FROM agency_lock_setting" +
                        " WHERE status = " + LockSettingStatus.WAITING.getId() +
                        " AND start_date <= NOW() " +
                        " ORDER BY id asc"
        );
    }

    public boolean activeAgencyLockSetting(int id) {
        return this.masterDB.update(
                "UPDATE agency_lock_setting" +
                        " SET status = " + LockSettingStatus.RUNNING.getId() +
                        " WHERE id = " + id
        );
    }

    public boolean pendingAgencyLockData(
            String setting_object_type,
            String agency_lock_setting_code,
            int status) {
        return this.masterDB.update(
                "UPDATE agency_lock_data" +
                        " SET status = " + status +
                        " WHERE setting_object_type = '" + setting_object_type + "'" +
                        " AND agency_lock_setting_code = '" + agency_lock_setting_code + "'"
        );
    }

    public JSONObject getAgencyLockDataById(int id) {
        return this.masterDB.getOne(
                "SELECT * FROM agency_lock_data" +
                        " WHERE id = " + id
        );
    }

    public boolean stopAgencyLockData(int id) {
        return this.masterDB.update(
                "UPDATE agency_lock_data SET" +
                        " status = " + LockDataStatus.PENDING.getId() +
                        " WHERE id = " + id
        );
    }

    public boolean resetLockCheckDate(int id) {
        return this.masterDB.update(
                "UPDATE agency" +
                        " SET lock_check_date = NOW()" +
                        " WHERE id = " + id
        );
    }

    public boolean saveLockedDate(int id) {
        return this.masterDB.update(
                "UPDATE agency" +
                        " SET locked_date = NOW()" +
                        " WHERE id = " + id
        );
    }

    public boolean lockAgency(int id, int status) {
        return this.masterDB.update(
                "UPDATE agency" +
                        " SET status = " + status + "," +
                        " locked_date = NOW()" +
                        " WHERE id = " + id
        );
    }

    public boolean blockCTSS(int agency_id, int block) {
        return this.masterDB.update(
                "UPDATE agency SET block_ctss = " + block +
                        " WHERE id = " + agency_id
        );
    }

    public boolean blockCSDM(int agency_id, int block) {
        return this.masterDB.update(
                "UPDATE agency SET block_csdm = " + block +
                        " WHERE id = " + agency_id
        );
    }

    public boolean blockAll(int agency_id) {
        return this.masterDB.update(
                "UPDATE agency" +
                        " SET block_csbh = " + 1 + "," +
                        "  block_ctkm = " + 1 + "," +
                        "  block_ctsn = " + 1 + "," +
                        "  block_cttl = " + 1 + "," +
                        "  block_ctss = " + 1 + "," +
                        "  block_csdm = " + 1 +
                        " WHERE id = " + agency_id
        );
    }

    public List<JSONObject> filterAgencyByApplyObject(
            String agency_include_ids,
            String agency_ignore_ids,
            String city_ids,
            String region_ids,
            String membership_ids,
            String business_department_ids,
            int has_filter) {
        return this.masterDB.find(
                "SELECT t1.id," +
                        " t1.shop_name," +
                        " t1.avatar," +
                        " t1.phone," +
                        " t1.code," +
                        " t1.membership_id," +
                        " t1.business_department_id" +
                        " FROM agency t1" +
                        " WHERE " +
                        " ('" + agency_ignore_ids + "' = '[]' OR " + "'" + agency_ignore_ids + "' NOT LIKE CONCAT('%\"',t1.id,'\"%'))" +
                        " AND " +
                        " (" +
                        " ('" + agency_include_ids + "' != '[]' AND " + "'" + agency_include_ids + "' LIKE CONCAT('%\"',t1.id,'\"%'))" +

                        " ('" + agency_include_ids + "' != '[]' AND " + "'" + agency_include_ids + "' LIKE CONCAT('%\"',t1.id,'\"%'))" +
                        " OR (" + has_filter + " = 0)" +
                        " OR (" + has_filter + " = 1" +
                        " AND ('" + city_ids + "' = '[]' OR '" + city_ids + "' LIKE CONCAT('%\"',t1.city_id,'\"%'))" +
                        " AND ('" + region_ids + "' = '[]' OR '" + region_ids + "' LIKE CONCAT('%\"',t1.region_id,'\"%'))" +
                        " AND ('" + membership_ids + "' = '[]' OR '" + membership_ids + "' LIKE CONCAT('%\"',t1.membership_id,'\"%'))" +
                        " AND ('" + business_department_ids + "' = '[]' OR '" + business_department_ids + "' LIKE CONCAT('%\"',t1.business_department_id,'\"%'))" +
                        " )" +
                        ")");
    }

    public List<JSONObject> getCatalogWaitingApprove(int agency_id) {
        return this.masterDB.find(
                "SELECT t.*" +
                        " FROM agency_catalog_detail t" +
                        " WHERE t.agency_id = " + agency_id +
                        " AND t.status = " + AgencyCatalogDetailStatus.WAITING.getId()
        );
    }

    public boolean requireCatalog(int id, int require_catalog) {
        return this.masterDB.update(
                "UPDATE agency SET require_catalog = " + require_catalog +
                        " WHERE id = " + id);
    }

    public int saveAgencyDTTYear(int agency_id, long start_value, long end_value, int year) {
        return this.masterDB.insert(
                "INSERT INTO agency_dtt_year(" +
                        "agency_id," +
                        "start_value," +
                        "end_value," +
                        "year)" +
                        " VALUES(" +
                        agency_id + "," +
                        start_value + "," +
                        end_value + "," +
                        year +
                        ")"
        );
    }

    public int saveAgencyTTYear(int agency_id, long start_value, long end_value, int year) {
        return this.masterDB.insert(
                "INSERT INTO agency_tt_year(" +
                        "agency_id," +
                        "start_value," +
                        "end_value," +
                        "year)" +
                        " VALUES(" +
                        agency_id + "," +
                        start_value + "," +
                        end_value + "," +
                        year +
                        ")"
        );
    }

    public int saveAgencyAcoinYear(int agency_id, long start_value, long end_value, int year) {
        return this.masterDB.insert(
                "INSERT INTO agency_acoin_year(" +
                        "agency_id," +
                        "start_value," +
                        "end_value," +
                        "year)" +
                        " VALUES(" +
                        agency_id + "," +
                        start_value + "," +
                        end_value + "," +
                        year +
                        ")"
        );
    }

    public int saveAgencyMembershipYear(int agency_id, long start_value, long end_value, int year, String note,
                                        String reward,
                                        String agency_code) {
        return this.masterDB.insert(
                "INSERT INTO agency_membership_year(" +
                        "agency_id," +
                        "start_value," +
                        "end_value," +
                        "year, " +
                        "note," +
                        "reward," +
                        "agency_code" +
                        ")" +
                        " VALUES(" +
                        agency_id + "," +
                        start_value + "," +
                        end_value + "," +
                        year + "," +
                        "'" + note + "'," +
                        "'" + reward + "'," +
                        "'" + agency_code + "'" +
                        ")"
        );
    }

    public boolean resetMembership(int agency_id, int membership_cycle_start_id, String code) {
        return this.masterDB.update(
                "UPDATE agency SET membership_id = " + membership_cycle_start_id + "," +
                        "membership_cycle_start_id = " + membership_cycle_start_id + "," +
                        "code = '" + code + "'" +
                        " WHERE id = " + agency_id
        );
    }

    public boolean resetAcoin(int agency_id, int pre_point) {
        return this.masterDB.update(
                "UPDATE agency SET current_point = " + 0 + "," +
                        "pre_point = " + pre_point +
                        " WHERE id = " + agency_id
        );
    }

    public List<JSONObject> getListAgencyMembershipYear(int year) {
        return this.masterDB.find(
                "SELECT * FROM agency_membership_year WHERE year = " + year
        );
    }

    public List<JSONObject> getListAgencyAcoinYear(int year) {
        return this.masterDB.find(
                "SELECT * FROM agency_acoin_year WHERE year = " + year + " AND reward > 0"
        );
    }

    public List<JSONObject> getListAgencyCNOYear(int year) {
        return this.masterDB.find(
                "SELECT * FROM agency_cno_year WHERE year = " + year
        );
    }

    public boolean createNickname(int id, String nick_name) {
        return this.masterDB.update(
                "UPDATE agency SET nick_name = " + parseStringToSql(nick_name) +
                        " WHERE id = " + id);
    }

    public boolean editAgencyContractInfo(int agency_id, String tax_number) {
        return this.masterDB.update(
                "UPDATE agency SET tax_number = " + parseStringToSql(tax_number) +
                        " WHERE id = " + agency_id
        );
    }

    public boolean setStaffManageAgency(int agency_id, Integer staff_id) {
        return this.masterDB.update(
                "UPDATE agency SET staff_manage_id = " + staff_id +
                        " WHERE id = " + agency_id
        );
    }

    public boolean rejectAgency(int agency_id, String note, String phone) {
        return this.masterDB.update(
                "UPDATE agency SET status = " + AgencyStatus.REJECT.getValue() + "," +
                        " approved_date = NOW()" + "," +
                        " note = " + parseStringToSql(note) + "," +
                        " phone = " + parseStringToSql(phone) + "" +
                        " WHERE id = " + agency_id);
    }

    public boolean deleteAgencyAccount(int agency_id) {
        return this.masterDB.update("DELETE FROM agency_account WHERE agency_id = " + agency_id);
    }

    public boolean setStaffSupportAgency(int agency_id, Integer staff_id) {
        return this.masterDB.update(
                "UPDATE agency SET staff_support_id = " + staff_id +
                        " WHERE id = " + agency_id
        );
    }

    public List<JSONObject> getListAgencyMissionByAgency(int agency_id, int mission_type_id) {
        return this.masterDB.find("SELECT * FROM agency_mission WHERE agency_id = " + agency_id +
                " AND mission_type_id = " + mission_type_id);
    }

    public List<JSONObject> getAllAgencyMissionByAgency(int agency_id) {
        return this.masterDB.find("SELECT * FROM agency_mission WHERE agency_id = " + agency_id);
    }

    public JSONObject getMissionPoint(int agency_id) {
        return this.masterDB.getOne("SELECT * FROM agency_mission_point WHERE agency_id = " + agency_id);
    }

    public int insertAgencyWaitingGenerateMission(int agency_id) {
        return this.masterDB.insert("INSERT INTO mission_waiting_generate(agency_id) VALUES(" + agency_id + ")");
    }

    public List<JSONObject> getListAgencyByIds(List<String> data) {
        return this.masterDB.find(
                "SELECT  id,shop_name,code,membership_id,business_department_id,phone,address,avatar,status,city_id,nick_name" +
                        " FROM agency" +
                        " WHERE '" + JsonUtils.Serialize(data) + "' LIKE " + "CONCAT('%\"',id,'\"%')"
        );
    }

    public Object getSupplierInfo(int id) {
        return this.masterDB.getOne("SELECT * FROM supplier WHERE id = " + id);
    }

    public int countAgencyApproved(int businessDepartmentId) {
        return this.masterDB.getTotal("SELECT * FROM agency WHERE status = " + AgencyStatus.APPROVED.getValue());
    }

    public boolean updateAgencyCode(int id, String code) {
        return this.masterDB.update("UPDATE agency SET code = " + parseStringToSql(code) + " WHERE id=" + id);
    }
}