package com.app.server.database;

import com.app.server.config.ConfigInfo;
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
public class DamMeDB extends BaseDB {
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

    public int insertAgencyCSDMModifyOrder(int agencyId,
                                           String key,
                                           int agencyOrderId,
                                           long giamGiaTriDonHang,
                                           String agency_order_code,
                                           String product_info) {
        return this.masterDB.insert(
                "INSERT INTO agency_csdm_modify_accumulate_order(" +
                        "agency_id," +
                        "type," +
                        "agency_order_id," +
                        "agency_order_code," +
                        "data," +
                        "product_info)" +
                        " VALUES(" +
                        agencyId + "," +
                        parseStringToSql(key) + "," +
                        agencyOrderId + "," +
                        parseStringToSql(agency_order_code) + "," +
                        giamGiaTriDonHang + "," +
                        parseStringToSql(product_info) +
                        ")"
        );
    }

    public JSONObject getAgencyCSDMModifyOrder(String type, int agency_order_id) {
        return this.masterDB.getOne(
                "SELECT * FROM agency_csdm_modify_accumulate_order" +
                        " WHERE type = " + parseStringToSql(type) +
                        " AND agency_order_id = " + agency_order_id
        );
    }

    public int insertAgencyCSDMModifyOrderOfProduct(int agencyId, String key, int agencyOrderId, String productId, String product_info, long data) {
        int id = 0;
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String query = "INSERT INTO agency_csdm_modify_accumulate_order(" +
                    "agency_id," +
                    "type," +
                    "agency_order_id," +
                    "product_id," +
                    "product_info," +
                    "data)" +
                    " VALUES(" +
                    agencyId + "," +
                    parseStringToSql(key) + "," +
                    agencyOrderId + "," +
                    "'" + productId + "'," +
                    "?," +
                    data +
                    ")";
            try (PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, product_info);
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

    public JSONObject getAgencyCSDMModifyOrderOfProduct(String type, int agency_order_id, int product_id) {
        return this.masterDB.getOne(
                "SELECT * FROM agency_csdm_modify_accumulate_order" +
                        " WHERE type = " + parseStringToSql(type) +
                        " AND agency_order_id = " + agency_order_id +
                        " AND product_id LIKE CONCAT('%\"'," + product_id + ",'\"%')"
        );
    }

    public int insertAgencyCSDMModifyValue(int agency_id, int promo_id, int type, long value, String note) {
        return this.masterDB.insert(
                "INSERT INTO agency_csdm_modify_value(" +
                        "agency_id," +
                        "promo_id," +
                        "type," +
                        "data," +
                        "note)" +
                        " VALUES(" +
                        agency_id + "," +
                        promo_id + "," +
                        type + "," +
                        value + "," +
                        parseStringToSql(note) +
                        ")"
        );
    }

    public JSONObject getAgencyCSDMModifyValue(int id) {
        return this.masterDB.getOne(
                "SELECT * FROM agency_csdm_modify_value" +
                        " WHERE id = " + id
        );
    }

    public List<JSONObject> getPromoLimitList(int promo_id) {
        return this.masterDB.find(
                "SELECT id, from_value" +
                        " FROM promo_limit_group" +
                        " WHERE promo_id = " + promo_id +
                        " ORDER BY from_value ASC"
        );
    }

    public List<JSONObject> getPromoOfferList(int promo_id) {
        return this.masterDB.find(
                "SELECT id, offer_value" +
                        " FROM promo_offer" +
                        " WHERE promo_id = " + promo_id +
                        " ORDER BY offer_value ASC"
        );
    }

    public boolean createPhieuDieuChinhFailed(int id) {
        return this.masterDB.update(
                "UPDATE agency_csdm_modify_value SET status = 0 WHERE id = " + id
        );
    }

    public long getTongUuDaiDamMe(int promo_id, int agency_id) {
        JSONObject rs = this.masterDB.getOne(
                "SELECT sum(product_total_dm_price) as total" +
                        " FROM agency_csdm_claim" +
                        " WHERE agency_id = " + agency_id +
                        " AND promo_id = " + promo_id
        );
        if (rs == null) {
            return 0;
        }
        return ConvertUtils.toLong(rs.get("total"));
    }

    public long getTongUuDaiDamMeOfPromo(int promo_id) {
        JSONObject rs = this.masterDB.getOne(
                "SELECT sum(product_total_dm_price) as total" +
                        " FROM agency_csdm_claim" +
                        " WHERE promo_id = " + promo_id
        );
        return ConvertUtils.toLong(rs.get("total"));
    }

    public List<JSONObject> getListDieuChinhDamMe(String data) {
        return this.masterDB.find("SELECT * FROM " + data);
    }
}