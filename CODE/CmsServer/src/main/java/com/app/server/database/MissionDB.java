package com.app.server.database;

import com.app.server.config.ConfigInfo;
import com.app.server.data.dto.mission.ApplyObjectRequest;
import com.app.server.data.dto.mission.MissionBXHData;
import com.app.server.data.dto.mission.TransactionInfo;
import com.app.server.data.request.mission.*;
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MissionDB extends BaseDB {
    private MasterDB masterDB;

    @Autowired
    public void setMasterDB(MasterDB masterDB) {
        this.masterDB = masterDB;
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

    public JSONObject getMissionGroupInfo(int id) {
        return this.masterDB.getOne("SELECT * FROM mission_group WHERE id = " + id);
    }

    public List<JSONObject> getListMissionByGroup(int mission_group_id) {
        return this.masterDB.find("SELECT * FROM misssion WHERE mission_group_id = " + mission_group_id);
    }

    public JSONObject getMissionSettingInfo(int id) {
        return this.masterDB.getOne("SELECT * FROM mission_setting WHERE id = " + id);
    }

    public JSONObject getMissionBXH(int id) {
        return this.masterDB.getOne("SELECT * FROM mission_bxh WHERE id = " + id);
    }

    public JSONObject getVRP(int id) {
        return this.masterDB.getOne("SELECT * FROM voucher_release_period WHERE id = " + id);
    }

    public int createMissionBXH(String code, MissionBXHInfoRequest info, ApplyObjectRequest applyObject, List<MissionBXHLimitRequest> limits) {
        int id = 0;
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String query =
                    "INSERT INTO mission_bxh(" +
                            "code," +
                            "name," +
                            "description," +
                            "image," +
                            "apply_object_data," +
                            "offer_data," +
                            "start_date," +
                            "status," +
                            "type," +
                            "mission_period_id," +
                            "name_app," +
                            "agency_position_rank_limit," +
                            "require_accumulate_value," +
                            "total_offer_value," +
                            "show_total_offer_value_in_app)" +
                            " VALUES(" +
                            "'" + code + "'," +
                            "'" + info.getName() + "'," +
                            "?," +
                            "'" + info.getImage() + "'," +
                            "?," +
                            "'" + JsonUtils.Serialize(limits) + "'," +
                            parseDateToSql(DateTimeUtils.getDateTime(info.getStart_date_millisecond())) + "," +
                            MissionBXHStatus.DRAFT.getId() + "," +
                            info.getType() + "," +
                            info.getMission_period_id() + "," +
                            parseStringToSql(info.getName_app()) + "," +
                            info.getAgency_position_rank_limit() + "," +
                            info.getRequire_accumulate_value() + "," +
                            info.getTotal_offer_value() + "," +
                            info.getShow_total_offer_value_in_app() +
                            ")";
            try (PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, info.getDescription());
                stmt.setString(2, JsonUtils.Serialize(applyObject));
                int row = stmt.executeUpdate();
                if (row > 0) {
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            id = rs.getInt(1);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            id = 0;
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
        return id;
    }

    public int getTotalMissionBXH() {
        return this.masterDB.getTotal("SELECT * FROM mission_bxh");
    }

    public List<JSONObject> getListVRPItem(int id) {
        return this.masterDB.find("SELECT * FROM voucher_release_period_item WHERE voucher_release_period_id = " + id);
    }

    public boolean updateMissionBXH(int id, MissionBXHInfoRequest info, ApplyObjectRequest applyObject, List<MissionBXHLimitRequest> limits) {
        boolean rs = false;
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String query =
                    "UPDATE mission_bxh SET" +
                            " name = " + parseStringToSql(info.getName()) + "," +
                            " name_app = " + parseStringToSql(info.getName_app()) + "," +
                            " description = ?," +
                            " image = " + parseStringToSql(info.getImage()) + "," +
                            " mission_period_id = " + info.getMission_period_id() + "," +
                            " type = " + info.getType() + "," +
                            " agency_position_rank_limit = " + info.getAgency_position_rank_limit() + "," +
                            " require_accumulate_value = " + info.getRequire_accumulate_value() + "," +
                            " show_total_offer_value_in_app = " + info.getShow_total_offer_value_in_app() + "," +
                            " total_offer_value = " + info.getTotal_offer_value() + "," +
                            " apply_object_data = ?," +
                            " offer_data = " + parseStringToSql(JsonUtils.Serialize(limits)) + "," +
                            " start_date = " + parseDateToSql(DateTimeUtils.getDateTime(info.getStart_date_millisecond())) +
                            " WHERE id = " + id;
            try (PreparedStatement stmt = con.prepareStatement(query)) {
                stmt.setString(1, info.getDescription());
                stmt.setString(2, JsonUtils.Serialize(applyObject));
                int row = stmt.executeUpdate();
                if (row > 0) {
                    rs = true;
                }
            }
        } catch (Exception ex) {
            rs = false;
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
        return rs;
    }

    public boolean startMissionBXH(int id) {
        return this.masterDB.update(
                "UPDATE mission_bxh SET status = " + MissionBXHStatus.RUNNING.getId() + ", effect_date = NOW() WHERE id = " + id);
    }

    public boolean updateAgencyDataForMissionBXH(int id, String agency_data) {
        return this.masterDB.update("UPDATE mission_bxh SET agency_data = " + parseStringToSql(agency_data) + " WHERE id = " + id);
    }

    public int insertMissionBXHAgencyJoin(Integer agency_id, int id, int type) {
        return this.masterDB.insert("INSERT INTO mission_bxh_agency_join(agency_id, mission_bxh_id, type)" +
                " VALUES(" + agency_id + ", " + id + "," + type + ")");
    }

    public boolean cancelMissionBXH(int id) {
        return this.masterDB.update("UPDATE mission_bxh SET status = " + MissionBXHStatus.CANCEL.getId() + " WHERE id = " + id);
    }

    public boolean deleteMissionBXH(int id) {
        return this.masterDB.update("UPDATE mission_bxh SET status = " + MissionBXHStatus.DELETE.getId() + " WHERE id = " + id);
    }

    public int getSoLuongThamGiaMissionBXH(int mission_bxh_id) {
        return this.masterDB.getTotal("SELECT id FROM mission_bxh_agency_join WHERE mission_bxh_id = " + mission_bxh_id);
    }

    public int insertMissionBXHHistory(int mission_bxh_id, JSONObject data, String note, int staffId, int status) {
        int id = 0;
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String query =
                    "INSERT INTO mission_bxh_history(mission_bxh_id,data,note,creator_id,status) VALUES(" +
                            mission_bxh_id + "," +
                            "?," +
                            parseStringToSql(note) + "," +
                            staffId + "," +
                            status + ")";
            try (PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, JsonUtils.Serialize(data));
                int row = stmt.executeUpdate();
                if (row > 0) {
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            id = rs.getInt(1);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            id = 0;
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
        return id;

    }

    public int createMission(MissionRequest mission, MissionConditionRequest missionConditionRequest, int mission_group_id, int mission_period_id) {
        return this.masterDB.insert(
                "INSERT INTO `mission` (" +
                        "`name`, " +
                        "`mission_period_id`, " +
                        "`mission_type_id`, " +
                        "`action_number`, " +
                        "`required_value`, " +
                        "`mission_unit_id`," +
                        " `action_status`, " +
                        "`item_data`," +
                        "`mission_group_id`) VALUES (" +
                        parseStringToSql(mission.getName()) + "," +
                        parseIntegerToSql(mission_period_id) + "," +
                        mission.getMission_type_id() + "," +
                        missionConditionRequest.getAction_number() + "," +
                        missionConditionRequest.getRequired_value() + "," +
                        mission.getMission_unit_id() + "," +
                        mission.getAction_status() + "," +
                        (mission.getItem_data() == null ? parseStringToSql(null) : parseStringToSql(JsonUtils.Serialize(mission.getItem_data()))) + "," +
                        mission_group_id + ")");
    }

    public int insertMissionGroup(String name, int staff_id) {
        return this.masterDB.insert(
                "INSERT INTO `mission_group` (" +
                        "`name`, " +
                        "`creator_id`," +
                        "status) VALUES(" +
                        parseStringToSql(name) + "," +
                        staff_id + "," + MissionGroupStatus.DRAFT.getId() + ")");
    }

    public JSONObject getMission(int id) {
        return this.masterDB.getOne("SELECT * FROM mission WHERE id =" + id);
    }

    public boolean cancelMission(int id) {
        return this.masterDB.update("UPDATE mission SET status = " + MissionStatus.DELETE.getId() + " WHERE id = " + id);

    }

    public boolean updateMissionGroup(EditMissionGroupRequest request) {
        return this.masterDB.update("UPDATE mission_group SET name = " + parseStringToSql(request.getName()) + " WHERE id = " + request.getId());
    }

    public int insertMissionGroupHistory(int link_id, JSONObject data, String note, int staffId, int status) {
        return this.masterDB.insert(
                " INSERT INTO mission_group_history(link_id,data,note,creator_id,status) VALUES(" +
                        link_id + "," +
                        parseStringToSql(JsonUtils.Serialize(data)) + "," +
                        parseStringToSql(note) + "," +
                        staffId + "," +
                        status + ")");
    }

    public int insertMissionSettingHistory(int link_id,
                                           JSONObject data,
                                           String note,
                                           int staff_id,
                                           int status) {
        return this.masterDB.insert(
                " INSERT INTO mission_setting_history(link_id,data,note,creator_id,status) VALUES(" +
                        link_id + "," +
                        parseStringToSql(JsonUtils.Serialize(data)) + "," +
                        parseStringToSql(note) + "," +
                        staff_id + "," +
                        status + ")");
    }

    public int insertMissionSetting(CreateMissionSettingRequest request, int creator_id) {
        return this.masterDB.insert(
                "INSERT INTO `mission_setting` (" +
                        "`name`, " +
                        "`mission_group_id`, " +
                        "`generate_rate_data`, " +
                        "`apply_object_data`, " +
                        "`offer_data`, " +
                        "`status`," +
                        "`start_date`," +
                        "creator_id)" +
                        " VALUES (" +
                        parseStringToSql(request.getName()) + ", " +
                        request.getMission_group_id() + ", " +
                        parseStringToSql(JsonUtils.Serialize(request.getGenerate_rate_data())) + ", " +
                        parseStringToSql(JsonUtils.Serialize(request.getApply_object())) + ", " +
                        parseStringToSql(JsonUtils.Serialize(request.getOffer_data())) + ", " +
                        MissionSettingStatus.DRAFT.getId() + "," +
                        parseDateToSql(DateTimeUtils.getDateTime(request.getStart_date_millisecond())) + "," +
                        creator_id +
                        ")");
    }

    public boolean startMissionSetting(int id) {
        return this.masterDB.update("UPDATE mission_setting SET status = " + MissionSettingStatus.RUNNING.getId() + " WHERE id = " + id);
    }

    public boolean cancelMissionSetting(int id) {
        return this.masterDB.update("UPDATE mission_setting SET status = " + MissionSettingStatus.CANCEL.getId() + " WHERE id = " + id);
    }

    public boolean deleteMissionSetting(int id) {
        return this.masterDB.update("UPDATE mission_setting SET status = " + MissionSettingStatus.DELETE.getId() + " WHERE id = " + id);
    }

    public boolean updateMissionSetting(int id, CreateMissionSettingRequest request) {
        return this.masterDB.update(
                "UPDATE `mission_setting` SET" +
                        " `name` = " + parseStringToSql(request.getName()) + ", " +
                        " `mission_group_id` = " + parseIntegerToSql(request.getMission_group_id()) + ", " +
                        " `generate_rate_data` = " + parseStringToSql(JsonUtils.Serialize(request.getGenerate_rate_data())) + ", " +
                        " `apply_object_data` = " + parseStringToSql(JsonUtils.Serialize(request.getApply_object())) + ", " +
                        "`offer_data`= " + parseStringToSql(JsonUtils.Serialize(request.getOffer_data())) + "," +
                        "`start_date` = " + parseDateToSql(DateTimeUtils.getDateTime(request.getStart_date_millisecond())) + " WHERE id = " + id);

    }

    public JSONObject getMissionBXHHistory(int id) {
        return this.masterDB.getOne("SELECT * FROM mission_bxh_history WHERE id = " + id);
    }

    public boolean deleteMissionSettingAgencyJoin(int mission_setting_id) {
        return this.masterDB.update("DELETE FROM mission_setting_agency_join WHERE mission_setting_id = " + mission_setting_id);
    }

    public long getTongHuyHieuTKMissionAgency(int mission_setting_id, int agency_id) {
        JSONObject rs = this.masterDB.getOne("SELECT SUM(mission_reward_point) as total FROM agency_mission WHERE agency_id = " + agency_id +
                " AND mission_setting_id = " + mission_setting_id +
                " AND status = " + MissionAgencyStatus.FINISH.getId());
        if (rs != null) {
            return ConvertUtils.toLong(rs.get("total"));
        }
        return 0;
    }

    public int getTongNhiemVuTKMissionAgency(int mission_setting_id, int agency_id) {
        JSONObject rs = this.masterDB.getOne("SELECT COUNT(id) as total FROM agency_mission WHERE agency_id = " + agency_id +
                " AND mission_setting_id = " + mission_setting_id);
        if (rs != null) {
            return ConvertUtils.toInt(rs.get("total"));
        }
        return 0;
    }

    public int getTongNhiemVuHoanThanhTKMissionAgency(int mission_setting_id, int agency_id) {
        JSONObject rs = this.masterDB.getOne("SELECT COUNT(id) as total FROM agency_mission WHERE agency_id = " + agency_id +
                " AND mission_setting_id = " + mission_setting_id +
                " AND status = " + MissionAgencyStatus.FINISH.getId());
        if (rs != null) {
            return ConvertUtils.toInt(rs.get("total"));
        }
        return 0;
    }

    public JSONObject getAgencyMissionInfo(int id) {
        return this.masterDB.getOne("SELECT * FROM agency_mission WHERE id = " + id);
    }

    public JSONObject getAgencyOrderDeptTransactionInfo(int id) {
        return this.masterDB.getOne("SELECT agency_order_id as id, dept_code as code, total_end_price as transaction_value, created_date FROM agency_order_dept WHERE id = " + id);
    }

    public JSONObject getAgencyOrderDeptTransactionInfoByDeptCode(String dept_code) {
        return this.masterDB.getOne("SELECT agency_order_id as id, dept_code as code, total_end_price as transaction_value, created_date FROM agency_order_dept WHERE dept_code = " + parseStringToSql(dept_code));
    }

    public JSONObject getDeptTransaction(String dept_code) {
        return this.masterDB.getOne("SELECT id, doc_no as code, transaction_value, created_date, dept_transaction_sub_type_id, description" +
                " FROM dept_transaction WHERE doc_no = " + parseStringToSql(dept_code));
    }

    public boolean editMission(MissionRequest mission, MissionConditionRequest missionConditionRequest, int mission_group_id, int mission_period_id) {
        return this.masterDB.update(
                "UPDATE `mission` SET " +
                        "`name` = " + parseStringToSql(mission.getName()) + "," +
                        "`mission_period_id` = " + parseIntegerToSql(mission_period_id) + "," +
                        "`mission_type_id` = " + mission.getMission_type_id() + "," +
                        "`action_number` = " + missionConditionRequest.getAction_number() + "," +
                        "`required_value` = " + missionConditionRequest.getRequired_value() + "," +
                        "`mission_unit_id` = " + mission.getMission_unit_id() + "," +
                        " `action_status` = " + mission.getAction_status() + "," +
                        "`item_data` = " + (mission.getItem_data() == null ? parseStringToSql(null) : parseStringToSql(JsonUtils.Serialize(mission.getItem_data()))) + " WHERE id = " + mission.getId());
    }

    public int insertMissionSettingAgencyJoin(Integer agency_id, int mission_setting_id) {
        return this.masterDB.insert("INSERT INTO mission_setting_agency_join(agency_id, mission_setting_id)" +
                " VALUES(" + agency_id + ", " + mission_setting_id + ")");
    }

    public List<JSONObject> getListMissionConfig() {
        return this.masterDB.find("SELECT * FROM mission_config");
    }

    public boolean updateMissionConfig(String type, String data) {
        boolean rs = false;
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String query =
                    "UPDATE mission_config SET data = ?" +
                            " WHERE code = " + parseStringToSql(type);
            try (PreparedStatement stmt = con.prepareStatement(query)) {
                stmt.setString(1, data);
                int row = stmt.executeUpdate();
                if (row > 0) {
                    rs = true;
                }
            }
        } catch (Exception ex) {
            rs = false;
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
        return rs;
    }

    public List<JSONObject> getListMissionSettingNeedStart() {
        return this.masterDB.find("SELECT * FROM mission_setting WHERE status = " + MissionSettingStatus.WAITING.getId() +
                " AND start_date <= NOW()");
    }

    public boolean activeMissionSetting(int id, Date start_date) {
        return this.masterDB.update("UPDATE mission_setting SET status = " + MissionSettingStatus.WAITING.getId() + "," +
                " start_date = " + parseDateToSql(start_date) + " WHERE id = " + id);
    }

    public int getTotalMissionByGroup(int mission_group_id) {
        JSONObject rs = this.masterDB.getOne("SELECT count(*) as total FROM mission WHERE mission_group_id = " + mission_group_id +
                " AND status != -1");
        if (rs != null) {
            return ConvertUtils.toInt(rs.get("total"));
        }
        return 0;
    }

    public boolean stopMissionSetting(int id, String note) {
        return this.masterDB.update("UPDATE mission_setting SET status = " + MissionSettingStatus.STOP.getId() + "," +
                " end_date = NOW()," +
                " note = " + parseStringToSql(note) +
                " WHERE id = " + id);
    }

    public List<JSONObject> getListOrderPrepareOver(String time) {
        return this.masterDB.find(
                "SELECT * FROM agency_order WHERE confirm_prepare_date < " + parseStringToSql(time) +
                        " AND status = " + OrderStatus.PREPARE.getKey());
    }

    public List<JSONObject> getListMissionSettingByAgency(int agency_id) {
        return this.masterDB.find("SELECT t1.id, t1.name, t1.status" +
                " FROM mission_setting_agency_join t" +
                " LEFT JOIN mission_setting t1 ON t1.id = t.mission_setting_id WHERE t.agency_id = " + agency_id);
    }

    public List<JSONObject> getListMissionBXHByAgency(int agency_id) {
        return this.masterDB.find("SELECT t1.id, t1.code, t1.name, t1.status" +
                " FROM mission_bxh_agency_join t LEFT JOIN mission_bxh t1 ON t1.id = t.mission_bxh_id WHERE t.agency_id = " + agency_id);
    }

    public JSONObject getAgencyMissionPoint(int agency_id) {
        return this.masterDB.getOne("SELECT * FROM agency_mission_point WHERE agency_id = " + agency_id);
    }

    public int initAgencyMisionPoint(int agency_id) {
        return this.masterDB.insert("INSERT INTO agency_mission_point(agency_id, point, updated_date, created_date) VALUES(" +
                agency_id + ",0, NOW(), NOW())");
    }

    public boolean increaseAgencyMissionPoint(int agency_id, int point) {
        return this.masterDB.update("UPDATE agency_mission_point SET point = point + " + point +
                " WHERE agency_id = " + agency_id);
    }

    public boolean decreaseAgencyMissionPoint(int agency_id, int point) {
        return this.masterDB.update("UPDATE agency_mission_point SET point = point - " + point +
                " WHERE agency_id = " + agency_id);
    }

    public int saveAgencyMissionPointHistory(
            int agency_id, long begin_point, int update_point, long end_point, Integer data_id, String note) {
        return this.masterDB.insert("INSERT INTO agency_mission_point_history(" +
                "agency_id," +
                "begin_point," +
                "update_point," +
                "end_point," +
                "data_id," +
                "note) VALUES(" +
                agency_id + "," +
                begin_point + "," +
                update_point + "," +
                end_point + "," +
                parseIntegerToSql(data_id) + "," +
                parseStringToSql(note) + ")");
    }

    public List<JSONObject> getListAgencyMission(int agency_id, int mission_period_id) {
        return this.masterDB.find("SELECT * FROM agency_mission WHERE agency_id = " + agency_id +
                " AND mission_period_id = " + mission_period_id);
    }

    public int insertMissionPeriodAgency(int agency_id, int mission_period_id,
                                         Date start_date,
                                         Date end_date,
                                         String mission_period_code,
                                         int mission_setting_id) {
        return this.masterDB.insert("INSERT INTO mission_period_agency(agency_id," +
                "mission_period_id," +
                "start_date," +
                "end_date," +
                "mission_period_code," +
                "mission_setting_id)" +
                " VALUES(" +
                agency_id + "," +
                mission_period_id + "," +
                parseDateToSql(start_date) + "," +
                parseDateToSql(end_date) + "," +
                parseStringToSql(mission_period_code) + "," +
                mission_setting_id +
                ")");
    }

    public JSONObject getMissionPeriodRunning(Integer mission_period_id) {
        return this.masterDB.getOne("SELECT * FROM mission_period_running WHERE mission_period_id=" + mission_period_id);
    }

    public boolean savePushedThongBaoKhiKetThucNhiemVu(Integer mission_period_id) {
        return this.masterDB.update("UPDATE mission_period_running SET push_notify_claim = 1 WHERE mission_period_id = " + mission_period_id);
    }

    public long getTongGiaTriTichLuyBXH(int mission_bxh_id) {
        JSONObject result = this.masterDB.getOne("SELECT sum(t1.point) as total" +
                " FROM mission_bxh_agency_join t" +
                " LEFT JOIN agency_mission_point t1 ON t1.agency_id = t.agency_id" +
                " WHERE t.mission_bxh_id = " + mission_bxh_id);
        if (result != null) {
            return ConvertUtils.toLong(result.get("total"));
        }
        return 0;
    }

    public int getTongKhachHangThamGiaBXH(int mission_bxh_id) {
        JSONObject result = this.masterDB.getOne("SELECT count(*) as total" +
                " FROM mission_bxh_agency_join t" +
                " WHERE t.mission_bxh_id = " + mission_bxh_id);
        if (result != null) {
            return ConvertUtils.toInt(result.get("total"));
        }
        return 0;
    }

    public int insertMissionSettingAgency(
            int mission_setting_id,
            int mission_period_id,
            String agency_data,
            Date start_date,
            Date end_date) {
        return this.masterDB.insert("INSERT INTO mission_setting_agency(" +
                "mission_setting_id," +
                "mission_period_id," +
                "start_date," +
                "end_date," +
                "agency_data)" +
                " VALUES(" +
                mission_setting_id + "," +
                mission_period_id + "," +
                parseDateToSql(start_date) + "," +
                parseDateToSql(end_date) + "," +
                parseStringToSql(agency_data) +
                ")");
    }

    public boolean stopMissionSettingAgency(int mission_setting_id) {
        return this.masterDB.update("UPDATE mission_setting_agency SET status = " + MissionSettingStatus.STOP.getId() +
                " WHERE mission_setting_id = " + mission_setting_id);
    }

    public JSONObject getMissionAgencyData(int agency_id, int kyDai) {
        return this.masterDB.getOne("SELECT * FROM agency_mission_data WHERE agency_id = " + agency_id +
                " LIMIT 1");
    }

    public JSONObject getHBTL(int agency_hbtl_id) {
        return this.masterDB.getOne("SELECT id, code, total_end_price as transaction_value, created_date" +
                " FROM agency_hbtl WHERE id = " + agency_hbtl_id);
    }

    public int insertMissionPeriodRunning(int missionPeriodId, Date startDate, Date endDate, Date endTLDate) {
        return this.masterDB.insert("INSERT INTO mission_period_running(" +
                " mission_period_id," +
                " start_date," +
                " end_date," +
                " end_tl_date) VALUES(" +
                missionPeriodId + "," +
                parseDateToSql(startDate) + "," +
                parseDateToSql(endDate) + "," +
                parseDateToSql(endTLDate) +
                ")");
    }

    public boolean updateMissionPeriodRunning(int missionPeriodId, Date startDate, Date endDate) {
        return this.masterDB.update("UPDATE mission_period_running SET" +
                " start_date = " + parseDateToSql(startDate) + "," +
                " end_date = " + parseDateToSql(endDate) +
                "  WHERE mission_period_id = " + missionPeriodId);
    }

    public boolean resetMissionPeriodRunning(
            int missionPeriodId,
            Date startDate,
            Date endDate,
            Date endTLDate) {
        return this.masterDB.update("UPDATE mission_period_running SET" +
                " start_date = " + parseDateToSql(startDate) + "," +
                " end_date = " + parseDateToSql(endDate) + "," +
                " end_tl_date = " + parseDateToSql(endTLDate) + "," +
                " push_notify_claim = 0" +
                "  WHERE mission_period_id = " + missionPeriodId);
    }

    public List<JSONObject> getListAgencyByMissionPeriod(int mission_period_id) {
        return this.masterDB.find("SELECT agency_id FROM agency_mission WHERE mission_period_id = " + mission_period_id +
                " GROUP BY agency_id");
    }

    public List<JSONObject> getListAgencyOrderPrepareOver(Date time) {
        return this.masterDB.find(
                "SELECT id, code, confirm_prepare_date" +
                        " FROM agency_order WHERE" +
                        " status IN (0,5) AND confirm_prepare_date > " +
                        parseDateToSql(time));
    }

    public boolean saveAgencyOrderEstimateTime(int id, Date time) {
        return this.masterDB.update("UPDATE agency_order SET" +
                " accumulate_mission_date = " + parseDateToSql(time) +
                "  WHERE id = " + id);
    }

    public List<JSONObject> getListOrderReady(int scheduleRunningLimit) {
        return this.masterDB.find("SELECT id, code, confirm_prepare_date" +
                " FROM agency_order WHERE status IN (0,5) AND type IN (1,2)" +
                " AND accumulate_mission_status = 0 AND accumulate_mission_date is null" +
                " LIMIT " + scheduleRunningLimit);
    }

    public boolean saveConfirmPrepareDate(int id) {
        return this.masterDB.update("UPDATE agency_order SET" +
                " confirm_prepare_date = update_status_date" +
                "  WHERE id = " + id);
    }

    public List<JSONObject> getListOrderReadyTichLuy(int scheduleRunningLimit) {
        return this.masterDB.find("SELECT id, agency_id, code, confirm_prepare_date" +
                " FROM agency_order" +
                " WHERE status IN (0,5) AND type IN (1,2)" +
                " AND accumulate_mission_status = 0" +
                " AND accumulate_mission_over_status = 0" +
                " AND accumulate_mission_date <= NOW()" +
                " LIMIT " + scheduleRunningLimit);
    }

    public List<JSONObject> getListMissionPeriodNeedReward(int scheduleRunningLimit) {
        return this.masterDB.find("SELECT *" +
                " FROM agency_mission_period_running" +
                " WHERE (NOW() BETWEEN reward_bxh_date AND end_date)" +
                " AND reward_bxh_status = 0");
    }

    public long getDttLastYear(int agency_id, int year) {
        JSONObject js = this.masterDB.getOne("SELECT * FROM dept_dtt_year WHERE agency_id = " + agency_id +
                " AND year = " + year + " LIMIT 1");
        if (js != null) {
            return ConvertUtils.toLong(js.get("end_value"));
        }
        return 0;
    }

    public List<JSONObject> getListMissionSettingRunning() {
        return this.masterDB.find("SELECT *" +
                " FROM mission_setting" +
                " WHERE status = " + MissionSettingStatus.RUNNING.getId());
    }

    public List<JSONObject> getListMissionSettingAgency(int mission_setting_id) {
        return this.masterDB.find("SELECT *" +
                " FROM mission_setting_agency" +
                " WHERE mission_setting_agency = " + mission_setting_id);
    }

    public boolean updateAgencyDataForMissionSettingAgency(int mission_setting_id, String agency_data) {
        return this.masterDB.update("UPDATE mission_setting_agency SET agency_data = " + parseStringToSql(agency_data) + " WHERE mission_setting_id = " + mission_setting_id);
    }

    public List<JSONObject> getListMissionSettingAgencyJoin(int mission_setting_id) {
        return this.masterDB.find("SELECT *" +
                " FROM mission_setting_agency_join" +
                " WHERE mission_setting_id = " + mission_setting_id);
    }

    public List<JSONObject> getListMissionBXHRunning() {
        return this.masterDB.find("SELECT *" +
                " FROM mission_bxh" +
                " WHERE status = " + MissionBXHStatus.RUNNING.getId() +
                " ORDER BY effect_date ASC, id ASC");
    }

    public JSONObject getVRPAcoin() {
        return this.masterDB.getOne("SELECT *" +
                " FROM voucher_release_period" +
                " WHERE offer_type = " + parseStringToSql(VoucherOfferType.ACOIN.getKey()) +
                " LIMIT 1");
    }

    public boolean clearMissionBXHAgencyJoin(int mission_bxh_id) {
        return this.masterDB.update("DELETE FROM mission_bxh_agency_join WHERE mission_bxh_id = " + mission_bxh_id);

    }

    public JSONObject getOneMissionAny() {
        return this.masterDB.getOne(
                "SELECT * FROM agency_mission LIMIT 1"
        );
    }

    public boolean activeMissionGroup(int id) {
        return this.masterDB.update("UPDATE mission_group" +
                " set status = " + MissionGroupStatus.RUNNING.getId() + " WHERE id = " + id);
    }

    public boolean stopMissionGroup(int id) {
        return this.masterDB.update("UPDATE mission_group" +
                " set status = " + MissionGroupStatus.STOP.getId() + " WHERE id = " + id);
    }

    public List<JSONObject> getListMissionSettingByMissionGroup(int mission_group_id) {
        return masterDB.find("SELECT id,name,start_date,status FROM mission_setting WHERE mission_group_id = " + mission_group_id);
    }

    public List<JSONObject> getListMissionPeriodRunning() {
        return this.masterDB.find("SELECT * FROM mission_period_running");
    }

    public JSONObject getOneMissionSettingOfAgency(int agency_id) {
        return this.masterDB.getOne("SELECT * FROM agency_mission_changed_data WHERE agency_id = " + agency_id + " LIMIT 1");
    }

    public boolean clearAgencyDataForMissionBXH(MissionBXHType missionBXHType) {
        return this.masterDB.update("DELETE FROM mission_bxh_agency_join WHERE type = " + missionBXHType.getId());
    }

    public List<JSONObject> getListMissionBXHRunningByType(int type) {
        return this.masterDB.find("SELECT *" +
                " FROM mission_bxh" +
                " WHERE status = " + MissionBXHStatus.RUNNING.getId() +
                " AND type = " + type +
                " ORDER BY effect_date ASC, id ASC");
    }

    public JSONObject getMissionBXHAgencyJoin(int type, int agency_id) {
        return this.masterDB.getOne("SELECT *" +
                " FROM mission_bxh_agency_join" +
                " WHERE type = " + type +
                " AND agency_id = " + agency_id +
                " LIMIT 1");
    }

    public JSONObject getAgencyMissionInfoEarly(int mission_setting_id, int agency_id) {
        return this.masterDB.getOne("SELECT *" +
                " FROM agency_mission" +
                " WHERE mission_setting_id = " + mission_setting_id +
                " AND agency_id = " + agency_id +
                " ORDER BY id DESC " +
                " LIMIT 1");
    }

    public JSONObject getAgencyMissionInfoLasted(int mission_setting_id, int agency_id) {
        return this.masterDB.getOne("SELECT *" +
                " FROM agency_mission" +
                " WHERE mission_setting_id = " + mission_setting_id +
                " AND agency_id = " + agency_id +
                " ORDER BY mission_end_date DESC " +
                " LIMIT 1");
    }

    public List<JSONObject> getListMissionSetting(int mission_group_id, int status) {
        return this.masterDB.find(
                "SELECT * FROM mission_setting WHERE mission_group_id = " + mission_group_id +
                        " AND status = " + status);
    }

    public int getTotalMissionBXHPoint(int agency_id, String startDate, String endDate) {
        JSONObject js = this.masterDB.getOne("SELECT SUM(mission_point) as total" +
                " FROM agency_mission_bxh_info WHERE agency_id = " + agency_id +
                " AND created_date >= '" + startDate + " 00:00:00'" +
                " AND created_date <= '" + endDate + " 23:59:59'");
        if (js == null) {
            return 0;
        }
        return ConvertUtils.toInt(js.get("total"));
    }

    public JSONObject getListMissionBXHByAgency(int agency_id, int type) {
        return this.masterDB.getOne("SELECT t1.id, t1.code, t1.name, t1.status" +
                " FROM mission_bxh_agency_join t LEFT JOIN mission_bxh t1 ON t1.id = t.mission_bxh_id" +
                " WHERE t.agency_id = " + agency_id + " AND t.type=" + type);
    }

    public JSONObject getAgencyMissionPeriodRunning(int agencyMissionPeriodRunningId) {
        return this.masterDB.getOne("SELECT * FROM agency_mission_period_running WHERE id =" + agencyMissionPeriodRunningId);
    }

    public int getTongKhachHangThamGiaBXHByPeriod(int mission_bxh_id, String mission_period_code) {
        JSONObject result = this.masterDB.getOne("SELECT count(*) as total" +
                " FROM mission_bxh_agency_join t" +
                " WHERE t.mission_bxh_id = " + mission_bxh_id +
                " AND mission_period_code = " + parseStringToSql(mission_period_code));
        if (result != null) {
            return ConvertUtils.toInt(result.get("total"));
        }
        return 0;
    }

    public JSONObject getAgencyInfo(int agency_id) {
        return this.masterDB.getOne("SELECT id,shop_name,code,membership_id,business_department_id,phone,address,avatar,status,city_id,nick_name" +
                " FROM agency WHERE id = " + agency_id);
    }

    public int getTotalMissionPoint(String query) {
        JSONObject js = this.masterDB.getOne("SELECT sum(update_point) as total" +
                " FROM (" + query + ") as a");
        if (js != null) {
            return ConvertUtils.toInt(js.get("total"));
        }
        return 0;
    }

    public JSONObject getMissionSettingBasicInfo(int id) {
        return this.masterDB.getOne("SELECT id,name,mission_group_id FROM mission_setting WHERE id = " + id);
    }

    public List<JSONObject> getListMissionWaitingGenerate(int scheduleRunningLimit) {
        return this.masterDB.find("SELECT * FROM mission_waiting_generate WHERE status = 0 LIMIT " + scheduleRunningLimit);
    }

    public boolean setMissionWaitingGenerateToDone(int missionWaitingGenerateId) {
        return this.masterDB.update("UPDATE mission_waiting_generate SET status = 1 WHERE id = " + missionWaitingGenerateId);
    }

    public JSONObject getLastData(int agency_id, Date startDate, Date endDate) {
        return this.masterDB.getOne("SELECT *" +
                " FROM agency_mission_bxh_info WHERE agency_id = " + agency_id +
                " AND created_date >= '" + DateTimeUtils.toString(startDate, "yyyy-MM-dd") + " 00:00:00'" +
                " AND created_date <= '" + DateTimeUtils.toString(endDate, "yyyy-MM-dd") + " 23:59:59'" +
                " ORDER BY id DESC" +
                " LIMIT 1");
    }

    public List<JSONObject> getListMissionPeriodNeedRewardForReset(int scheduleRunningLimit) {
        return this.masterDB.find("SELECT *" +
                " FROM agency_mission_period_running" +
                " WHERE reward_bxh_status = 0");
    }

    public List<JSONObject> getListAgencyMissionByMissionType(int agency_id, int mission_type_id) {
        return this.masterDB.find("SELECT * FROM agency_mission WHERE agency_id = " + agency_id +
                " AND mission_type_id = " + mission_type_id +
                " AND status != " + MissionAgencyStatus.REPLACED.getId());
    }

    public List<JSONObject> getListAgencyMissionMuaHangContainOrder(
            int agency_id,
            int mission_type_id,
            String code) {
        return this.masterDB.find("SELECT * FROM agency_mission WHERE agency_id = " + agency_id +
                " AND mission_type_id = " + mission_type_id +
                " AND mission_data LIKE '%" + code + "%'");
    }

    public List<JSONObject> getListMissionBXHByAgencyAndType(int agency_id, int type) {
        return this.masterDB.find("SELECT t1.id, t1.code, t1.name, t1.status" +
                " FROM mission_bxh_agency_join t LEFT JOIN mission_bxh t1 ON t1.id = t.mission_bxh_id WHERE t.agency_id = " + agency_id +
                " AND t1.type = " + type);
    }

    public boolean onOffRepeatMissionBXH(int id, int is_repeat) {
        return this.masterDB.update("UPDATE mission_bxh SET is_repeat = " + is_repeat +
                " WHERE id = " + id);
    }

    public boolean stopMissionBXHNotRepeat(int mission_period_id) {
        return this.masterDB.update("UPDATE mission_bxh SET status = " + MissionBXHStatus.STOP.getId() +
                " WHERE is_repeat = 0 AND mission_period_id = " + mission_period_id);
    }

    public boolean rejectAgencyOrder(int id) {
        return this.masterDB.update("UPDATE agency_order SET accumulate_mission_status = 0, accumulate_mission_over_status=1 WHERE id = " + id);
    }

    public boolean saveMissionSettingEndDate(int id, long stopTime, String note) {
        return this.masterDB.update("UPDATE mission_setting SET end_date = " + parseDateToSql(DateTimeUtils.getDateTime(stopTime)) + "," +
                " note = " + parseStringToSql(note) +
                " WHERE id = " + id);
    }

    public List<JSONObject> getListMissionSettingWaitingStop(int scheduleRunningLimit) {
        return this.masterDB.find("SELECT * FROM mission_setting WHERE status = " + MissionSettingStatus.RUNNING.getId() + " AND end_date is not null AND end_date <= NOW()");
    }

    public boolean resetEstimateAgencyOrderAutoTichLuy() {
        return this.masterDB.update("UPDATE agency_order SET" +
                " accumulate_mission_date = null, accumulate_mission_over_status = 0" +
                "  WHERE status IN (0,5) AND accumulate_mission_status = 0");
    }
}