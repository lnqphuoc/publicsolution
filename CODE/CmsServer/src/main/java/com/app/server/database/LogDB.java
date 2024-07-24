package com.app.server.database;

import com.app.server.config.ConfigInfo;
import com.app.server.utils.JsonUtils;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.dbconn.ClientManager;
import com.ygame.framework.dbconn.ManagerIF;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import org.json.simple.JSONObject;
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
public class LogDB {
    public JSONObject getOne(String query) {
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE_LOG);
            con = cm.borrowClient();
            try (PreparedStatement stmt = con.prepareStatement(query)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return JsonUtils.convertToJSON(rs);
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

    public List<JSONObject> find(String query) {
        List<JSONObject> result = new ArrayList<>();
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE_LOG);
            con = cm.borrowClient();
            try (PreparedStatement stmt = con.prepareStatement(query)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        result.add(JsonUtils.convertToJSON(rs));
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
        return result;
    }

    public int getTotal(String query) {
        List<JSONObject> result = new ArrayList<>();
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE_LOG);
            con = cm.borrowClient();
            String sql = "SELECT count(*) as total FROM (" + query + ") as a";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return ConvertUtils.toInt(JsonUtils.convertToJSON(rs).get("total"));
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
        return 0;
    }

    public int insert(String query) {
        int id = 0;
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE_LOG);
            con = cm.borrowClient();

            try (PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
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

    public boolean update(String query) {
        boolean status = false;
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE_LOG);
            con = cm.borrowClient();
            try (PreparedStatement stmt = con.prepareStatement(query)) {
                int row = stmt.executeUpdate();
                if (row > 0) {
                    status = true;
                }
            }
        } catch (Exception ex) {
            status = false;
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
        return status;
    }

    public int getTotalAgencyAccess() {
        return this.getTotal(
                "SELECT agency_id, DATE(created_date)" +
                        " FROM agency_access_app_log" +
                        " GROUP BY agency_id, DATE(created_date)"
        );
    }

    public int sumAgencyAccess() {
        JSONObject rs = this.getOne(
                "SELECT SUM(quantity) as total" +
                        " FROM (SELECT agency_id, DATE(created_date), count(*) as quantity" +
                        " FROM agency_access_app_log" +
                        " GROUP BY agency_id, DATE(created_date)) as a"
        );
        if (rs != null) {
            return ConvertUtils.toInt(rs.get("total"));
        }
        return 0;
    }

    public List<JSONObject> filterAgencyAccess(int page, int is_limit) {
        return this.find(
                "SELECT agency_id, DATE(created_date) as date, count(*) as quantity" +
                        " FROM agency_access_app_log" +
                        " GROUP BY agency_id, DATE(created_date)"
        );
    }

    public List<JSONObject> filter(String query, int offset, int pageSize, int isLimit) {
        if (isLimit == 1) {
            query += " LIMIT " + offset + "," + pageSize;
        }
        return this.find(query);
    }

    public int sum(String query) {
        JSONObject rs = this.getOne(
                "SELECT SUM(quantity) as total" + " FROM (" + query + ") as a");
        if (rs != null) {
            return ConvertUtils.toInt(rs.get("total"));
        }
        return 0;
    }

    public int getTotalMissionPoint(int agency_id, Date startDate, Date endDate) {
        JSONObject js = this.getOne("SELECT SUM(mission_point) as total" +
                " FROM agency_mission_bxh_history WHERE agency_id = " + agency_id +
                " AND created_date >= '" + DateTimeUtils.toString(startDate, "yyyy-MM-dd") + " 00:00:00'" +
                " AND created_date >= '" + DateTimeUtils.toString(endDate, "yyyy-MM-dd") + " 23:59:59'");
        if (js == null) {
            return 0;
        }
        return ConvertUtils.toInt(js.get("total"));
    }

    public int getTongKhachHangThamGiaBXHByPeriod(int mission_bxh_id, String mission_period_code) {
        JSONObject result = this.getOne("SELECT count(*) as total" +
                " FROM mission_bxh_agency_join t" +
                " WHERE t.mission_bxh_id = " + mission_bxh_id +
                " AND mission_period_code = '" + mission_period_code + "'");
        if (result != null) {
            return ConvertUtils.toInt(result.get("total"));
        }
        return 0;
    }
}