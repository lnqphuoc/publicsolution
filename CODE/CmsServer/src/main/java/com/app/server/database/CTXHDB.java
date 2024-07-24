package com.app.server.database;

import com.app.server.config.ConfigInfo;
import com.app.server.data.dto.ctxh.CTXHAgencyResult;
import com.app.server.data.entity.VoucherReleasePeriodEntity;
import com.app.server.enums.AgencyStatus;
import com.app.server.enums.VoucherStatus;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.dbconn.ClientManager;
import com.ygame.framework.dbconn.ManagerIF;
import com.ygame.framework.utils.ConvertUtils;
import jdk.nashorn.internal.scripts.JO;
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
public class CTXHDB extends BaseDB {
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
                "SELECT p.id, p.offer_value, p1.level" +
                        " FROM promo_offer p" +
                        " LEFT JOIN promo_limit p1 ON p1.id = p.promo_limit_id" +
                        " WHERE p.promo_id = " + promo_id +
                        " ORDER BY p1.level ASC"
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

    public int insertVoucherReleasePeriod(VoucherReleasePeriodEntity entity) {
        return this.masterDB.insert(
                "INSERT INTO voucher_release_period(code,name,image,status,offer_type,limit_data,creator_id,max_percent_per_order) VALUES(" +
                        parseStringToSql(entity.getCode()) + "," +
                        parseStringToSql(entity.getName()) + "," +
                        parseStringToSql(entity.getImage()) + "," +
                        parseIntegerToSql(entity.getStatus()) + "," +
                        parseStringToSql(entity.getOffer_type()) + "," +
                        parseStringToSql(entity.getLimit_data()) + "," +
                        parseIntegerToSql(entity.getCreator_id()) + "," +
                        parseIntegerToSql(entity.getMax_percent_per_order()) + ")");
    }

    public int insertVoucherReleasePeriodItem(int voucher_release_period_id, int item_id, int item_quantity, int staff_id) {
        return this.masterDB.insert(
                "INSERT INTO voucher_release_period_item(voucher_release_period_id,item_id,item_quantity) VALUES(" +
                        parseIntegerToSql(voucher_release_period_id) + "," +
                        parseIntegerToSql(item_id) + "," +
                        parseIntegerToSql(item_quantity) + ")");
    }

    public JSONObject getVRPByCode(String code) {
        return this.masterDB.getOne(
                "SELECT * FROM voucher_release_period WHERE code = " + parseStringToSql(code));
    }

    public JSONObject getVRPById(int id) {
        return this.masterDB.getOne(
                "SELECT * FROM voucher_release_period WHERE id = " + parseIntegerToSql(id));
    }

    public List<JSONObject> getListVRPItem(int id) {
        return this.masterDB.find("SELECT * FROM voucher_release_period_item WHERE voucher_release_period_id = " + id);
    }

    public List<JSONObject> getListVoucherByVRP(int voucher_release_period_id) {
        return this.masterDB.find("SELECT * FROM voucher WHERE voucher_release_period_id = " + voucher_release_period_id);
    }

    public boolean activeVRP(int id, int staff_id, int status) {
        return this.masterDB.update("UPDATE voucher_release_period SET status = " + status + "," +
                " active_date = NOW()," +
                " modified_date = NOW()," +
                " modifier_id = " + staff_id +
                " WHERE id = " + id);
    }

    public boolean cancelVRP(int id, int staff_id, int status, String note) {
        return this.masterDB.update("UPDATE voucher_release_period SET status = " + status + "," +
                " modified_date = NOW()," +
                " modifier_id = " + staff_id + "," +
                " note = " + parseStringToSql(note) +
                " WHERE id = " + id);
    }

    public boolean clearVRPDetail(int voucher_release_period_id) {
        return this.masterDB.update("DELETE FROM voucher_release_period_item WHERE voucher_release_period_id=" + voucher_release_period_id);
    }

    public boolean updateVoucherReleasePeriod(Integer id, VoucherReleasePeriodEntity entity, int staff_id) {
        return this.masterDB.update("UPDATE voucher_release_period SET " +
                " name = " + parseStringToSql(entity.getName()) + "," +
                " code = " + parseStringToSql(entity.getCode()) + "," +
                " image = " + parseStringToSql(entity.getImage()) + "," +
                " limit_data = " + parseStringToSql(entity.getLimit_data()) + "," +
                " modified_date = NOW()," +
                " modifier_id = " + staff_id +
                " WHERE id = " + id);
    }

    public boolean checkVRPCanEdit(int voucher_release_period_id) {
        return this.getListVoucherByVRP(voucher_release_period_id).size() == 0;
    }

    public List<JSONObject> getPromoRunningUseVRP(String key, int status) {
        return this.masterDB.find("SELECT t1.id, t1.code,t1.name, t1.promo_type, t1.start_date, t1.end_date, t1.status" +
                "FROM (SELECT promo_id FROM promo_offer WHERE voucher_data LIKE (%" + key + "%) GROUP BY promo_id) as t" +
                " LEFT JOIN promo t1 ON t1.id = t.promo_id AND t1.status = " + status);
    }

    public int getTongKhachHangThamGia(int program_id) {
        return this.masterDB.getTotal("SELECT id FROM agency_bxh_info WHERE program_id = " + program_id);
    }

    public long getTongGiaTriTichLuy(int program_id) {
        JSONObject rs = this.masterDB.getOne(
                "SELECT sum(rank_value) as total FROM agency_bxh_info WHERE program_id = " + program_id);
        if (rs != null) {
            return ConvertUtils.toLong(rs.get("total"));
        }

        return 0;
    }

    public int getTotalVoucherByVRP(int voucher_release_period_id) {
        return this.masterDB.getTotal("SELECT * FROM voucher WHERE voucher_release_period_id = " + voucher_release_period_id);
    }

    public int getTotalVoucherUsedByVRP(int voucher_release_period_id) {
        return this.masterDB.getTotal("SELECT * FROM voucher WHERE voucher_release_period_id = " + voucher_release_period_id
                + " AND status = " + VoucherStatus.USED.getId());
    }

    public long getTotalValueVoucherByVRP(int voucher_release_period_id) {
        JSONObject rs = this.masterDB.getOne("SELECT sum(total_value) as total FROM voucher WHERE voucher_release_period_id = " + voucher_release_period_id);
        if (rs != null) {
            return ConvertUtils.toLong(rs.get("total"));
        }

        return 0;
    }

    public long getTotalValueVoucherUsedByVRP(int voucher_release_period_id) {
        JSONObject rs = this.masterDB.getOne("SELECT sum(total_value) as total FROM voucher WHERE voucher_release_period_id = " + voucher_release_period_id
                + " AND status = " + VoucherStatus.USED.getId());
        if (rs != null) {
            return ConvertUtils.toLong(rs.get("total"));
        }

        return 0;
    }

    public List<JSONObject> getListAgencyRank(int id, int rank_value) {
        return this.masterDB.find(
                "SELECT agency_id,rank_value FROM agency_bxh_info WHERE program_id=" + id +
                        " AND rank_value>= " + rank_value + " ORDER BY rank_value DESC,rank_date ASC,id ASC"
        );
    }

    public JSONObject getAgencyRank(int agency_id, int promo_id, int rank_value) {
        return this.masterDB.getOne(
                "SELECT t.* FROM (SELECT agency_id,rank_value FROM agency_bxh_info WHERE program_id=" + promo_id +
                        " AND rank_value>= " + rank_value + " ORDER BY rank_value DESC,rank_date ASC) as t " +
                        " WHERE t.agency_id = " + agency_id
        );
    }

    public List<JSONObject> getListPromoOffer(int promo_id) {
        return this.masterDB.find("SELECT * FROM promo_offer" +
                " WHERE promo_id = " + promo_id +
                " ORDER BY id ASC");
    }

    public int insertBXHAgencyJoin(int agency_id, int promo_id) {
        return this.masterDB.insert("INSERT INTO bxh_agency_join(agency_id, promo_id)" +
                " VALUES(" +
                agency_id + ", " + promo_id + ")");
    }

    public JSONObject getPromoBasicData(int promo_id) {
        return this.masterDB.getOne("SELECT id,code,name,promo_type,start_date,end_date,status,condition_type" +
                " FROM promo WHERE id = " + promo_id);
    }
}