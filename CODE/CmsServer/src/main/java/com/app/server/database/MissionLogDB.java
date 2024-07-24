package com.app.server.database;

import com.app.server.config.ConfigInfo;
import com.app.server.data.SessionData;
import com.app.server.data.dto.mission.ApplyObjectRequest;
import com.app.server.data.dto.mission.UpdateMissionConfigRequest;
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
public class MissionLogDB extends BaseDB {
    private LogDB masterDB;

    @Autowired
    public void setMasterDB(LogDB masterDB) {
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

    public List<JSONObject> getListAgencyMissionBXHAgencyByPeriod(int mission_bxh_id, Date start_date, Date end_date) {
        return this.masterDB.find("SELECT * FROM mission_bxh_agency_join_history" +
                " WHERE mission_bxh_id = " + mission_bxh_id +
                " AND created_date = " + parseDateToSql(start_date) +
                " AND created_date = " + parseDateToSql(end_date) +
                " ORDER BY position DESC, id ASC");
    }

    public int getTongKhachHangThamGiaBXHByPeriod(int mission_bxh_id, Date start_date, Date end_date) {
        JSONObject result = this.masterDB.getOne("SELECT count(*) as total" +
                " FROM mission_bxh_agency_join_history t" +
                " WHERE t.mission_bxh_id = " + mission_bxh_id +
                " AND created_date = " + parseDateToSql(start_date) +
                " AND created_date = " + parseDateToSql(end_date));
        if (result != null) {
            return ConvertUtils.toInt(result.get("total"));
        }
        return 0;
    }

    public JSONObject getAgencyMissionHistory(int agency_mission_id) {
        return this.masterDB.getOne("SELECT * FROM agency_mission_history WHERE agency_mission_id = " + agency_mission_id);
    }

    public int getTotalMissionPoint(String query) {
        JSONObject js = this.masterDB.getOne("SELECT sum(update_point) as total" +
                " FROM (" + query + ") as a");
        if (js != null) {
            return ConvertUtils.toInt(js.get("total"));
        }
        return 0;
    }

    public int saveMissionConfigHistory(SessionData sessionData, UpdateMissionConfigRequest request) {
        int id = 0;
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE_LOG);
            con = cm.borrowClient();
            String query = "INSERT INTO mission_config_history(data,creator_id) VALUES(?," + sessionData.getId() + ");";
            try (PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, JsonUtils.Serialize(request));
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
}