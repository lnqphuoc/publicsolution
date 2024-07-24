package com.app.server.database;

import com.app.server.config.ConfigInfo;
import com.app.server.utils.JsonUtils;
import com.mysql.cj.log.Log;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.dbconn.ClientManager;
import com.ygame.framework.dbconn.ManagerIF;
import com.ygame.framework.utils.ConvertUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MasterDB {
    public JSONObject getOne(String query) {
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
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
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
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
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
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
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
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
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
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


}