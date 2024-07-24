package com.app.server.database;

import com.app.server.config.ConfigInfo;
import com.app.server.enums.MissionBXHStatus;
import com.app.server.enums.PromoActiveStatus;
import com.app.server.utils.JsonUtils;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.dbconn.ClientManager;
import com.ygame.framework.dbconn.ManagerIF;
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
import java.util.Date;
import java.util.List;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class PomDB extends BaseDB {
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

    public JSONObject getQOM(int id) {
        return this.masterDB.getOne("SELECT * FROM supplier_quotation WHERE id = " + id);
    }

    public int getTotalQOM() {
        return this.masterDB.getTotal("SELECT * FROM supplier_quotation");
    }

    public int createQOM(String code, String name, int business_department_id, Date dateTime, String data) {
        int id = 0;
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String query =
                    "INSERT INTO supplier_quotation(" +
                            "code," +
                            "name," +
                            "start_date," +
                            "data," +
                            "business_department_id)" +
                            " VALUES(" +
                            "'" + code + "'," +
                            "'" + name + "'," +
                            parseDateToSql(dateTime) + "," +
                            "?," +
                            business_department_id +
                            ")";
            try (PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, data);
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

    public boolean approveQOM(int id) {
        return this.masterDB.update("UPDATE supplier_quotation SET start_date = NOW()," +
                " status = " + PromoActiveStatus.RUNNING.getId() +
                " WHERE id = " + id);
    }

    public boolean stopQOM(int id) {
        return this.masterDB.update("UPDATE supplier_quotation SET end_date = NOW()," +
                " status = " + PromoActiveStatus.STOPPED.getId() +
                " WHERE id = " + id);
    }

    public boolean updateQOMData(int id, String data) {
        boolean rs = false;
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String query =
                    "UPDATE supplier_quotation SET" +
                            " data = ?" +
                            " WHERE id = " + id;
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

    public boolean updateQOM(
            int id,
            String name,
            Date start_date,
            String data) {
        boolean rs = false;
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String query =
                    "UPDATE supplier_quotation SET" +
                            " name = " + parseStringToSql(name) + "," +
                            " start_date = " + parseDateToSql(start_date) + "," +
                            " data = ?" +
                            " WHERE id = " + id;
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

    public List<JSONObject> getListQOMRunning(int business_department_id) {
        return this.masterDB.find("SELECT * FROM supplier_quotation WHERE business_department_id=" + business_department_id);
    }
}