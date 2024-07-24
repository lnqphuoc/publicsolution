package com.app.server.database;

import com.app.server.config.ConfigInfo;
import com.app.server.data.dto.catalog.Catalog;
import com.app.server.enums.AgencyCatalogDetailStatus;
import com.app.server.enums.AgencyCatalogRequestStatus;
import com.app.server.enums.OrderStatus;
import com.app.server.utils.JsonUtils;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.dbconn.ClientManager;
import com.ygame.framework.dbconn.ManagerIF;
import com.ygame.framework.utils.ConvertUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class CatalogDB extends BaseDB {
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

    public List<JSONObject> getListCategoryInCatalog(int catalog_id) {
        return this.masterDB.find(
                "SELECT t1.*" +
                        "FROM catalog_category t" +
                        " JOIN category t1 ON t1.id = t.category_id" +
                        " WHERE t.catalog_id = " + catalog_id
        );
    }

    public int insertCategory(String name, String image, int is_show, int creator_id) {
        int id = 0;
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String query = "INSERT INTO catalog(" +
                    "name," +
                    "image," +
                    "is_show," +
                    "created_date," +
                    "creator_id)" +
                    " VALUES(" +
                    "?" + "," +
                    parseStringToSql(image) + "," +
                    parseIntegerToSql(is_show) + "," +
                    "NOW()" + "," +
                    parseIntegerToSql(creator_id) +
                    ")";
            try (PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, name);
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

    public JSONObject getCatalog(int id) {
        return this.masterDB.getOne(
                "SELECT * FROM catalog WHERE id = " + id
        );
    }

    public boolean updateCatalog(
            int id,
            String name,
            String image,
            int is_show,
            int staff_id) {
        boolean status = false;
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String sql = "UPDATE catalog SET " +
                    "name" + " = ?," +
                    "image" + " = " + parseStringToSql(image) + "," +
                    "is_show" + " = " + parseIntegerToSql(is_show) + "," +
                    "modifier_id" + " = " + parseIntegerToSql(staff_id) + "," +
                    "modified_date = NOW()" +
                    " WHERE id = " + id;
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, name);
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

    public int addCategory(Integer categoryId, int catalogId) {
        return this.masterDB.insert(
                "INSERT INTO catalog_category(" +
                        "catalog_id," +
                        "category_id" +
                        ") VALUES(" +
                        catalogId + "," +
                        categoryId +
                        ")"
        );
    }

    public boolean deleteCategory(Integer categoryId, int catalogId) {
        return this.masterDB.update(
                "DELETE FROM catalog_category WHERE " +
                        " catalog_id = " + catalogId +
                        " AND category_id = " + categoryId
        );
    }

    public int getLastPriority() {
        JSONObject rs = this.masterDB.getOne(
                "SELECT * FROM catalog" +
                        " ORDER BY priority DESC" +
                        " LIMIT 1"
        );
        if (rs != null) {
            return ConvertUtils.toInt(rs.get("priority"));
        }

        return 0;
    }

    public boolean updateCatalogPriority(int id, int priority) {
        return this.masterDB.update(
                "UPDATE catalog SET priority = " + priority +
                        " WHERE id = " + id
        );
    }

    public boolean approveAgencyCatalogDetail(int id, int status, int staff_id, String note) {
        return this.masterDB.update(
                "UPDATE agency_catalog_detail" +
                        " SET status = " + status + "," +
                        " modified_date = NOW()," +
                        " confirmer_id = " + staff_id + "," +
                        " note = " + parseStringToSql(note) +
                        " WHERE id = " + id
        );
    }

    public JSONObject getAgencyCatalogDetail(int id) {
        return this.masterDB.getOne(
                "SELECT * FROM agency_catalog_detail WHERE id = " + id
        );
    }

    public boolean rejectAgencyCatalogDetail(int id, int status, int staff_id, String note) {
        return this.masterDB.update(
                "UPDATE agency_catalog_detail" +
                        " SET status = " + status + "," +
                        " modified_date = NOW()," +
                        " confirmer_id = " + staff_id + "," +
                        " note = " + parseStringToSql(note) +
                        " WHERE id = " + id
        );
    }

    public boolean finishAgencyCatalogRequest(int id) {
        return this.masterDB.update(
                "UPDATE agency_catalog_request" +
                        " SET status = " + AgencyCatalogRequestStatus.FINISH.getId() + "," +
                        " modified_date = NOW()" +
                        " WHERE id = " + id
        );
    }

    public JSONObject getAgencyCatalogDetailWaiting(int agency_catalog_request_id, int status) {
        return this.masterDB.getOne(
                "SELECT * FROM agency_catalog_detail" +
                        " WHERE agency_catalog_request_id = " + agency_catalog_request_id +
                        " AND status = " + status +
                        " LIMIT 1"
        );
    }

    public int insertAgencyCatalogRequest(int agency_id, String note, String code, int staff_id) {
        return this.masterDB.insert(
                "INSERT INTO agency_catalog_request(" +
                        "agency_id," +
                        "note," +
                        "code," +
                        "creator_id" +
                        ") VALUES(" +
                        agency_id + "," +
                        "'" + note + "'," +
                        "'" + code + "'," +
                        staff_id +
                        ")"
        );
    }

    public int insertAgencyCatalogDetail(int agency_catalog_request_id, Integer catalog_id, int agency_id) {
        return this.masterDB.insert(
                "INSERT INTO agency_catalog_detail(" +
                        "agency_catalog_request_id," +
                        "agency_id," +
                        "catalog_id" +
                        ") VALUES(" +
                        agency_catalog_request_id + "," +
                        agency_id + "," +
                        catalog_id +
                        ")"
        );
    }

    public List<JSONObject> getListAgency(int catalog_id) {
        return this.masterDB.find(
                "SELECT t1.id," +
                        " t1.shop_name," +
                        " t1.avatar," +
                        " t1.phone," +
                        " t1.code," +
                        " t1.membership_id," +
                        " t1.business_department_id" +
                        " FROM ( SELECT agency_id" +
                        " FROM agency_catalog_detail" +
                        " WHERE catalog_id = " + catalog_id +
                        " AND status = " + AgencyCatalogDetailStatus.APPROVED.getId() +
                        " GROUP BY agency_id) as t" +
                        " LEFT JOIN agency t1 ON t.agency_id = t1.id"
        );
    }

    public List<JSONObject> getListCategoryByRequest(int agency_catalog_request_id) {
        return this.masterDB.find(
                "SELECT t.*, t1.name,t1.image, t.note as reason" +
                        " FROM agency_catalog_detail t" +
                        " LEFT JOIN catalog t1 ON t1.id = t.catalog_id" +
                        " WHERE t.agency_catalog_request_id = " + agency_catalog_request_id
        );
    }

    public boolean processingAgencyCatalogRequest(int id) {
        return this.masterDB.update(
                "UPDATE agency_catalog_request" +
                        " SET status = " + AgencyCatalogRequestStatus.PROCESSING.getId() + "," +
                        " modified_date = NOW()" +
                        " WHERE id = " + id
        );
    }

    public JSONObject getAgencyCatalogWaitingByCatalog(int agency_id, int catalog_id, int status) {
        return this.masterDB.getOne(
                "SELECT * FROM agency_catalog_detail" +
                        " WHERE agency_id = " + agency_id +
                        " AND catalog_id = " + catalog_id +
                        " AND status = " + status +
                        " LIMIT 1"
        );
    }

    public JSONObject getCatalogOfCategory(Integer category_id) {
        return this.masterDB.getOne(
                "SELECT * FROM catalog_category WHERE category_id = " + category_id + " LIMIT 1"
        );
    }

    public int saveCatalogHistory(
            int catalog_id, String data, int type) {
        return this.masterDB.insert(
                "INSERT INTO catalog_history(" +
                        "catalog_id," +
                        "type," +
                        "data" +
                        ") VALUES(" +
                        catalog_id + "," +
                        type + "," +
                        "'" + data + "'" +
                        ")"
        );
    }

    public int getCount() {
        return this.masterDB.getTotal(
                "SELECT * FROM agency_catalog_request"
        );
    }

    public boolean rejectAllCatalog(int agency_id, String note, int staff_id) {
        return this.masterDB.update(
                "UPDATE agency_catalog_detail" +
                        " SET status = " + AgencyCatalogDetailStatus.REJECT.getId() + "," +
                        " note = " + parseStringToSql(note) + "," +
                        " confirmer_id = " + staff_id + "," +
                        " modified_date = NOW()" +
                        " WHERE agency_id = " + agency_id +
                        " AND (status = " + AgencyCatalogDetailStatus.APPROVED.getId() +
                        " OR status = " + AgencyCatalogDetailStatus.WAITING.getId() + ")"
        );
    }

    public boolean finishAllAgencyCatalogRequest(int agency_id) {
        return this.masterDB.update(
                "UPDATE agency_catalog_request" +
                        " SET status = " + AgencyCatalogRequestStatus.FINISH.getId() + "," +
                        " modified_date = NOW()" +
                        " WHERE agency_id = " + agency_id +
                        " AND (status =" + AgencyCatalogRequestStatus.WAITING.getId() +
                        " OR status = " + AgencyCatalogRequestStatus.PROCESSING.getId() + ")"
        );
    }
}